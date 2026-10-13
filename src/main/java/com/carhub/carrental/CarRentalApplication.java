package com.carhub.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableScheduling
@SpringBootApplication
public class CarRentalApplication {
	public static void main(String[] args) {
		SpringApplication.run(CarRentalApplication.class, args);
	}
}
