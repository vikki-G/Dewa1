package com.dewa.uccxreports.serviceImpl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dewa.uccxreports.entity.Feature;
import com.dewa.uccxreports.repository.FeatureRepository;
import com.dewa.uccxreports.service.FeatureService;
import com.sun.tools.sjavac.Log;

import lombok.extern.log4j.Log4j;

@Service
@Transactional
public class FeatureServiceImpl implements FeatureService {

	@Autowired
	private FeatureRepository featureRepository;

	@Override
	public List<Feature> findAllByStatus(String status) {
		try {
			List<Feature> feature = featureRepository.findAllByStatusIgnoreCase(status);

			return feature;
		} catch (Exception e) {
			Log.error("Error While Getting Feature");
		}
		return null;
	}

	@Override
	public Feature findById(Integer featureId) {
		try {
			Feature feature = featureRepository.findById(featureId).get();

			return feature;
		} catch (Exception e) {
			Log.error("Error While Getting Specific Feature");
		}

		return null;
	}

}
