package com.dewa.uccxreports.controller;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewa.uccxreports.serviceImpl.DailyStatusReportsService;

@RestController
@EnableScheduling
public class DailyStatusReportsController {

	@Autowired
	private DailyStatusReportsService reportsService;

	private static final Logger log = LogManager.getLogger("DailyStatusReportsController");
	
	
	private final Lock lock = new ReentrantLock();

	@Scheduled(cron = "${cronExpression}")
	public void scheduleMailSending() {
		if (lock.tryLock()) {

			try {
				log.info("scheduler Start");
				sendMail();
			} finally {
				lock.unlock();
			}
		}
	}


	@GetMapping("/test")
	public String test() {
		return "test ok";
	}
	 
	@GetMapping("/sendMail")
	public void sendMail() {

		try {
			reportsService.schedulingDetailedReport();
			reportsService.schedulingCallTransactionReport();
			reportsService.schedulingSummarReport();
		} catch (Exception e) {
			log.error("Error While Sending Mail");
			e.printStackTrace();
		}
	}

}
