package com.dewa.uccxreports.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewa.uccxreports.dto.RoleDto;
import com.dewa.uccxreports.entity.DbConnection;
import com.dewa.uccxreports.entity.Feature;
import com.dewa.uccxreports.entity.Role;
import com.dewa.uccxreports.entity.Section;
import com.dewa.uccxreports.service.FeatureService;
import com.dewa.uccxreports.service.RoleService;
import com.dewa.uccxreports.util.EntityConstants.Status;
import com.dewa.uccxreports.util.Response;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/roles")
public class RoleController {

	private static final Logger log = LogManager.getLogger("RoleController");

	@Autowired
	private Environment env;

	@Autowired
	private RoleService roleService;

	@Autowired
	private FeatureService featureService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private DbConnection dbConnection;

	@PostMapping("/save")
	public Response<Role> createrole(@RequestBody RoleDto roleDto) {

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		Role role = modelMapper.map(roleDto, Role.class);
		Set<Feature> features = new HashSet<>();
		if (roleService.existsByRoleNameIgnoreCase(roleDto.getRoleName())) {
			return new Response<Role>(400, false, HttpStatus.BAD_REQUEST, "RoleName already In Use").setResponse(null);
		}

		String[] featureIds = roleDto.getFeatureIds().split(",");
		System.out.println(featureIds);
		for (String i : featureIds) {
			features.add(featureService.findById(Integer.parseInt(i)));
			//System.out.println(features);
		}

		try {

			role.setCreatedOn(LocalDateTime.now().format(format));
			role.setUpdatedOn(LocalDateTime.now().format(format));
			role.setFeatures(features);
			role.setSectionsAccessible(saveRole(roleDto.getSectionsAccessible()));
			role = roleService.save(role);
			log.info("Role has been created -->"+role);
			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + role);
		} catch (Exception e) {
			log.error("Error while saving role : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<Role>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating role")
					.setResponse(null);
		}
		return new Response<Role>(200, true, HttpStatus.OK, "Role has been created").setResponse(role);
	}

	@GetMapping("/list")
	public Response<List<Role>> getAllRoles() {
		List<Role> roles = new ArrayList<>();
		try {
			roles = roleService.getAllRoles();

			for (Role role : roles) {
				role.setSectionsAccessible(getTypes(role.getSectionsAccessible()));
			}
			log.info("role has been retrived");
		} catch (Exception e) {
			log.error("Error while listing role : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<List<Role>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting roles")
					.setResponse(null);
		}
		return new Response<List<Role>>(200, true, HttpStatus.OK, roles.size() + " role has been retrived")
				.setResponse(roles);
	}

	@GetMapping("/listByStatus")
	public Response<List<Role>> getRoles() {
		List<Role> roles = new ArrayList<>();
		try {
			roles = roleService.getRolesByStatus(Status.ACTIVE.toString());
			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + roles);
		} catch (Exception e) {
			log.error("Error while listing role by status : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<List<Role>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting roles")
					.setResponse(null);
		}
		return new Response<List<Role>>(200, true, HttpStatus.OK, roles.size() + " role has been retrived")
				.setResponse(roles);
	}

	@DeleteMapping("/delete/{roleId}")
	public Response<String> deleteUserById(@PathVariable Integer roleId) {
		try {

			Role role = null;
			try {
				role = roleService.findById(roleId);
			} catch (Exception e) {
				return new Response<String>(500, false, HttpStatus.NOT_FOUND,
						"No role found with the roleId : " + roleId).setResponse(null);
			}
			roleService.deleteById(roleId);
			log.info("Role has been deleted "+role);
		} catch (Exception e) {
			log.error("Error while delete by role id : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<String>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting role")
					.setResponse(null);
		}
		return new Response<String>(200, true, HttpStatus.OK, "Role has been deleted").setResponse(null);
	}

	@PutMapping("/update/{roleId}")
	public Response<Role> updaterole(@PathVariable Integer roleId, @RequestBody RoleDto roleDto) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// Role role = null;

		Role role = modelMapper.map(roleDto, Role.class);

		Set<Feature> features = new HashSet<>();
		try {
			String[] featureIds = roleDto.getFeatureIds().split(",");
			for (String i : featureIds) {
				features.add(featureService.findById(Integer.parseInt(i)));
			}
			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + features);
			if (roleDto != null) {
				Role existingRole = roleService.findById(roleId);
				if (existingRole != null) {

					role.setId(existingRole.getId());
					role.setUpdatedOn(LocalDateTime.now().format(format));
					role.setFeatures(features);
					role.setSectionsAccessible(saveRole(roleDto.getSectionsAccessible()));
					role.setCreatedOn(existingRole.getCreatedOn());
					role.setCreatedBy(existingRole.getCreatedBy());
					role = roleService.save(role);
					log.info("Role has been updated");
					// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + role);
				}

			}
		} catch (Exception e) {
			log.error("Error while updating role : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<Role>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating role")
					.setResponse(null);
		}
		return new Response<Role>(200, true, HttpStatus.OK, "Role has been updated").setResponse(role);

	}

	@GetMapping("/typelist")
	public Response<List<Section>> getTypes() {
		List<Section> supportTypes = new ArrayList<>();
		Section supportType = null;

		String supportQuery = env.getProperty("getTypes");

		Connection pom_con = null;
		try {

			pom_con = dbConnection.getConnection();
			Statement stmt = pom_con.createStatement();
			ResultSet rs = stmt.executeQuery(supportQuery);
			while (rs.next()) {
				supportType = new Section();
				supportType.setId(rs.getString("id"));
				supportType.setSection(rs.getString("SECTION"));
				supportTypes.add(supportType);
			}

			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + supportTypes);

		} catch (Exception e) {
			log.error("Error while getting supportType : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<List<Section>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while getting supportType").setResponse(null);
		}
		return new Response<List<Section>>(200, true, HttpStatus.OK,
				supportTypes.size() + " supportType has been retrieved").setResponse(supportTypes);
	}

	String getValue = "";

	public String getTypes(String sectionIds) {

		CallableStatement callableStatement = null;
		Connection pom_con = null;
		String sections = "";
		try {

			pom_con = dbConnection.getConnection();
			LinkedHashMap<String, String> sectionDetails = new LinkedHashMap<>();
			callableStatement = pom_con.prepareCall("{ call getSections() }");
			ResultSet rs = callableStatement.executeQuery();

			while (rs.next()) {
				sectionDetails.put(rs.getString("id"), rs.getString("SECTION"));
			}

			String[] ids = sectionIds.split(",");

			// System.out.println("ids---> " + ids.toString());

			for (int i = 0; i < ids.length; i++) {
				sections = sections + sectionDetails.get(ids[i]) + ",";

			}

			if (!sections.isEmpty()) {
				sections = sections.substring(0, sections.length() - 1);
			}

			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + sections);
		} catch (Exception e) {
			log.error("Error while getting supportType : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));

		}
		return sections;
	}

	public String saveRole(String role) {

		CallableStatement callableStatement = null;
		Connection pom_con = null;
		String sections = "";

		try {

			pom_con = dbConnection.getConnection();

			LinkedHashMap<String, String> sectionDetails = new LinkedHashMap<>();

			callableStatement = pom_con.prepareCall("{ call getSections() }");
			ResultSet rs = callableStatement.executeQuery();
			String reducedSpaces = role.replaceAll("\\s+", "");
			String[] ids = reducedSpaces.split(",");
			while (rs.next()) {
				sectionDetails.put(rs.getString("id"), rs.getString("SECTION"));
			}

			for (int i = 0; i < ids.length; i++) {

				boolean found = false;

				for (Map.Entry<String, String> entry : sectionDetails.entrySet()) {
					if (ids[i].equals(entry.getValue())) {
						sections += entry.getKey() + ",";
						found = true;
						break;
					}
				}

				if (!found) {
					return null;
				}
			}

			if (!sections.isEmpty()) {
				sections = sections.substring(0, sections.length() - 1);
			}
		} catch (Exception e) {
			log.error("Error while getting supportType: " + e);
			log.error("stack trace: " + Arrays.toString(e.getStackTrace()));
		}

		System.out.println(sections);
		return sections;
	}

}
