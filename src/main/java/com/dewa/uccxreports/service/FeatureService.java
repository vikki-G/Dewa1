package com.dewa.uccxreports.service;

import java.util.List;

import com.dewa.uccxreports.entity.Feature;


public interface FeatureService {
	
	List<Feature> findAllByStatus(String status);
	
	Feature findById(Integer featureId);

}
