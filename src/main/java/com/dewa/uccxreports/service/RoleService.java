package com.dewa.uccxreports.service;

import java.util.List;

import com.dewa.uccxreports.entity.Role;

public interface RoleService {
	
	List<Role> getRolesByStatus(String status);
	
	void deleteById(Integer roleId);

	Role findById(Integer roleId);

	Role save(Role role);
	
	boolean existsByRoleNameIgnoreCase(String roleName);
	
	List<Role> getAllRoles();

	
}
