package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String fromEmail = "margarit.fil@gmail.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "resetCodeExpirationTime", Duration.ofMinutes(7));
    }

    @Test
    void sendPasswordResetEmail_WithValidParameters_ShouldSendEmailSuccessfully() {
        // Arrange
        String toEmail = "user@test.com";
        String resetCode = "abc123-def456";
        String role = "CLIENT";

        // Act
        emailService.sendPasswordResetEmail(toEmail, resetCode, role);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(fromEmail, capturedMessage.getFrom());
        assertEquals(toEmail, Objects.requireNonNull(capturedMessage.getTo())[0]);
        assertEquals("Password reset code for Margosha Book Store ", capturedMessage.getSubject());

        String messageText = capturedMessage.getText();
        assertNotNull(messageText);
        assertTrue(messageText.contains("Hello Dear User❤️️❤️❤️"));
        assertTrue(messageText.contains("client account"));
        assertTrue(messageText.contains(resetCode));
        assertTrue(messageText.contains("7 minutes"));
        assertTrue(messageText.contains("Margosha Book Store"));
    }

    @Test
    void sendPasswordResetEmail_WithEmployeeRole_ShouldIncludeCorrectRoleInMessage() {
        // Arrange
        String toEmail = "employee@test.com";
        String resetCode = "xyz789";
        String role = "EMPLOYEE";

        // Act
        emailService.sendPasswordResetEmail(toEmail, resetCode, role);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        String messageText = capturedMessage.getText();
        assertNotNull(messageText);
        assertTrue(messageText.contains("employee account"));
        assertTrue(messageText.contains(resetCode));
    }

    @Test
    void sendPasswordResetEmail_WhenJavaMailSenderThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        String toEmail = "user@test.com";
        String resetCode = "abc123";
        String role = "CLIENT";

        MailException mailException = new MailException("SMTP server unavailable") {};
        doThrow(mailException).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendPasswordResetEmail(toEmail, resetCode, role));

        assertEquals("Failed to send password reset email", exception.getMessage());
        assertEquals(mailException, exception.getCause());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_WithDifferentExpirationTime_ShouldReflectInMessage() {
        // Arrange
        Duration customExpiration = Duration.ofMinutes(15);
        ReflectionTestUtils.setField(emailService, "resetCodeExpirationTime", customExpiration);

        String toEmail = "user@test.com";
        String resetCode = "test123";
        String role = "CLIENT";

        // Act
        emailService.sendPasswordResetEmail(toEmail, resetCode, role);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        String messageText = capturedMessage.getText();
        assertNotNull(messageText);
        assertTrue(messageText.contains("15 minutes"));
        assertFalse(messageText.contains("7 minutes"));
    }
}