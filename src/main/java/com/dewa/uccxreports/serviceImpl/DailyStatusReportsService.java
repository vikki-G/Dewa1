package com.dewa.uccxreports.serviceImpl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dewa.uccxreports.dto.AgentReportRequestDTO;
import com.dewa.uccxreports.entity.DbConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DailyStatusReportsService {

	private static final Logger log = LogManager.getLogger("DailyStatusReportsService");
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private Environment env;

	@Autowired
	private DbConnection dbConnection;

	@Value("${calltransactionreportUrl}")
	private String ctrreport;

	@Value("${summaryreportUrl}")
	private String srreport;

	@Value("${detailedreportUrl}")
	private String drreport;

	private String ctrUrl;
	private String srUrl;
	private String drUrl;

	@PostConstruct
	public void init() {
		ctrUrl = ctrreport;
		srUrl = srreport;
		drUrl = drreport;

	}

	LocalDate currentDate = LocalDate.now();
	int numberOfDays = 1; // Example: Previous day
	LocalDate previousDate = currentDate.minusDays(numberOfDays);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	String currentDateString = currentDate.format(formatter);
	String previousDateString = previousDate.format(formatter);

	private byte[] CTRexcelBytes;
	private byte[] SRexcelBytes;
	private byte[] DRexcelBytes;

	private String fileNameCTR;
	private String fileNameSR;
	private String fileNameDR;

	public void schedulingCallTransactionReport() throws Exception {

		LocalDate currentDate = LocalDate.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd yyyy");
		String formattedDate = currentDate.format(formatter);

		fileNameCTR = "CallTransactionReport " + formattedDate + ".xlsx";
		CTRexcelBytes = retrieveCTRExcelFile();
		sendMail(CTRexcelBytes);
	}

	public byte[] retrieveCTRExcelFile() throws Exception {
		AgentReportRequestDTO agentReportRequestDTO = new AgentReportRequestDTO();

		String query = env.getProperty("getTypes");

		List<String> types = new ArrayList<>();
		Connection connection = dbConnection.getConnection();

		PreparedStatement statement = connection.prepareStatement(query);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			String type = resultSet.getString("SECTION");

			types.add(type);
		}
		agentReportRequestDTO.setStartDate(previousDateString);
		agentReportRequestDTO.setEndDate(currentDateString);
		agentReportRequestDTO.setDateRange("custom");
		agentReportRequestDTO.setType(String.join(",", types));
		ObjectMapper objectMapper = new ObjectMapper();
		String reqBody;
		try {
			reqBody = objectMapper.writeValueAsString(agentReportRequestDTO);
		} catch (JsonProcessingException e) {
			log.error(" Error while Getting Requestbody retrieveCallTransactionReportExcelFile", e);
			return null;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(ctrUrl, HttpMethod.POST, entity, byte[].class);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody();
		} else {
			log.error(

					" Error while downloading CallTransactionReport Excel File. Status code: {}",
					responseEntity.getStatusCodeValue());
		}
		return null;
	}

	public void schedulingSummarReport() throws Exception {

		LocalDate currentDate = LocalDate.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd yyyy");
		String formattedDate = currentDate.format(formatter);

		fileNameSR = "SummaryReport " + formattedDate + ".xlsx";

		SRexcelBytes = retrieveSRExcelFile();
		sendMail(SRexcelBytes);

	}

	public byte[] retrieveSRExcelFile() throws Exception {
		AgentReportRequestDTO agentReportRequestDTO = new AgentReportRequestDTO();

		String query = env.getProperty("getTypes");

		List<String> types = new ArrayList<>();
		Connection connection = dbConnection.getConnection();

		PreparedStatement statement = connection.prepareStatement(query);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			String type = resultSet.getString("SECTION");

			types.add(type);
		}
		agentReportRequestDTO.setStartDate(previousDateString);
		agentReportRequestDTO.setEndDate(currentDateString);
		// agentReportRequestDTO.setStartDate("01/05/2023");
		// agentReportRequestDTO.setEndDate("14/07/2023");
		agentReportRequestDTO.setDateRange("custom");
		agentReportRequestDTO.setType(String.join(",", types));
		ObjectMapper objectMapper = new ObjectMapper();
		String reqBody;
		try {
			reqBody = objectMapper.writeValueAsString(agentReportRequestDTO);
		} catch (JsonProcessingException e) {
			log.error(" Error while converting AgentReportRequestDTO to JSON: {}", e);
			return null;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// String getSRUrl = readPropertyFile.loadWidgetScriptName("summaryreportUrl");
		HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(srUrl, HttpMethod.POST, entity, byte[].class);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody();
		} else {
			log.error(

					" Error while downloading SummaryReport Excel File. Status code: {}",
					responseEntity.getStatusCodeValue());
		}
		return null;
	}

	public void schedulingDetailedReport() throws Exception {

		LocalDate currentDate = LocalDate.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd yyyy");
		String formattedDate = currentDate.format(formatter);

		fileNameDR = "DetailedReport " + formattedDate + ".xlsx";

		DRexcelBytes = retrieveDRExcelFile();
		sendMail(DRexcelBytes);

	}

	public byte[] retrieveDRExcelFile() throws Exception {
		AgentReportRequestDTO agentReportRequestDTO = new AgentReportRequestDTO();

		String query = env.getProperty("getTypes");

		List<String> types = new ArrayList<>();
		Connection connection = dbConnection.getConnection();

		PreparedStatement statement = connection.prepareStatement(query);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			String type = resultSet.getString("SECTION");

			types.add(type);

		}

		agentReportRequestDTO.setStartDate(previousDateString);
		agentReportRequestDTO.setEndDate(currentDateString);
//		agentReportRequestDTO.setStartDate("17/07/2023");
//		 agentReportRequestDTO.setEndDate("18/07/2023");
		agentReportRequestDTO.setDateRange("custom");
		agentReportRequestDTO.setType(String.join(",", types));
		ObjectMapper objectMapper = new ObjectMapper();
		String reqBody;
		try {
			reqBody = objectMapper.writeValueAsString(agentReportRequestDTO);
		} catch (JsonProcessingException e) {
			log.error(" Error while converting AgentReportRequestDTO to JSON: {}", e);
			return null;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// String getDRUrl = readPropertyFile.loadWidgetScriptName("detailedreportUrl");
		HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(drUrl, HttpMethod.POST, entity, byte[].class);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody();
		} else {
			log.error(

					" Error while downloading DetailedReport Excel File. Status code: {}",
					responseEntity.getStatusCodeValue());
		}
		return null;
	}

//	public void schedulingMail() throws Exception {
//	    byte[] ctrReport = retrieveExcelFile(ctrUrl, "CallTransactionReport.xlsx");
//	    byte[] srReport = retrieveExcelFile(srUrl, "SummaryReport.xlsx");
//	    byte[] drReport = retrieveExcelFile(drUrl, "DetailedReport.xlsx");
//	    sendMail(ctrReport, srReport, drReport);
//	}

	public void sendMail(byte[] getReports) throws Exception {

		// Connection connection = null;
		CallableStatement callableStatement = null;

		String htmlTextMessage = "" + "<html>" + "<head><title>Daily Status Report</title>"
				+ "<style>.greenText{color:green;} p{font-size:14;}</style></head>" + "<body>"
				+ "<h2 style='color:red;'>Daily Status Report</h2>" + "<p> Hi <br>"
				+ "Please Find the below attached Reports <br>" + "Here is a summary of today's activities <br><br> "
				+ "<span class='greenText'>Thank you,</span><br><br>" + "Have a good day!<br>" + "</p>" + "</body>"
				+ "</html>";

		// String recepients = env.getProperty("email.recipients");

//
//		PreparedStatement statement = connection.prepareStatement(recepients);
//		ResultSet resultSet = statement.executeQuery();			

		// List<String> recipientList = Arrays.asList(recepients.split(","));
		if (CTRexcelBytes != null && SRexcelBytes != null && DRexcelBytes != null) {
			String from = env.getProperty("spring.mail.username");
			List<String> recepientsMail = new ArrayList<>();
			Connection connection = dbConnection.getConnection();
			// connection = dbConnection.getConnection();
			callableStatement = connection.prepareCall("{call recipientmail}");
			callableStatement.execute();
			ResultSet resultSet = callableStatement.getResultSet();

			while (resultSet.next()) {
				String mail = resultSet.getString("email");
				recepientsMail.add(mail);
			}
			System.out.println(recepientsMail);

			// for (String receipient : recipientList) {

			for (int i = 0; i < recepientsMail.size(); i++) {
				String currentValue = recepientsMail.get(i);
				System.out.println("Current Element: " + currentValue);
				try {
					MimeMessage message = emailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true);
					helper.setFrom(from);
					helper.setTo(currentValue);
					helper.setSubject("Daily Status Reports");
					helper.setText(htmlTextMessage, true);

					helper.addAttachment(fileNameCTR, new ByteArrayResource(CTRexcelBytes));
					helper.addAttachment(fileNameSR, new ByteArrayResource(SRexcelBytes));
					helper.addAttachment(fileNameDR, new ByteArrayResource(DRexcelBytes));

					emailSender.send(message);
					log.info(" Email sent successfully with the attached Excel file");
				} catch (MessagingException e) {
					log.error(" Error while sending email with attachment: {}", e);
				}
			}
//			for (String receipient : recepientsMail) {
//				try {
//					MimeMessage message = emailSender.createMimeMessage();
//					MimeMessageHelper helper = new MimeMessageHelper(message, true);
//					helper.setFrom(from);
//					helper.setTo(receipient);
//					helper.setSubject("Daily Status Reports");
//					helper.setText(htmlTextMessage, true);
//
//					helper.addAttachment(fileNameCTR, new ByteArrayResource(CTRexcelBytes));
//					helper.addAttachment(fileNameSR, new ByteArrayResource(SRexcelBytes));
//					helper.addAttachment(fileNameDR, new ByteArrayResource(DRexcelBytes));
//
//					emailSender.send(message);
//					log.info(CustomWidgetLog.getCurrentClassAndMethodName()
//							+ " Email sent successfully with the attached Excel file");
//				} catch (MessagingException e) {
//					log.error(CustomWidgetLog.getCurrentClassAndMethodName()
//							+ " Error while sending email with attachment: {}", e);
//				}
//			}
		} else if (CTRexcelBytes == null) {
			log.info("while retrieving CallTransaction Report Excel file");
		} else if (CTRexcelBytes != null) {
			log.info("while retrieving Summary Report Excel file");
		} else if (CTRexcelBytes != null && SRexcelBytes != null) {
			log.info("while retrieving Detailed Report Excel file");
		} else {

			log.error("Error while retrieving Excel file");
		}

	}

}
