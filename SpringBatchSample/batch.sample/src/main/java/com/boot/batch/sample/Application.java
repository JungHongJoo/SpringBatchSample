package com.boot.batch.sample;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@EnableBatchProcessing
@SpringBootApplication
public class Application {

	public static void main(String[] args) throws SQLException {
		System.out.println("#################TEST MAIN START####################");
		SpringApplication.run(Application.class, args);
	}
}