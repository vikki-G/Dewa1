package com.dewa.uccxreports.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.dewa.uccxreports.dto.UserDto;
import com.dewa.uccxreports.entity.Feature;
import com.dewa.uccxreports.entity.Role;
import com.dewa.uccxreports.entity.User;
import com.dewa.uccxreports.repository.RoleRepository;
import com.dewa.uccxreports.service.UserService;
import com.dewa.uccxreports.util.Aes;
import com.dewa.uccxreports.util.EntityConstants.Status;
import com.dewa.uccxreports.util.Response;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final Logger log = LogManager.getLogger("UserController");

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private Aes aes;

	@Autowired
	private RoleController rolecontroller;

	@Autowired
	private ModelMapper modelMapper;

	// @Autowired
	// private DbConnection dbConnection;

	// @Autowired
	// private UserRepository userRepository;

	// @Autowired
	// private UserServiceImpl userServiceImpl;

	@GetMapping("/list")
	public Response<List<User>> getUsers() {
		List<User> users = new ArrayList<>();
		try {
			users = userService.findAll();
			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + users);
			for (User u : users) {
				u.setPassword(aes.decrypt(u.getPassword()));
			}
			// users.removeIf(
			// u -> (u.getUserName().equalsIgnoreCase("superadmin") ||
			// u.getStatus().equalsIgnoreCase("D")));
		} catch (Exception e) {
			log.error("Error while listing user : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<List<User>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting users")
					.setResponse(null);
		}
		return new Response<List<User>>(200, true, HttpStatus.OK, users.size() + " users has been retrived")
				.setResponse(users);
	}

//	@GetMapping("/list")
//	public List<User> fetchUsersInChunks(@RequestParam int pageNumber,@RequestParam int pageSize) {
//		List<User> allUsers = new ArrayList<>();
//		boolean hasMoreData = true;
//
//		while (hasMoreData) {
//			ResponseEntity<Response<List<User>>> responseEntity = fetchUsersPage(pageNumber, pageSize);
//
//			if (responseEntity.getStatusCode() == HttpStatus.OK) {
//				Response<List<User>> apiResponse = responseEntity.getBody();
//				List<User> users = apiResponse.getResponse();
//				allUsers.addAll(users);
//
//				// Check if there are more pages to fetch
//				if (users.size() == pageSize) {
//					pageNumber++;
//				} else {
//					hasMoreData = false;
//				}
//			} else {
//				// Handle error if needed
//				break;
//			}
//		}
//
//		return allUsers;
//	}
//
//	private ResponseEntity<Response<List<User>>> fetchUsersPage(int pageNumber, int pageSize) {
//		RestTemplate restTemplate = new RestTemplate();
//		ResponseEntity<Response<List<User>>> responseEntity = restTemplate.exchange(
//				"http://localhost:8089/api/users/list?pageNumber=" + pageNumber + "&pageSize=" + pageSize,
//				HttpMethod.GET, null, new ParameterizedTypeReference<Response<List<User>>>() {
//				});
//
//		return responseEntity;
//	}

//	@GetMapping("/list")
//	public ResponseEntity<Response<List<User>>> getAllUsersInChunks(@RequestParam(defaultValue = "0") int pageNumber,
//	                                                                @RequestParam(defaultValue = "5000") int pageSize) {
//
//	    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
//	    Page<User> userPage = userRepository.findAll(pageRequest);
//
//	    List<User> users = new ArrayList<>();
//	    for (User u : userPage) {
//	        u.setPassword(aes.decrypt(u.getPassword()));
//	        users.add(u);
//	    }
//
//	    Response<List<User>> apiResponse = new Response<>(200, true, HttpStatus.OK, users.size() + " users have been retrieved");
//	    apiResponse.setResponse(users);
//
//	    return ResponseEntity.ok()
//	            .header("X-Total-Count", String.valueOf(userPage.getTotalElements()))
//	            .header("X-Total-Pages", String.valueOf(userPage.getTotalPages()))
//	            .body(apiResponse);
//	}

//	@GetMapping("/list")
//	public Response<List<User>> getAllUsersInChunks(@RequestParam(defaultValue = "0") int pageNumber,
//			@RequestParam(defaultValue = "1000") int pageSize) {
//		// int pageNumber = 0;
//		// int pageSize = 100;
//		Page<User> userPage;
//		List<User> users = new ArrayList<>();
//
//			do {
//				PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
//				userPage = userRepository.findAll(pageRequest);
//				for (User u : userPage) {
//					u.setPassword(aes.decrypt(u.getPassword()));
//				}
//				users.addAll(userPage.getContent());
//			//	pageNumber++;
//			
//				if (pageRequest.isPaged()) {
//					// pageNumber++;
//					//Response<List<User>> loadData = loadAllHugeData();
//					break;
//				}
//			} while (userPage.hasNext());
//
//		return new Response<List<User>>(200, true, HttpStatus.OK, users.size() + " users has been retrived")
//				.setResponse(users);
//
//	}

//	public Response<List<User>> loadAllHugeData() {
//	    List<User> hugeData = new ArrayList<>();
//	    Page<User> userPage;
//	    int pageNumber = 0;
//	    int pageSize = 1000;
//
//	    do {
//	        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
//	        userPage = userRepository.findAll(pageRequest);
//
//	        for (User user : userPage) {
//	            user.setPassword(aes.decrypt(user.getPassword()));
//	        }
//
//	        hugeData.addAll(userPage.getContent());
//	        pageNumber++;
//
//	        // Continue fetching until there are no more pages (hasNext returns false)
//	    } while (userPage.hasNext());
//
//	    return new Response<List<User>>(200, true, HttpStatus.OK, hugeData.size() + " users have been retrieved")
//	            .setResponse(hugeData);
//	}

	@PostMapping("/save")
	public Response<User> saveUser(@RequestBody UserDto userDto) {

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		User user = modelMapper.map(userDto, User.class);
		if (userService.existsByUserNameIgnoreCase(userDto.getUserName())) {
			return new Response<User>(400, false, HttpStatus.BAD_REQUEST, "Username already In Use").setResponse(null);
		}
		try {
			if (userDto != null) {

				Role role = roleRepository.findById(userDto.getRoleId()).get();
				user.setRole(role);
				user.setPassword(aes.encrypt(userDto.getPassword()));
				user.setCreatedOn(LocalDateTime.now().format(format));
				user = userService.save(user);
				log.info("User has been created");
				// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + user);
			}
		} catch (Exception e) {
			log.error("Error while creating user : " + e);
			log.error("stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<User>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating user")
					.setResponse(null);
		}
		return new Response<User>(200, true, HttpStatus.OK, "User has been created").setResponse(user);

	}

//	@GetMapping("/list")
//	public Response<List<User>> getUsers() {
//		
//		try {
//		
//        	users = userService.findAll();
//			//log.info(CustomWidgetLog.getCurrentClassAndMethodName() + users);
//			/*for (User u : users) {
//				u.setPassword(aes.decrypt(u.getPassword()));
//			} */
//	//		users.removeIf(
//	//				u -> (u.getUserName().equalsIgnoreCase("superadmin") || u.getStatus().equalsIgnoreCase("D")));
//		} catch (
//
//		Exception e) {
//			log.error(CustomWidgetLog.getCurrentClassAndMethodName() + "Error while listing user : " + e);
//			log.error(CustomWidgetLog.getCurrentClassAndMethodName() + "stack trace : "
//					+ Arrays.toString(e.getStackTrace()));
//			return new Response<List<User>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting users")
//					.setResponse(null);
//		}
//		return new Response<List<User>>(200, true, HttpStatus.OK, users.size() + " users has been retrived")
//				.setResponse(users);
//	}

//	@GetMapping("/list")
//	public Object getUsers() throws Exception {
//		ArrayList<UserDto> users = new ArrayList<UserDto>();
//		ResultSet rs = null;
//		String query = "sp_widget_get_users_list";
//		CallableStatement callableStatement;
//		Connection connection = null;
//		try {
//			connection = dbConnection.getConnection();
//			Object response = new Object();
//			// callableStatement = connection.prepareCall("{call "+procedure_name+"(?)}");
//			callableStatement = connection.prepareCall("{call " + query + "}");
//
//			// String json = new ObjectMapper().writeValueAsString(request1);
//
//			// callableStatement.setString(1, request1.toString());
//			// callableStatement.setString(1, json.toString());
//			// callableStatement.setString(1, request1.toString());
//
//			rs = callableStatement.executeQuery();
//			System.out.println("Query executed successfully");
//			ObjectMapper objectMapper = new ObjectMapper();
//
//			while (rs.next()) {
//				response = rs.getObject("responseJson");
//				// response = rs.getString("responseJson");
//				System.out.println("response received");
//				System.out.println(response);
//				return response;
//			}
//			// User user = objectMapper.readValue(response.toString(), User.class);
//
//		} catch (
//
//		Exception e) {
//			log.error("Exception in wid call detail  : " + e.getMessage());
//			throw e;
//		}
	// ObjectMapper objectMapper = new ObjectMapper();
	// User user ;

//		try {
//			Connection connection = null;
//			connection = dbConnection.getConnection();
//			CallableStatement callableStatement = null;
//			String query = "getRoles";
//			// String query = "sp_widget_get_users_list";
//			callableStatement = connection.prepareCall("{call " + query + "}");
//			callableStatement.setFetchSize(50);
//			callableStatement.execute();
//			rs = callableStatement.getResultSet();
//			String sanitizedResponse = "";
//			// sanitizedResponse = JsonSanitizer.sanitize(rs.toString());
//			// String jsonString = objectMapper.writeValueAsString(new User());
//			ObjectMapper objectMapper = new ObjectMapper();
//			    System.out.println(rs.toString());
//				hugeData = objectMapper.readValue(rs.toString(), User.class);

	/*
	 * Object response = new Object(); callableStatement =
	 * connection.prepareCall("{call "+query+"(?)}"); rs
	 * =callableStatement.executeQuery();
	 * System.out.println("Query executed successfully");
	 */

//			while(rs.next()) 
//			{
//			ObjectMapper objectMapper = new ObjectMapper();
//			users = objectMapper.readValue(rs, new TypeReference<List<UserDto>>() {
//			});
//				response = rs.getObject("responseJson");
//				System.out.println("response received");
//				return response;
//			}
//
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		while (rs.next()) {
//			users.addAll(rs);
//		}
//
//		try {
//			ObjectMapper objectMapper = new ObjectMapper();
//			users = objectMapper.readValue(rs, new TypeReference<List<UserDto>>() {
//			});
//			while (rs.next()) {
//				UserDto userDto = new UserDto();
//				userDto.setId(rs.getInt("id"));
//				userDto.setUserName(rs.getString("userName"));
//				userDto.setFirstName(rs.getString("firstName"));
//				userDto.setLastName(rs.getString("lastName"));
//				userDto.setEmail(rs.getString("email"));
//				userDto.setRoleName(rs.getString("roleName"));
//				userDto.setStatus(rs.getString("status"));
//				userDto.setCreatedBy(rs.getString("createdBy"));
//				userDto.setCreatedOn(rs.getString("createdOn"));
//				users.add(userDto);
	// }
//	}catch(
//
//	Exception e)
//	{
//		log.error(CustomWidgetLog.getCurrentClassAndMethodName() + "Error while listing user : " + e);
//		log.error(
//				CustomWidgetLog.getCurrentClassAndMethodName() + "stack trace : " + Arrays.toString(e.getStackTrace()));
//		return new Response<List<UserDto>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting users")
//				.setResponse(null);
//	}
	// return new Response<List<UserDto>>(200, true, HttpStatus.OK, users.size() + "
	// users has been retrived")
	// .setResponse(users);
//		 return null;
//	}

//	@GetMapping("/list")
//	public Response<List<User>> getUsers() {
//		List<User> users = new ArrayList<>();
//		try {
//		
//        	users = userService.findAll();
//			log.info(CustomWidgetLog.getCurrentClassAndMethodName() + users);
//			for (User u : users) {
//				u.setPassword(aes.decrypt(u.getPassword()));
//			}
//			users.removeIf(
//					u -> (u.getUserName().equalsIgnoreCase("superadmin") || u.getStatus().equalsIgnoreCase("D")));
//		} catch (
//
//		Exception e) {
//			log.error(CustomWidgetLog.getCurrentClassAndMethodName() + "Error while listing user : " + e);
//			log.error(CustomWidgetLog.getCurrentClassAndMethodName() + "stack trace : "
//					+ Arrays.toString(e.getStackTrace()));
//			return new Response<List<User>>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting users")
//					.setResponse(null);
//		}
//		return new Response<List<User>>(200, true, HttpStatus.OK, users.size() + " users has been retrived")
//				.setResponse(users);
//	}

	@PutMapping("/update/{userId}")
	public Response<User> updateUser(@PathVariable Integer userId, @RequestBody UserDto userDto) {
		// log.info("userDto : " + userDto);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		User user = modelMapper.map(userDto, User.class);
		String dbPassword = aes.decrypt(userDto.getPassword());
		try {
			if (userDto != null) {
				User existingUser = userService.findById(userId);
				if (existingUser != null) {
					user.setId(existingUser.getId());
					if (dbPassword.equals("NA")) {
						user.setPassword(aes.encrypt(userDto.getPassword()));
					} else {
						user.setPassword(aes.encrypt(dbPassword));
					}
					Role role = roleRepository.findById(userDto.getRoleId()).get();
					user.setRole(role);
					user.setCreatedOn(existingUser.getCreatedOn());
					user.setCreatedBy(existingUser.getCreatedBy());
					user.setUpdatedOn(LocalDateTime.now().format(formatter));
					user = userService.save(user);
					log.info("user has been updated" + user);
				} else {
					return new Response<User>(404, false, HttpStatus.NOT_FOUND, "User not found").setResponse(user);
				}
			}
		} catch (Exception e) {
			log.error(" Error while updating user : " + e);
			log.error(" stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<User>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating user")
					.setResponse(null);
		}
		return new Response<User>(200, true, HttpStatus.OK, "User has been updated").setResponse(user);

	}

	@PutMapping("/update/password/{userId}")
	public Response<User> updateUserPassword(@PathVariable Integer userId, @RequestBody UserDto userDto) {
		// log.info("userDto : " + userDto);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		User user = modelMapper.map(userDto, User.class);
		try {
			if (userDto != null) {
				User existingUser = userService.findById(userId);
				if (existingUser != null) {
					user.setId(existingUser.getId());
					user.setPassword(aes.encrypt(userDto.getPassword()));
					Role role = roleRepository.findById(userDto.getRoleId()).get();
					user.setRole(role);
					user.setCreatedOn(existingUser.getCreatedOn());
					user.setCreatedBy(existingUser.getCreatedBy());
					user.setUpdatedOn(LocalDateTime.now().format(formatter));
					user = userService.save(user);
					log.info("User Password has been updated");
				} else {
					return new Response<User>(404, false, HttpStatus.NOT_FOUND, "User not found").setResponse(user);
				}
			}
		} catch (Exception e) {
			log.error(" Error while updating user : " + e);
			log.error(" stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<User>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating user")
					.setResponse(null);
		}
		return new Response<User>(200, true, HttpStatus.OK, "User has been updated").setResponse(user);

	}

	@DeleteMapping("/delete/{userId}")
	public Response<String> deleteUserById(@PathVariable Integer userId) {
		try {

			try {
				User user = userService.findById(userId);
				log.info("User has been deleted");
			} catch (Exception e) {
				return new Response<String>(500, false, HttpStatus.NOT_FOUND,
						"No user found with the userId : " + userId).setResponse(null);
			}
			userService.deleteById(userId);
		} catch (Exception e) {
			log.error(" Error while deleting user by id : " + e);
			log.error(" stack error : " + Arrays.toString(e.getStackTrace()));
			return new Response<String>(500, false, HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting user")
					.setResponse(null);
		}
		return new Response<String>(200, true, HttpStatus.OK, "User has been deleted").setResponse(null);
	}

	@GetMapping("/hasAccess/{userId}")
	public List<String> userFeatures(@PathVariable Integer userId) {
		List<String> features = new ArrayList<>();
		Set<Feature> userfeatures = new HashSet<>();

		User user = null;
		try {
			user = userService.findById(userId);
			userfeatures = user.getRole().getFeatures();
			for (Feature f : userfeatures) {
				features.add(f.getFeatureName());
			}
		} catch (Exception e) {
			log.error(" Error while getting user features : " + e);
			log.error(" stack trace : " + Arrays.toString(e.getStackTrace()));
		}
		return features;
	}

	@GetMapping("/supportDtls/{userId}")
	public String[] supportDtls(@PathVariable Integer userId) {
		User user = null;
		String usersupport = null;
		try {
			user = userService.findById(userId);
			usersupport = rolecontroller.getTypes(user.getRole().getSectionsAccessible());
		} catch (Exception e) {
			log.error(" Error while getting user features : " + e);
			log.error(" stack trace : " + Arrays.toString(e.getStackTrace()));
		}
		return usersupport.split(",");

	}

	@GetMapping("/findUser/{userName}")
	public Response<User> findByUserName(@PathVariable String userName) {

		User user = null;
		try {
			user = userService.findByUserNameIgnoreCaseAndStatusIgnoreCase(userName, Status.ACTIVE.toString());
			log.info("User retrieved successfully");

		} catch (Exception e) {
			log.error(" Error while getting user by username : " + e);
			log.error(" stack trace : " + Arrays.toString(e.getStackTrace()));
			return new Response<User>(500, false, HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while getting user by username").setResponse(null);
		}

		if (user == null) {
			return new Response<User>(404, false, HttpStatus.NOT_FOUND, "No user found with User name : " + userName)
					.setResponse(null);
		}

		return new Response<User>(200, true, HttpStatus.OK, "User retrieved successfully").setResponse(user);

	}
}
