package com.epam.rd.autocode.spring.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableJpaRepositories("com.epam.rd.autocode.spring.project.repo")
@EntityScan("com.epam.rd.autocode.spring.project.model")
@EnableJpaAuditing
@EnableMethodSecurity
@EnableWebSecurity
public class BookStoreServiceSolutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookStoreServiceSolutionApplication.class, args);
    }

}
