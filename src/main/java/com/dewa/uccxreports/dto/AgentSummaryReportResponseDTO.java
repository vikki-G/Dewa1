package com.dewa.uccxreports.dto;

import lombok.Data;

@Data
public class AgentSummaryReportResponseDTO {

	private String section;
	private String manager;
	private String offered;
	private String answered;
	private String abandoned;
	private String perAnswered;
	private String perAbandoned;
}
