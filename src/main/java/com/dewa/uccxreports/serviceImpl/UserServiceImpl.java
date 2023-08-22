package com.dewa.uccxreports.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dewa.uccxreports.entity.User;
import com.dewa.uccxreports.repository.UserRepository;
import com.dewa.uccxreports.service.UserService;

import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Override
	public User save(User user) {

		try {
			User savedUser = userRepository.save(user);
			log.info("User data saved successfully");
			return savedUser;
		} catch (Exception e) {
			log.error("Error saving user data: " + (e));
		}
		return null;
	}

	@Override
	public boolean existsByUserNameIgnoreCase(String userName) {
		// TODO Auto-generated method stub
		return userRepository.existsByUserNameIgnoreCase(userName);
	}

	@Override
	public List<User> findAll() {
		try {
			List<User> users = userRepository.findAll();
			//log.info(CustomWidgetLog.getCurrentClassAndMethodName() + users);
			return users;
		} catch (Exception e) {
			log.error("Error While Getting Find All Users");
		}
		return null;
	}

	@Override
	public User findById(Integer userId) {
		// TODO Auto-generated method stub

		try {
			User user = userRepository.findById(userId).get();
			return user;
		} catch (Exception e) {
			log.error("Error While Finding Specific User");
		}
		return null;
	}

	@Override
	public void deleteById(Integer userId) {
		userRepository.deleteById(userId);

	}

	@Override
	public User findByUserNameIgnoreCaseAndStatusIgnoreCase(String userName, String status) {
		try {
			User user = userRepository.findByUserNameIgnoreCaseAndStatusIgnoreCase(userName, status);
			return user;
		} catch (Exception e) {
			log.error("Error While Finding User With Status");
		}
		return null;
	}

	    public Page<User> getUsersInChunks(int pageNumber, int pageSize) {
	        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
	        return userRepository.findAll(pageRequest);
	    }
}