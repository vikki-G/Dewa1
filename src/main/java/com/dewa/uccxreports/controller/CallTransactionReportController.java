package com.dewa.uccxreports.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.validation.Valid;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewa.uccxreports.dto.AgentReportRequestDTO;
import com.dewa.uccxreports.dto.AgentReportResponseDTO;
import com.dewa.uccxreports.dto.SectionDto;
import com.dewa.uccxreports.entity.DbConnection;
import com.dewa.uccxreports.util.DateRange;
import com.dewa.uccxreports.util.Response;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent")

public class CallTransactionReportController {

	private static final Logger log = LogManager.getLogger("CallTransactionReportController");

	@Autowired
	private Environment env;

	@Autowired
	private DateRange dateRange;

	@Autowired
	private DbConnection dbConnection;

	@GetMapping("/list/{supportId}")
	public Response<List<String>> getSupportType(@PathVariable String supportId) {
		// log.info("supportIds---->" + supportId);
		List<String> supportTypeList = new ArrayList<>();
		supportTypeList.add("All");
		String Query = env.getProperty("getSupportTypeByRole");
		String supportIdMod = supportId.replace(",", "','").trim();
		supportIdMod = "'" + supportIdMod.replaceAll("\\s", "") + "'";

		@SuppressWarnings("unused")
		String[] supportIDS = supportIdMod.split(",");
		// log.info(supportIdMod + "test");

		Connection pom_con = null;
		try {

			pom_con = dbConnection.getConnection();
			PreparedStatement stmt = pom_con.prepareStatement(Query.replace("@query", supportIdMod));

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				supportTypeList.add(rs.getString("SupportType"));
			}
			// log.info(supportTypeList);
		} catch (Exception e) {
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<List<String>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while getting supportTypeList").setResponse(null);
		}
		return new Response<List<String>>(200, true, HttpStatus.OK,
				supportTypeList.size() + " SupportTypeList has been retrieved").setResponse(supportTypeList);
	}

	@PostMapping("/agentReport")
	public List<AgentReportResponseDTO> getTjxData(@Valid @RequestBody AgentReportRequestDTO agentRequestDto)
			throws SQLException {

		// log.info("agentRequestDto : " + agentRequestDto);
		List<AgentReportResponseDTO> agentRequestDtos = new ArrayList<>();
		String startDate = null;
		String endDate = null;
		String SupportType = agentRequestDto.getType().replaceAll("\\s", "");

		String st_edDates = dateRange.getDateRanges(agentRequestDto.getDateRange(), agentRequestDto.getStartDate(),
				agentRequestDto.getEndDate());
		String arr[] = st_edDates.split("###");
		startDate = arr[0];
		endDate = arr[1];

		startDate = startDate.replace("/", "-");
		endDate = endDate.replace("/", "-");

		log.info("Start Date before calling DB:" + startDate + "end date=> " + endDate);

		Connection tjxConnection = null;
		CallableStatement callableStatement = null;
		try {

			tjxConnection = dbConnection.getConnection();
			log.info("Call Transaction Report Connection created successfully for the DB");

			Properties properties = new Properties();

			try {

				File a_config = ResourceUtils.getFile("classpath:config.properties");
				properties.load(new FileInputStream(a_config.toPath().toString()));

			} catch (IOException e) {
				e.printStackTrace();
				log.error("error while reading the calltransactionreport storedprocedure");
			}

			// Get the stored procedure name from the property file

			// callableStatement = tjxConnection.prepareCall("{ call
			// DEWA_AGENT_REPORT(?,?,?) }");

			try {
				String callTransactionReport = properties.getProperty("CallTransactionReport");
				callableStatement = tjxConnection.prepareCall("{ call " + callTransactionReport + "(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();

				agentRequestDtos = agentRequestDtos(resultset);

				resultset.close();

			} catch (SQLException e) {
				e.printStackTrace();
				log.error("SQLException In CallTransactionReportController");
			}

		} catch (Exception e) {
			log.error("Error while getting call transaction report records from database : " + e);
			log.error("stack strace : " + Arrays.toString(e.getStackTrace()));
			return null;
		} finally {
			if (callableStatement != null) {
				callableStatement.close();
			}
			if (tjxConnection != null) {
				tjxConnection.close();
			}
		}
		return agentRequestDtos;
	}

	private List<AgentReportResponseDTO> agentRequestDtos(ResultSet resultset) throws Exception {
		List<AgentReportResponseDTO> agentRequestDtos = new ArrayList<>();
		AgentReportResponseDTO tjxDto = null;
		Properties properties = new Properties();

		try {

			File a_config = ResourceUtils.getFile("classpath:config.properties");
			properties.load(new FileInputStream(a_config.toPath().toString()));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("error while reading the GET_SECTIONS_IN_ORDER storedprocedure from the property file");
		}
		String getSectionsInOrder = properties.getProperty("GetSectionsInOrder");
		Connection connection = null;

		CallableStatement callableStatement = null;
		try {
			connection = dbConnection.getConnection();
			// callableStatement = connection.prepareCall("{call GET_SECTIONS_IN_ORDER}");
			callableStatement = connection.prepareCall("{call " + getSectionsInOrder + "}");
			callableStatement.execute();

		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error While Execute GET_SECTIONS_IN_ORDER storedprocedure");
		}
		ResultSet resultSet2 = callableStatement.getResultSet();
		List<SectionDto> dto = new ArrayList<>();
		while (resultSet2.next()) {
			SectionDto sectionDto = new SectionDto();
			sectionDto.setSection(resultSet2.getString("SECTION"));
			dto.add(sectionDto);
		}
		while (resultset.next()) {

			tjxDto = new AgentReportResponseDTO();

			if (resultset.getString("NAME") != null) {
				tjxDto.setName(resultset.getString("NAME"));
			} else {
				tjxDto.setName(""); // Set a default value or handle it according to your requirements
			}
//			String name = resultset.getString("NAME");
//			tjxDto.setName(name != null ? name : "");

			tjxDto.setContact(resultset.getString("CONTACT"));
			tjxDto.setType(resultset.getString("TYPE"));
			tjxDto.setOffered(resultset.getString("OFFERED"));
			tjxDto.setAnswered(resultset.getString("ANSWERED"));
			tjxDto.setAbandoned(resultset.getString("ABANDONED"));
			tjxDto.setPerAnswered(resultset.getString("PER_ANSWERED") + "%");
			agentRequestDtos.add(tjxDto);

		}
		List<AgentReportResponseDTO> finalAgentSummaryList = new ArrayList<>();
		for (SectionDto sectionDto : dto) {
			for (AgentReportResponseDTO agentDto : agentRequestDtos) {
				if (sectionDto.getSection().equalsIgnoreCase(agentDto.getType())) {
					finalAgentSummaryList.add(agentDto);
				}
			}
		}

		return finalAgentSummaryList;
	}

	@PostMapping("/agentReport/excel")
	public HttpEntity<ByteArrayResource> downloadExcel(@Valid @RequestBody AgentReportRequestDTO agentReportRequestDto)
			throws IOException, SQLException {
		List<AgentReportResponseDTO> aReportResponseDTOs = new ArrayList<>();
		byte[] buf = null;

		HttpHeaders headers = new HttpHeaders();
		try {
			String startDate = null;
			String endDate = null;
			String SupportType = agentReportRequestDto.getType().replaceAll("\\s", "");

			String st_edDates = dateRange.getDateRanges(agentReportRequestDto.getDateRange(),
					agentReportRequestDto.getStartDate(), agentReportRequestDto.getEndDate());
			String arr[] = st_edDates.split("###");
			startDate = arr[0];
			endDate = arr[1];

			startDate = startDate.replace("/", "-");
			endDate = endDate.replace("/", "-");

			log.info("Start Date before calling DB:" + startDate);
			log.info("End Date before calling DB:" + endDate);

			Connection tjxConnection = null;
			CallableStatement callableStatement = null;

			Properties properties = new Properties();

			try {

				File a_config = ResourceUtils.getFile("classpath:config.properties");
				properties.load(new FileInputStream(a_config.toPath().toString()));

			} catch (IOException e) {
				e.printStackTrace();
				log.error("Excel - error while reading the calltransactionreport storedprocedure");
			}

			try {

				tjxConnection = dbConnection.getConnection();
				String callTransactionReport = properties.getProperty("CallTransactionReport");
				callableStatement = tjxConnection.prepareCall("{ call " + callTransactionReport + "(?,?,?) }");

				// callableStatement = tjxConnection.prepareCall("{ call " +
				// callTransactionReport + "(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				aReportResponseDTOs = agentRequestDtos(resultset);

				resultset.close();
			} catch (Exception e) {
				log.error("Error while getting call transaction report records from database in excel : " + e);
				log.error("stack strace : " + Arrays.toString(e.getStackTrace()));
				return null;
			} finally {
				if (callableStatement != null) {
					callableStatement.close();
				}
				if (tjxConnection != null) {
					tjxConnection.close();
				}
			}

			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				XSSFSheet sheet = workbook.createSheet("CALL TRANSACTION REPORT");

				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setBold(true);
				style.setFont(font);
				style.setAlignment(HorizontalAlignment.CENTER);

				Row headerRow = sheet.createRow(0);
				Cell headerCell = headerRow.createCell(0);
				headerCell.setCellValue("CALL TRANSACTION REPORT");

				headerCell.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

				Row headerRow1 = sheet.createRow(1);
				Cell headerCell1 = headerRow1.createCell(0);
				headerCell1.setCellValue("From date : " + startDate + "  To date : " + endDate);
				headerCell1.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

				Row columnRow = sheet.createRow(3);
				Cell headerCells = columnRow.createCell(0);
				headerCells.setCellValue("NAME");
				sheet.setColumnWidth(0, 5000);
				headerCells.setCellStyle(style);

				Cell headerCellh12 = columnRow.createCell(1);
				headerCellh12.setCellValue("CONTACT");
				sheet.setColumnWidth(1, 5000);
				headerCellh12.setCellStyle(style);

				Cell headerCellh1 = columnRow.createCell(2);
				headerCellh1.setCellValue("SECTION");
				sheet.setColumnWidth(2, 5000);
				headerCellh1.setCellStyle(style);

				Cell headerCellh2 = columnRow.createCell(3);
				headerCellh2.setCellValue("OFFERED");
				sheet.setColumnWidth(3, 5000);
				headerCellh2.setCellStyle(style);

				Cell headerCellh3 = columnRow.createCell(4);
				headerCellh3.setCellValue("ANSWERED");
				sheet.setColumnWidth(4, 5000);
				headerCellh3.setCellStyle(style);

				Cell headerCells1 = columnRow.createCell(5);
				headerCells1.setCellValue("ABANDONED");
				sheet.setColumnWidth(5, 5000);
				headerCells1.setCellStyle(style);

				Cell headerCells4 = columnRow.createCell(6);
				headerCells4.setCellValue("% ANSWERED");
				sheet.setColumnWidth(6, 5000);
				headerCells4.setCellStyle(style);

				int rowCount = 4;
				Row row = sheet.createRow(rowCount++);

				int j = 4;
				for (int i = 0; i < aReportResponseDTOs.size(); i++) {
					row = sheet.createRow(j++);

					Cell cell = row.createCell(0);
					cell.setCellValue(aReportResponseDTOs.get(i).getName());

					Cell cell0 = row.createCell(1);
					cell0.setCellValue(aReportResponseDTOs.get(i).getContact());

					Cell cell1 = row.createCell(2);
					cell1.setCellValue(aReportResponseDTOs.get(i).getType());

					Cell cell2 = row.createCell(3);
					cell2.setCellValue(aReportResponseDTOs.get(i).getOffered());

					Cell cell3 = row.createCell(4);
					cell3.setCellValue(aReportResponseDTOs.get(i).getAnswered());

					Cell cell4 = row.createCell(5);
					cell4.setCellValue(aReportResponseDTOs.get(i).getAbandoned());

					Cell cell5 = row.createCell(6);
					cell5.setCellValue(aReportResponseDTOs.get(i).getPerAnswered());

				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					workbook.write(baos);
				} finally {
					baos.close();
				}

				buf = baos.toByteArray();
			}
			headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "force-download"));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.xlsx");
		} catch (Exception e) {
			log.error("Error while downloading Call Transaction Report excel file  : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}

		return new HttpEntity<>(new ByteArrayResource(buf), headers);

	}

	@PostMapping("/agentReport/pdf")
	public HttpEntity<ByteArrayResource> downloadPdf(@Valid @RequestBody AgentReportRequestDTO agentReportRequestDto) {

		Document document = new Document(PageSize.A2, 10f, 10f, 10f, 0f);

		byte[] buf = null;
		List<List<String>> arrlist = new ArrayList<>();
		List<String> list = new ArrayList<>();

		HttpHeaders headers = new HttpHeaders();
		try {
			String startDate = null;
			String endDate = null;
			String SupportType = agentReportRequestDto.getType().replaceAll("\\s", "");

			String st_edDates = dateRange.getDateRanges(agentReportRequestDto.getDateRange(),
					agentReportRequestDto.getStartDate(), agentReportRequestDto.getEndDate());
			String arr[] = st_edDates.split("###");
			startDate = arr[0];
			endDate = arr[1];

			startDate = startDate.replace("/", "-");
			endDate = endDate.replace("/", "-");

			log.info("Start Date before calling DB:" + startDate);
			log.info("End Date before calling DB:" + endDate);

			Connection tjxConnection = null;
			CallableStatement callableStatement = null;

			Properties properties = new Properties();

			try {

				File a_config = ResourceUtils.getFile("classpath:config.properties");
				properties.load(new FileInputStream(a_config.toPath().toString()));
				log.info("PDF-calltransactionreport-property file is successfully loaded");

			} catch (IOException e) {
				e.printStackTrace();
				log.error("PDF - error while reading the calltransactionreport storedprocedure");
			}

			try {

				tjxConnection = dbConnection.getConnection();
				String callTransactionReport = properties.getProperty("CallTransactionReport");
				callableStatement = tjxConnection.prepareCall("{ call " + callTransactionReport + "(?,?,?) }");

				// callableStatement = tjxConnection.prepareCall("{ call
				// DEWA_AGENT_REPORT(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				while (resultset.next()) {

					list = new ArrayList<>();
					String name = resultset.getString("NAME");
					String conatct = resultset.getString("CONTACT");
					String type = resultset.getString("TYPE");
					String offered = resultset.getString("OFFERED");
					String answered = resultset.getString("ANSWERED");
					String abandoned = resultset.getString("ABANDONED");
					String perAnswered = resultset.getString("PER_ANSWERED");

					list.add(name);
					list.add(conatct);
					list.add(type);
					list.add(offered);
					list.add(answered);
					list.add(abandoned);
					list.add(perAnswered);
					arrlist.add(list);

				}

				resultset.close();
			} catch (Exception e) {
				log.error("Error while getting call transaction report records from database in pdf : " + e);
				log.error("stack strace : " + Arrays.toString(e.getStackTrace()));
				return null;
			} finally {
				if (callableStatement != null) {
					callableStatement.close();
				}
				if (tjxConnection != null) {
					tjxConnection.close();
				}
			}

			com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.COURIER, 7);
			titleFont.setColor(BaseColor.WHITE);
			com.itextpdf.text.Font bodyFont = FontFactory.getFont(FontFactory.COURIER, 6);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			@SuppressWarnings("unused")
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			document.open();
			PdfPTable table = new PdfPTable(7); // 11 columns. table.setWidthPercentage(100); // Width 100%
			float[] columnWidths = { 4f, 4f, 4f, 4f, 4f, 4f, 4f };
			table.setWidths(columnWidths);

			PdfPCell header = new PdfPCell(new Paragraph("CALL TRANSACTION REPORT"));
			header.setPaddingLeft(20);
			header.setColspan(20);
			header.setHorizontalAlignment(Element.ALIGN_CENTER);
			header.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(header);

			PdfPCell fromToDate = new PdfPCell(new Paragraph("Start Date: " + startDate + " End Date: " + endDate));
			fromToDate.setPaddingLeft(20);
			fromToDate.setColspan(20);
			fromToDate.setHorizontalAlignment(Element.ALIGN_CENTER);
			fromToDate.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(fromToDate);

			PdfPCell cell = new PdfPCell(new Paragraph("Name", titleFont));
			cell.setBackgroundColor(BaseColor.DARK_GRAY);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell);

			PdfPCell cell1 = new PdfPCell(new Paragraph("Contact", titleFont));
			cell1.setBackgroundColor(BaseColor.DARK_GRAY);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell1);

			PdfPCell cell2 = new PdfPCell(new Paragraph("Section", titleFont));
			cell2.setBackgroundColor(BaseColor.DARK_GRAY);
			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell2);

			PdfPCell cell3 = new PdfPCell(new Paragraph("Offered", titleFont));
			cell3.setBackgroundColor(BaseColor.DARK_GRAY);
			cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell3);

			PdfPCell cell4 = new PdfPCell(new Paragraph("Answered", titleFont));
			cell4.setBackgroundColor(BaseColor.DARK_GRAY);
			cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell4);

			PdfPCell cell5 = new PdfPCell(new Paragraph("Abandoned", titleFont));
			cell5.setBackgroundColor(BaseColor.DARK_GRAY);
			cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell5);

			PdfPCell cell6 = new PdfPCell(new Paragraph("% Answered", titleFont));
			cell6.setBackgroundColor(BaseColor.DARK_GRAY);
			cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell6);

			for (int i = 0; i < arrlist.size(); i++) {
				list = (List) arrlist.get(i);
				for (int j = 0; j < list.size(); j++) {
					PdfPCell body = new PdfPCell(new Paragraph((String) list.get(j), bodyFont));
					body.setPaddingLeft(10);
					body.setHorizontalAlignment(Element.ALIGN_CENTER);
					body.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(body);
				}
			}

			document.add(table);
			document.close();

			buf = baos.toByteArray();

			headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "force-download"));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.pdf");

		} catch (Exception e) {
			log.error("Error while downloading pdf file Call Transaction Report : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}
		return new HttpEntity<>(new ByteArrayResource(buf), headers);
	}

}
