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

import com.dewa.uccxreports.dto.AgentDetailReportRequestDTO;
import com.dewa.uccxreports.dto.AgentDetailReportResponseDTO;
import com.dewa.uccxreports.dto.AgentReportRequestDTO;
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

@RequestMapping("/api/detailReport")
public class DetailReportController {

	private static final Logger log = LogManager.getLogger("DetailReportController");
	@Autowired
	private DateRange dateRange;

	@Autowired
	private DbConnection dbConnection;

	@PostMapping("/getDetailReport")
	public List<AgentDetailReportResponseDTO> getTjxData(
			@Valid @RequestBody AgentDetailReportRequestDTO agentRequestDto) throws SQLException {

		// log.info("agentRequestDto : " + agentRequestDto);
		List<AgentDetailReportResponseDTO> agentRequestDtos = new ArrayList<>();
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

		log.info(" Start Date before calling DB:" + startDate + "end date=> " + endDate);

		Connection tjxConnection = null;
		CallableStatement callableStatement = null;

		Properties properties = new Properties();

		try {

			File a_config = ResourceUtils.getFile("classpath:config.properties");
			properties.load(new FileInputStream(a_config.toPath().toString()));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while reading the DetailedReport storedprocedure");
		}
		try {

			tjxConnection = dbConnection.getConnection();
			log.info("Detailed report Connection created successfully for the DB");

			String getDetailedReport = properties.getProperty("GetDetailedReport");
			callableStatement = tjxConnection.prepareCall("{ call " + getDetailedReport + "(?,?,?) }");
			// callableStatement = tjxConnection.prepareCall("{ call
			// DEWA_DETAILED_REPORT(?,?,?) }");

			callableStatement.setString(1, startDate);
			callableStatement.setString(2, endDate);
			callableStatement.setString(3, SupportType);
			callableStatement.execute();

			ResultSet resultset = callableStatement.getResultSet();

			agentRequestDtos = agentRequestDtos(resultset);

			resultset.close();
		} catch (Exception e) {
			log.error("Error while getting detailed report records from database : " + e);
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

	private List<AgentDetailReportResponseDTO> agentRequestDtos(ResultSet resultset) throws SQLException {
		List<AgentDetailReportResponseDTO> agentResponseDtos = new ArrayList<>();
		AgentDetailReportResponseDTO tjxDto = null;
		while (resultset.next()) {

			tjxDto = new AgentDetailReportResponseDTO();
			tjxDto.setSessionId(resultset.getString("SessionID"));
			tjxDto.setDate(resultset.getString("Date"));
			tjxDto.setTime(resultset.getString("Time"));
			tjxDto.setCallingNumber(resultset.getString("CallingNumber"));
			tjxDto.setAnsweringNumber(resultset.getString("AnsweringNumber"));
			tjxDto.setCallDuration(resultset.getString("CallDuration"));
			tjxDto.setHoldDuration(resultset.getString("HoldDuration"));
			tjxDto.setCallAnswered(resultset.getString("CallAnswered"));
			tjxDto.setSection(resultset.getString("ExitLocation"));

			agentResponseDtos.add(tjxDto);
		}

		List<AgentDetailReportResponseDTO> finalAgentResponseDtos = new ArrayList<>();
		for (AgentDetailReportResponseDTO currentAgentDTO : agentResponseDtos) {
			if (finalAgentResponseDtos.isEmpty()) {
				if (currentAgentDTO.getCallAnswered().equalsIgnoreCase("No")) {
					currentAgentDTO.setUnAnsweredCalls(currentAgentDTO.getAnsweringNumber());
					currentAgentDTO.setAnsweringNumber("");
				}
				finalAgentResponseDtos.add(currentAgentDTO);
			} else {
				AgentDetailReportResponseDTO lastAgentDTO = finalAgentResponseDtos
						.get(finalAgentResponseDtos.size() - 1);
				if (currentAgentDTO.getSessionId().equals(lastAgentDTO.getSessionId())) {
					if (currentAgentDTO.getCallAnswered().equalsIgnoreCase("Yes")) {
						lastAgentDTO.setAnsweringNumber(currentAgentDTO.getAnsweringNumber());
					} else {
						if (lastAgentDTO.getUnAnsweredCalls() == null) {
							lastAgentDTO.setUnAnsweredCalls(currentAgentDTO.getAnsweringNumber());
						} else {
							lastAgentDTO.setUnAnsweredCalls(
									lastAgentDTO.getUnAnsweredCalls() + " - " + currentAgentDTO.getAnsweringNumber());
						}
					}
				} else {
					if (currentAgentDTO.getCallAnswered().equalsIgnoreCase("No")) {
						currentAgentDTO.setUnAnsweredCalls(currentAgentDTO.getAnsweringNumber());
						currentAgentDTO.setAnsweringNumber("");
					}
					finalAgentResponseDtos.add(currentAgentDTO);
				}
			}
		}

		return finalAgentResponseDtos;
	}

	@PostMapping("/excel")
	public HttpEntity<ByteArrayResource> downloadExcel(@Valid @RequestBody AgentReportRequestDTO agentReportRequestDto)
			throws IOException, SQLException {
		List<AgentDetailReportResponseDTO> aReportResponseDTOs = new ArrayList<>();
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
				log.error("Excel - Error while reading the DetailedReport storedprocedure");
			}
			try {

				tjxConnection = dbConnection.getConnection();
				String getDetailedReport = properties.getProperty("GetDetailedReport");
				callableStatement = tjxConnection.prepareCall("{ call " + getDetailedReport + "(?,?,?) }");

				// callableStatement = tjxConnection.prepareCall("{ call
				// DEWA_DETAILED_REPORT(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				aReportResponseDTOs = agentRequestDtos(resultset);

				resultset.close();
			} catch (Exception e) {
				log.error("Error while getting detailed report records from database in excel : " + e);
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
				XSSFSheet sheet = workbook.createSheet("DETAILED REPORT");

				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setBold(true);
				style.setFont(font);
				style.setAlignment(HorizontalAlignment.CENTER);

				Row headerRow = sheet.createRow(0);
				Cell headerCell = headerRow.createCell(0);
				headerCell.setCellValue("DETAILED REPORT");

				headerCell.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

				Row headerRow1 = sheet.createRow(1);
				Cell headerCell1 = headerRow1.createCell(0);
				headerCell1.setCellValue("From date : " + startDate + "  To date : " + endDate);
				headerCell1.setCellStyle(style);
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

				Row columnRow = sheet.createRow(3);
				Cell headerCells = columnRow.createCell(0);
				headerCells.setCellValue("DATE");
				sheet.setColumnWidth(0, 5000);
				headerCells.setCellStyle(style);

				Cell headerCellh12 = columnRow.createCell(1);
				headerCellh12.setCellValue("TIME");
				sheet.setColumnWidth(1, 5000);
				headerCellh12.setCellStyle(style);

				Cell headerCellh1 = columnRow.createCell(2);
				headerCellh1.setCellValue("CALLING NUMBER");
				sheet.setColumnWidth(2, 5000);
				headerCellh1.setCellStyle(style);

				Cell headerCellh2 = columnRow.createCell(3);
				headerCellh2.setCellValue("ANSWERING NUMBER");
				sheet.setColumnWidth(3, 5000);
				headerCellh2.setCellStyle(style);

				Cell headerCellh3 = columnRow.createCell(4);
				headerCellh3.setCellValue("CALL DURATION");
				sheet.setColumnWidth(4, 5000);
				headerCellh3.setCellStyle(style);

				Cell headerCells1 = columnRow.createCell(5);
				headerCells1.setCellValue("HOLD DURATION");
				sheet.setColumnWidth(5, 5000);
				headerCells1.setCellStyle(style);

				Cell headerCells4 = columnRow.createCell(6);
				headerCells4.setCellValue("UNANSWERED CALLS");
				sheet.setColumnWidth(6, 5000);
				headerCells4.setCellStyle(style);

				int rowCount = 4;
				Row row = sheet.createRow(rowCount++);

				int j = 4;
				for (int i = 0; i < aReportResponseDTOs.size(); i++) {
					row = sheet.createRow(j++);

					Cell cell = row.createCell(0);
					cell.setCellValue(aReportResponseDTOs.get(i).getDate());

					Cell cell0 = row.createCell(1);
					cell0.setCellValue(aReportResponseDTOs.get(i).getTime());

					Cell cell1 = row.createCell(2);
					cell1.setCellValue(aReportResponseDTOs.get(i).getCallingNumber());

					Cell cell2 = row.createCell(3);
					cell2.setCellValue(aReportResponseDTOs.get(i).getAnsweringNumber());

					Cell cell3 = row.createCell(4);
					cell3.setCellValue(aReportResponseDTOs.get(i).getCallDuration());

					Cell cell4 = row.createCell(5);
					cell4.setCellValue(aReportResponseDTOs.get(i).getHoldDuration());

					Cell cell5 = row.createCell(6);
					cell5.setCellValue(aReportResponseDTOs.get(i).getUnAnsweredCalls());

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
			log.error("Error while downloading detailed report excel file  : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}

		return new HttpEntity<>(new ByteArrayResource(buf), headers);

	}

	@PostMapping("/pdf")
	public HttpEntity<ByteArrayResource> downloadPdf(@Valid @RequestBody AgentReportRequestDTO agentReportRequestDto) {

		Document document = new Document(PageSize.A2, 10f, 10f, 10f, 0f);

		byte[] buf = null;

		List<String> list = new ArrayList<>();

		List<AgentDetailReportResponseDTO> agentDtos = new ArrayList<>();
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
				log.error("PDF - Error while reading the DetailedReport storedprocedure");
			}
			try {

				tjxConnection = dbConnection.getConnection();
				String getDetailedReport = properties.getProperty("GetDetailedReport");
				callableStatement = tjxConnection.prepareCall("{ call " + getDetailedReport + "(?,?,?) }");

				// callableStatement = tjxConnection.prepareCall("{ call
				// DEWA_DETAILED_REPORT(?,?,?) }");

				callableStatement.setString(1, startDate);
				callableStatement.setString(2, endDate);
				callableStatement.setString(3, SupportType);
				callableStatement.execute();

				ResultSet resultset = callableStatement.getResultSet();
				agentDtos = agentRequestDtos(resultset);

				resultset.close();
			} catch (Exception e) {
				log.error("Error while getting detailed report records from database in pdf : " + e);
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

			PdfPCell header = new PdfPCell(new Paragraph("DETAILED REPORT"));
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

			PdfPCell cell = new PdfPCell(new Paragraph("DATE", titleFont));
			cell.setBackgroundColor(BaseColor.DARK_GRAY);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell);

			PdfPCell cell1 = new PdfPCell(new Paragraph("TIME", titleFont));
			cell1.setBackgroundColor(BaseColor.DARK_GRAY);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell1);

			PdfPCell cell2 = new PdfPCell(new Paragraph("CALLING NUMBER", titleFont));
			cell2.setBackgroundColor(BaseColor.DARK_GRAY);
			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell2);

			PdfPCell cell3 = new PdfPCell(new Paragraph("ANSWERING NUMBER", titleFont));
			cell3.setBackgroundColor(BaseColor.DARK_GRAY);
			cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell3);

			PdfPCell cell4 = new PdfPCell(new Paragraph("CALL DURATION", titleFont));
			cell4.setBackgroundColor(BaseColor.DARK_GRAY);
			cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell4);

			PdfPCell cell5 = new PdfPCell(new Paragraph("HOLD DURATION", titleFont));
			cell5.setBackgroundColor(BaseColor.DARK_GRAY);
			cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell5);

			PdfPCell cell6 = new PdfPCell(new Paragraph("UNANSWERED CALLS", titleFont));
			cell6.setBackgroundColor(BaseColor.DARK_GRAY);
			cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.addCell(cell6);

			for (int i = 0; i < agentDtos.size(); i++) {

				AgentDetailReportResponseDTO agentDto = agentDtos.get(i);

				list.add(agentDto.getDate());
				list.add(agentDto.getTime());
				list.add(agentDto.getCallingNumber());
				list.add(agentDto.getAnsweringNumber());
				list.add(agentDto.getCallDuration());
				list.add(agentDto.getHoldDuration());
				list.add(agentDto.getUnAnsweredCalls());

			}

			for (int j = 0; j < list.size(); j++) {
				PdfPCell body = new PdfPCell(new Paragraph((String) list.get(j), bodyFont));
				body.setPaddingLeft(10);
				body.setHorizontalAlignment(Element.ALIGN_CENTER);
				body.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(body);
			}
			document.add(table);
			document.close();

			buf = baos.toByteArray();

			headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "force-download"));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.pdf");

		} catch (Exception e) {
			log.error("Error while downloading pdf file detailed report : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return null;
		}
		return new HttpEntity<>(new ByteArrayResource(buf), headers);
	}

}
