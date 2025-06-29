package com.epam.rd.autocode.spring.project.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetCode, String role);
}
