package com.dewa.uccxreports.controller;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.cache.EhCacheBasedUserCache;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewa.uccxreports.dto.LoginUserDTO;
import com.dewa.uccxreports.dto.ResponseDto;
import com.dewa.uccxreports.dto.UserDto;
import com.dewa.uccxreports.entity.Feature;
import com.dewa.uccxreports.entity.User;
import com.dewa.uccxreports.repository.UserRepository;
import com.dewa.uccxreports.util.Aes;
import com.dewa.uccxreports.util.EntityConstants.Status;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class LoginController {

	private static final Logger log = LogManager.getLogger("LoginController");
	@Autowired
	private EhCacheBasedUserCache userCache;

	@Autowired
	UserRepository usersRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private Aes aes;

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserDto loginRequest) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		User userDtls = modelMapper.map(loginRequest, User.class);
		try {
			userDtls = usersRepository.findByUserNameIgnoreCaseAndStatusIgnoreCase(loginRequest.getUserName(),
					Status.ACTIVE.toString());
			// log.info(CustomWidgetLog.getCurrentClassAndMethodName() + userDtls);
		} catch (Exception e) {
			log.error("User Not Found");
		}

		if (userDtls != null) {

			String dbPassword = aes.decrypt(userDtls.getPassword());
			if (loginRequest.getPassword().equals(dbPassword)) {

				LoginUserDTO userDTO = modelMapper.map(userDtls, LoginUserDTO.class);

				Set<Feature> featureObjs = userDtls.getRole().getFeatures();
				List<String> features = featureObjs.stream().map(f -> f.getFeatureName()).collect(Collectors.toList());
				userDTO.setFeatures(features);
				userDTO.setPassword(dbPassword);
				userDTO.setStatusCode(HttpStatus.OK);
				log.info("Login success " + userDtls.getUserName());
				return ResponseEntity.ok(userDTO);
			} else {

				return new ResponseEntity<>(new ResponseDto("Please enter vaild credentials!", HttpStatus.BAD_REQUEST),
						HttpStatus.OK);
			}
		} else {
			log.info("---> \"User Not Found\"");
			return new ResponseEntity<>(new ResponseDto("Please enter vaild credentials!", HttpStatus.BAD_REQUEST),
					HttpStatus.OK);
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@Valid @RequestBody LoginUserDTO userDTO) {
		userCache.removeUserFromCache(userDTO.getUsername());
		log.info("User Logout successfully!");
		return new ResponseEntity<>(new ResponseDto("Logout successfully!", HttpStatus.OK), HttpStatus.OK);
	}

}
