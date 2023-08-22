package com.dewa.uccxreports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dewa.uccxreports.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Integer>{

	List<Feature> findAllByStatusIgnoreCase(String status);

}
