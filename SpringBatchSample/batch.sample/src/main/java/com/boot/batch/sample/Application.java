package com.boot.batch.sample;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		System.out.println("#################TEST MAIN START####################");
		System.out.println("###############GIT HUB COMMIT TEST##################");
		SpringApplication.run(Application.class, args);
	}
}
