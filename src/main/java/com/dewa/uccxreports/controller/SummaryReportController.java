package com.dewa.uccxreports.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewa.uccxreports.dto.AgentReportRequestDTO;
import com.dewa.uccxreports.dto.AgentSummaryReportRequestDTO;
import com.dewa.uccxreports.dto.AgentSummaryReportResponseDTO;
import com.dewa.uccxreports.dto.SectionDto;
import com.dewa.uccxreports.entity.DbConnection;
import com.dewa.uccxreports.util.DateRange;
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
@RequestMapping("/api/summaryReport")
public class SummaryReportController {

	private static final Logger log = LogManager.getLogger("SummaryReportController");

	@Autowired
	private DbConnection dbConnection;

	@Autowired
	private DateRange dateRange;

	@PostMapping("/getSummaryReport")
	public List<AgentSummaryReportResponseDTO> SummaryReport(
			@Valid @RequestBody AgentSummaryReportRequestDTO agentSummaryReportRequestDTO) {

		List<AgentSummaryReportResponseDTO> agentSummaryReportResponseDTO = new ArrayList<>();
		String startDate = null;
		String endDate = null;

		String start_endDates = dateRange.getDateRanges(agentSummaryReportRequestDTO.getDateRange(),
				agentSummaryReportRequestDTO.getStartDate(), agentSummaryReportRequestDTO.getEndDate());

		String supportType = agentSummaryReportRequestDTO.getType().replaceAll("\\s", "");
		String arr[] = start_endDates.split("###");
		startDate = arr[0];
		endDate = arr[1];

		startDate = startDate.replace("/", "-");
		endDate = endDate.replace("/", "-");

		log.info("Start Date before calling DB:" + startDate + "end date=> " + endDate);

		Connection connection = null;
		CallableStatement callableStatement = null;
		Properties properties = new Properties();

		try {

			File a_config = ResourceUtils.getFile("classpath:config.properties");
			properties.load(new FileInputStream(a_config.toPath().toString()));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while reading the SummaryReport storedprocedure");
		}
		try {

			connection = dbConnection.getConnection();
			log.info("Summary Report Connection created successfully for the DB");

			String getSummaryReport = properties.getProperty("GetSummaryReport");
			callableStatement = connection.prepareCall("{ call " + getSummaryReport + "(?,?,?) }");
			// callableStatement = connection.prepareCall("{ call DEWA_SUMMARY_REPORT(?,?,?)
			// }");

			callableStatement.setString(1, startDate);
			callableStatement.setString(2, endDate);
			callableStatement.setString(3, supportType);
			callableStatement.execute();

			ResultSet resultset = callableStatement.getResultSet();

			agentSummaryReportResponseDTO = agentSummaryReportResponseDTO(resultset);

			resultset.close();
		} catch (Exception e) {
			log.error("Error while getting summary report records from database : " + e);
			log.error("stack strace : " + Arrays.toString(e.getStackTrace()));
			return null;
		} finally {
			if (callableStatement != null) {
				try {
					callableStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return agentSummaryReportResponseDTO;
	}

	private List<AgentSummaryReportResponseDTO> agentSummaryReportResponseDTO(ResultSet resultset) throws Exception {
		List<AgentSummaryReportResponseDTO> agentSummaryReportResponseDTO = new ArrayList<>();
		AgentSummaryReportResponseDTO asrDto = null;
		Connection connection = null;
		CallableStatement callableStatement = null;
		Properties properties = new Properties();

		try {

			File a_config = ResourceUtils.getFile("classpath:config.properties");
			properties.load(new FileInputStream(a_config.toPath().toString()));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while reading the DetailedReport_GET_SECTIONS_IN_ORDER storedprocedure");
		}

		connection = dbConnection.getConnection();
		String getSectionsInOrder = properties.getProperty("GetSectionsInOrder");
		callableStatement = connection.prepareCall("{call " + getSectionsInOrder + "}");
		// callableStatement = connection.prepareCall("{ call " + getSectionsInOrder +
		// "(?,?,?) }");
		// callableStatement = connection.prepareCall("{call GET_SECTIONS_IN_ORDER}");

		callableStatement.execute();
		ResultSet resultSet2 = callableStatement.getResultSet();

		List<SectionDto> dto = new ArrayList<>();
		while (resultSet2.next()) {
			SectionDto sectionDto = new SectionDto();
			sectionDto.setSection(resultSet2.getString("SECTION"));
			dto.add(sectionDto);
		}
		while (resultset.next()) {

			asrDto = new AgentSummaryReportResponseDTO();
			asrDto.setSection(resultset.getString("SECTION"));
			asrDto.setManager(resultset.getString("MANAGER"));
			asrDto.setOffered(resultset.getString("OFFERED"));
			asrDto.setAnswered(resultset.getString("ANSWERED"));
			asrDto.setAbandoned(resultset.getString("ABANDONED"));
			asrDto.setPerAnswered(resultset.getString("PER_ANSWERED"));
			asrDto.setPerAbandoned(resultset.getString("PER_ABANDONED"));
			agentSummaryReportResponseDTO.add(asrDto);

		}

		List<AgentSummaryReportResponseDTO> finalAgentSummaryList = new ArrayList<>();
		for (SectionDto sectionDto : dto) {
			for (AgentSummaryReportResponseDTO agentDto : agentSummaryReportResponseDTO) {
				if (sectionDto.getSection().equalsIgnoreCase(agentDto.getSection())) {
					finalAgentSummaryList.add(agentDto);
				}
			}
		}

		return finalAgentSummaryList;

	}

	@PostMapping("/excel")
	public HttpEntity<ByteArrayResource> downloadExcel(@Valid @RequestBody AgentReportRequestDTO agentReportRequestDto)
			throws IOException, SQLException {
		List<AgentSummaryReportResponseDTO> aReportResponseDTOs = new ArrayList<>();
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
				log.error("Excel - Error while reading the SummaryReport storedprocedure");
			}
			try {

				tjxConnection = dbConnection.getConnection();

				String summaryReport = properties.getProperty("GetSummaryReport");
				callableStatement = tjxConnection.prepareCall("{ call " + summaryReport + "(?,?,?) }");
				// callableStatement = tjxConnection.prepareCall("{ call
				// DEWA_SUMMARY_REPORT(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				aReportResponseDTOs = agentSummaryReportResponseDTO(resultset);

				resultset.close();
			} catch (Exception e) {
				log.error("Error while getting summary report records from database in excel : " + e);
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
				XSSFSheet sheet = workbook.createSheet("IT SUMMARY REPORT");

				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setBold(true);
				style.setFont(font);
				style.setAlignment(HorizontalAlignment.CENTER);

				Row headerRow = sheet.createRow(0);
				Cell headerCell = headerRow.createCell(0);
				headerCell.setCellValue("SUMMARY REPORT");
				// headerCell.setCellValue("" + "");
				headerCell.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

				Row headerRow1 = sheet.createRow(1);
				Cell headerCell1 = headerRow1.createCell(0);
				headerCell1.setCellValue("From date : " + startDate + "  To date : " + endDate);
				headerCell1.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

				Row columnRow = sheet.createRow(3);
				Cell headerCells = columnRow.createCell(0);
				headerCells.setCellValue("Section");
				sheet.setColumnWidth(0, 5000);
				headerCells.setCellStyle(style);

				Cell headerCellh12 = columnRow.createCell(1);
				headerCellh12.setCellValue("Manager");
				sheet.setColumnWidth(1, 5000);
				headerCellh12.setCellStyle(style);

				Cell headerCellh1 = columnRow.createCell(2);
				headerCellh1.setCellValue("Offered");
				sheet.setColumnWidth(2, 5000);
				headerCellh1.setCellStyle(style);

				Cell headerCellh2 = columnRow.createCell(3);
				headerCellh2.setCellValue("Answered");
				sheet.setColumnWidth(3, 5000);
				headerCellh2.setCellStyle(style);

				Cell headerCellh3 = columnRow.createCell(4);
				headerCellh3.setCellValue("Abandoned");
				sheet.setColumnWidth(4, 5000);
				headerCellh3.setCellStyle(style);

				Cell headerCells1 = columnRow.createCell(5);
				headerCells1.setCellValue("% Answered");
				sheet.setColumnWidth(5, 5000);
				headerCells1.setCellStyle(style);

				Cell headerCells2 = columnRow.createCell(6);
				headerCells2.setCellValue("% Abandoned");
				sheet.setColumnWidth(6, 5000);
				headerCells2.setCellStyle(style);

				int rowCount = 4;
				Row row = sheet.createRow(rowCount++);

				int j = 4;
				for (int i = 0; i < aReportResponseDTOs.size(); i++) {
					row = sheet.createRow(j++);

					Cell cell = row.createCell(0);
					cell.setCellValue(aReportResponseDTOs.get(i).getSection());

					Cell cell0 = row.createCell(1);
					cell0.setCellValue(aReportResponseDTOs.get(i).getManager());

					Cell cell1 = row.createCell(2);
					cell1.setCellValue(aReportResponseDTOs.get(i).getOffered());

					Cell cell2 = row.createCell(3);
					cell2.setCellValue(aReportResponseDTOs.get(i).getAnswered());

					Cell cell3 = row.createCell(4);
					cell3.setCellValue(aReportResponseDTOs.get(i).getAbandoned());

					Cell cell4 = row.createCell(5);
					cell4.setCellValue(aReportResponseDTOs.get(i).getPerAnswered());

					Cell cell5 = row.createCell(6);
					cell5.setCellValue(aReportResponseDTOs.get(i).getPerAbandoned());

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
			log.error("Error while downloading summary report excel file  : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}

		return new HttpEntity<>(new ByteArrayResource(buf), headers);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })

	@PostMapping("/pdf")
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

			} catch (IOException e) {
				e.printStackTrace();
				log.error("PDF - Error while reading the SummaryReport storedprocedure");
			}
			try {

				tjxConnection = dbConnection.getConnection();
				String getSummaryReport = properties.getProperty("GetSummaryReport");
				callableStatement = tjxConnection.prepareCall("{ call " + getSummaryReport + "(?,?,?) }");

				// callableStatement = tjxConnection.prepareCall("{ call
				// DEWA_SUMMARY_REPORT(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				while (resultset.next()) {

					list = new ArrayList<>();
					String name = resultset.getString("SECTION");
					String conatct = resultset.getString("MANAGER");
					String type = resultset.getString("OFFERED");
					String offered = resultset.getString("ANSWERED");
					String answered = resultset.getString("ABANDONED");
					String abandoned = resultset.getString("PER_ANSWERED");
					String perAnswered = resultset.getString("PER_ABANDONED");

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
				log.error("Error while getting summary report records from database in pdf : " + e);
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

			PdfPCell header = new PdfPCell(new Paragraph("IT SUMMARY REPORT"));
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

			PdfPCell cell = new PdfPCell(new Paragraph("SECTION", titleFont));
			cell.setBackgroundColor(BaseColor.DARK_GRAY);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell);

			PdfPCell cell1 = new PdfPCell(new Paragraph("MANAGER", titleFont));
			cell1.setBackgroundColor(BaseColor.DARK_GRAY);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell1);

			PdfPCell cell2 = new PdfPCell(new Paragraph("OFFERED", titleFont));
			cell2.setBackgroundColor(BaseColor.DARK_GRAY);
			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell2);

			PdfPCell cell3 = new PdfPCell(new Paragraph("ANSWERED", titleFont));
			cell3.setBackgroundColor(BaseColor.DARK_GRAY);
			cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell3);

			PdfPCell cell4 = new PdfPCell(new Paragraph("ABANDONED", titleFont));
			cell4.setBackgroundColor(BaseColor.DARK_GRAY);
			cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell4);

			PdfPCell cell5 = new PdfPCell(new Paragraph("% ANSWERED", titleFont));
			cell5.setBackgroundColor(BaseColor.DARK_GRAY);
			cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell5);

			PdfPCell cell6 = new PdfPCell(new Paragraph("% ABANDONED", titleFont));
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
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.pdf");

		} catch (Exception e) {
			log.error("Error while downloading summary report pdf file  : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}
		return new HttpEntity<>(new ByteArrayResource(buf), headers);
	}

}
