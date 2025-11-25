package com.exhibition.exhibition_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExhibitionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExhibitionServiceApplication.class, args);
	}

}
