package com.dewa.uccxreports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dewa.uccxreports.entity.Role;

public interface RoleRepository extends JpaRepository<Role,Integer>{

	List<Role> findAllByStatusIgnoreCase(String status);

	boolean existsByRoleNameIgnoreCase(String roleName);

	Role save(Role role);

}
