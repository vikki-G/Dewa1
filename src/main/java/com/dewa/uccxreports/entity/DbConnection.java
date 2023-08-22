package com.dewa.uccxreports.entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.dewa.uccxreports.util.Aes;

@Repository
public class DbConnection {
	private static final Logger log = LogManager.getLogger(DbConnection.class);

	private String driverNameKey = "spring.datasource.driver-class-name";
	private String urlKey = "spring.datasource.url";
	private String userNameKey = "spring.datasource.username";
	private String passWordKey = "spring.datasource.password";

	// private final Aes aes;

	public Connection getConnection() throws Exception {
		Connection connection = null;
		try {
			ResourceBundle rb = ResourceBundle.getBundle("application");
			String driverName = rb.getString(driverNameKey);
			String url = rb.getString(urlKey);
			String userName = rb.getString(userNameKey);
			String encryptedPassWord = rb.getString(passWordKey);
			Aes aes = new Aes();
			String decryptedPassWord = aes.decrypt(encryptedPassWord);
		//	System.out.println("Decrypted Password: " + decryptedPassWord);

			Class.forName(driverName);
			connection = DriverManager.getConnection(url, userName, decryptedPassWord);

			//System.out.println("Connection Success!!");
			log.info("DB Connection Success!!");
		} catch (Exception e) {
			//System.out.println("Db Connection Exception: " + e);
			log.error("Db Connection Exception: " + e);
		}

		return connection;
	}
}
