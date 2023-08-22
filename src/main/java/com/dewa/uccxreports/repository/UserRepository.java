package com.dewa.uccxreports.repository;

import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dewa.uccxreports.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	User findByUserNameIgnoreCaseAndStatusIgnoreCase(String userName, String status);

	boolean existsByUserNameIgnoreCase(String userName);

	Page<User> findAll(Pageable pageable);

}
