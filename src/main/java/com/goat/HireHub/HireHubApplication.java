package com.goat.HireHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HireHubApplication {
	public static void main(String[] args) {
		SpringApplication.run(HireHubApplication.class, args);
	}
}