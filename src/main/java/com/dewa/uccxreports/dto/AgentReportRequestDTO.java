package com.dewa.uccxreports.dto;

import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentReportRequestDTO {
	@NotNull
	private String startDate;
	
	@NotNull
	private String endDate;
	
	@NotNull
	private String dateRange;
	
	private String type;
}
