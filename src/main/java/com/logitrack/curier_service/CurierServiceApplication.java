package com.logitrack.curier_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CurierServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurierServiceApplication.class, args);
	}

}
