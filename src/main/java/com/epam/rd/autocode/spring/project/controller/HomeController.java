package com.epam.rd.autocode.spring.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public ResponseEntity<String> getHello(){
        return ResponseEntity.ok("Welcome!");
    }
}
