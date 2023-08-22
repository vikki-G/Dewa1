package com.dewa.uccxreports.dto;

import lombok.Data;

@Data
public class AgentDetailReportResponseDTO {
	private String sessionId;
	private String date;
	private String time;
	private String callingNumber;
	private String answeringNumber;
	private String callDuration;
	private String holdDuration;
	private String callAnswered;
	private String unAnsweredCalls="";
	private String section;
}
