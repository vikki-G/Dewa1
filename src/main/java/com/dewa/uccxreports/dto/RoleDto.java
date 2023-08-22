package com.dewa.uccxreports.dto;

import lombok.Data;

@Data
public class RoleDto {
	
	private String roleName;
	private String status;
	private String featureIds;
	private String createdOn;
	private String createdBy;
	private String updatedBy;
	private String sectionsAccessible;

}
