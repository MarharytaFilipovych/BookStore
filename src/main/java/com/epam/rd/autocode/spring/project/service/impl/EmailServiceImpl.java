package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${reset-code-expiration-time}")
    private Duration resetCodeExpirationTime;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetCode, String role) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password reset code for Margosha Book Store ");
            message.setText(buildPasswordResetMessage(resetCode, role));

            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetMessage(String resetCode, String userRole) {
        return String.format("""
            Hello Dear User❤️️❤️❤️,
            
            You have requested a password reset for your %s account!
            
            Your password reset code is: %s
            
            Please use this code to reset your password. This code will expire in %d minutes.
            
            If you didn't request this reset, please ignore this email.
            
            Thanks,
            Margosha Book Store
            """,
                userRole.toLowerCase(),
                resetCode,
                resetCodeExpirationTime.toMinutes()
        );
    }
}
