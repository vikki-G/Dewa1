package com.dewa.uccxreports.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AgentDetailReportRequestDTO {
	
	
	@NotNull
	private String StartDate;
	@NotNull
	private String endDate;
	@NotNull
	private String dateRange;
	
	private String type;
	
	
}
