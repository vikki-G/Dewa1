package com.dewa.uccxreports.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.dewa.uccxreports.entity.User;

public interface UserService {

	User save(User user);

	boolean existsByUserNameIgnoreCase(String userName);

	List<User> findAll();

	User findById(Integer userId);

	void deleteById(Integer userId);

	User findByUserNameIgnoreCaseAndStatusIgnoreCase(String userName, String status);



}
