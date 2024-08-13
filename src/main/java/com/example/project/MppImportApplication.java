package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories("com.example.project.repository")
@EntityScan("com.example.project.model")
public class MppImportApplication {
	public static void main(String[] args) {
		SpringApplication.run(MppImportApplication.class, args);
	}
}
