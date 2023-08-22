package com.dewa.uccxreports.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data

@Table(name = "SECTIONS")
public class Section {

	@Id

	@Column(name = "id", nullable = false)
	private String id;

	@Column(name = "SECTION", nullable = true)
	private String section;

}
