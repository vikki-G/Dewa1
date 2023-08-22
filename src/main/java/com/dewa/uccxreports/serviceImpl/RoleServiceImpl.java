package com.dewa.uccxreports.serviceImpl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dewa.uccxreports.entity.Role;
import com.dewa.uccxreports.repository.RoleRepository;
import com.dewa.uccxreports.service.RoleService;

import lombok.extern.log4j.Log4j;

@Service
@Transactional
@Log4j

public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public List<Role> getRolesByStatus(String status) {

		try {
			List<Role> role = roleRepository.findAllByStatusIgnoreCase(status);
			return role;
			
		} catch (Exception e) {
			log.error("Error While Getting Role");
		}
		return null;
	}

	@Override
	public void deleteById(Integer roleId) {
		try {
			roleRepository.deleteById(roleId);
		} catch (Exception e) {
			log.error("Error While Delete Role");
		}

	}

	@Override
	public Role findById(Integer roleId) {
		try {
			Role role = roleRepository.findById(roleId).get();
			
			return role;
		} catch (Exception e) {
			log.error("Error While Find The Role");
		}
		return null;
	}

	@Override
	public boolean existsByRoleNameIgnoreCase(String roleName) {
		return roleRepository.existsByRoleNameIgnoreCase(roleName);
	}

	@Override
	public List<Role> getAllRoles() {

		try {
			List<Role> roles = roleRepository.findAll();
			return roles;
		} catch (Exception e) {
			log.error("Error While Getting All The Roles");
		}
		return null;
	}

	@Override
	public Role save(Role role) {
		try {
			Role roles = roleRepository.save(role);
			
			return roles;
		} catch (Exception e) {
			log.error("Error While Save The Role");
		}
		return null;

	}

}
