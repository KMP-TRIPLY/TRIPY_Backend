package com.kmp.Triply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TriplyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriplyApplication.class, args);
	}

}
