package com.dewa.uccxreports.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AgentSummaryReportRequestDTO {

	@NotNull
	private String startDate;
	
	@NotNull
	private String endDate;
	
	@NotNull
	private String dateRange;
	
	private String type;
}
