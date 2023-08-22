package com.dewa.uccxreports.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
@Data
@Entity
@Table(name="roles")
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id" , nullable=false)
	private Integer id;
	
	@Column(name="roleName" , nullable=false)
	private String roleName;
	
	@Column(name="status" , nullable=false)
	private String status;

	
	@Column(name="createdOn" , nullable=true)
	private String createdOn;
	
	@Column(name="updatedOn" , nullable=true)
	private String updatedOn;
	
	@Column(name="createdBy" , nullable=true)
	private String createdBy;
	
	@Column(name="updatedBy" , nullable=true)
	private String updatedBy;
	
	@Column(name="sectionsAccessible" , nullable=true)
	private String sectionsAccessible;
	
	@ManyToMany(targetEntity = Feature.class,fetch = FetchType.EAGER)
	@JoinTable(name="role_features",
		joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "feature_id", referencedColumnName = "id"))
	    private Set<Feature> features = new HashSet<>();
	

 
}
