package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.ClientRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import com.epam.rd.autocode.spring.project.repo.ClientRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.epam.rd.autocode.spring.project.testdata.ClientData.getClientEntity;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.getEmployeeEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private ClientRefreshTokenRepository clientRefreshTokenRepository;

    @Mock
    private EmployeeRefreshTokenRepository employeeRefreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private Client client;
    private Employee employee;
    private UUID testToken;
    private Duration refreshTokenExpirationTime;

    @BeforeEach
    void setUp() {
        client = getClientEntity();
        employee = getEmployeeEntity();
        testToken = UUID.randomUUID();
        refreshTokenExpirationTime = Duration.ofDays(7);

        // Set the expiration time using reflection
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationTime", refreshTokenExpirationTime);
    }

    @Test
    void generateRefreshToken_WhenUserIsEmployee_ShouldCreateEmployeeRefreshToken() {
        // Arrange
        EmployeeRefreshToken savedToken = new EmployeeRefreshToken(employee, LocalDateTime.now().plus(refreshTokenExpirationTime));
        savedToken.setToken(testToken);
        when(employeeRefreshTokenRepository.save(any(EmployeeRefreshToken.class))).thenReturn(savedToken);

        // Act
        UUID result = refreshTokenService.generateRefreshToken(employee);

        // Assert
        assertNotNull(result);
        assertEquals(testToken, result);

        ArgumentCaptor<EmployeeRefreshToken> tokenCaptor = ArgumentCaptor.forClass(EmployeeRefreshToken.class);
        verify(employeeRefreshTokenRepository).save(tokenCaptor.capture());
        verify(clientRefreshTokenRepository, never()).save(any());

        EmployeeRefreshToken capturedToken = tokenCaptor.getValue();
        assertEquals(employee, capturedToken.getEmployee());
        assertNotNull(capturedToken.getExpiresAt());
        assertTrue(capturedToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void generateRefreshToken_WhenUserIsClient_ShouldCreateClientRefreshToken() {
        // Arrange
        ClientRefreshToken savedToken = new ClientRefreshToken(client, LocalDateTime.now().plus(refreshTokenExpirationTime));
        savedToken.setToken(testToken);
        when(clientRefreshTokenRepository.save(any(ClientRefreshToken.class))).thenReturn(savedToken);

        // Act
        UUID result = refreshTokenService.generateRefreshToken(client);

        // Assert
        assertNotNull(result);
        assertEquals(testToken, result);

        ArgumentCaptor<ClientRefreshToken> tokenCaptor = ArgumentCaptor.forClass(ClientRefreshToken.class);
        verify(clientRefreshTokenRepository).save(tokenCaptor.capture());
        verify(employeeRefreshTokenRepository, never()).save(any());

        ClientRefreshToken capturedToken = tokenCaptor.getValue();
        assertEquals(client, capturedToken.getClient());
        assertNotNull(capturedToken.getExpiresAt());
        assertTrue(capturedToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void generateRefreshToken_ShouldSetCorrectExpirationTime() {
        // Arrange
        Duration customDuration = Duration.ofHours(24);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationTime", customDuration);

        EmployeeRefreshToken savedToken = new EmployeeRefreshToken(employee, LocalDateTime.now().plus(customDuration));
        savedToken.setToken(testToken);
        when(employeeRefreshTokenRepository.save(any(EmployeeRefreshToken.class))).thenReturn(savedToken);

        // Act
        refreshTokenService.generateRefreshToken(employee);

        // Assert
        ArgumentCaptor<EmployeeRefreshToken> tokenCaptor = ArgumentCaptor.forClass(EmployeeRefreshToken.class);
        verify(employeeRefreshTokenRepository).save(tokenCaptor.capture());

        EmployeeRefreshToken capturedToken = tokenCaptor.getValue();
        LocalDateTime expectedExpiration = LocalDateTime.now().plus(customDuration);

        // Allow for small time differences in test execution
        assertTrue(capturedToken.getExpiresAt().isAfter(expectedExpiration.minusSeconds(1)));
        assertTrue(capturedToken.getExpiresAt().isBefore(expectedExpiration.plusSeconds(1)));
    }

    @Test
    void isValidRefreshToken_WhenUserIsEmployeeAndTokenIsValid_ShouldReturnTrue() {
        // Arrange
        when(employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(testToken, employee);

        // Assert
        assertTrue(result);
        verify(employeeRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class));
        verify(clientRefreshTokenRepository, never()).existsByTokenAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidRefreshToken_WhenUserIsEmployeeAndTokenIsInvalid_ShouldReturnFalse() {
        // Arrange
        when(employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(testToken, employee);

        // Assert
        assertFalse(result);
        verify(employeeRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class));
        verify(clientRefreshTokenRepository, never()).existsByTokenAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidRefreshToken_WhenUserIsClientAndTokenIsValid_ShouldReturnTrue() {
        // Arrange
        when(clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(testToken, client);

        // Assert
        assertTrue(result);
        verify(clientRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class));
        verify(employeeRefreshTokenRepository, never()).existsByTokenAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidRefreshToken_WhenUserIsClientAndTokenIsInvalid_ShouldReturnFalse() {
        // Arrange
        when(clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(testToken, client);

        // Assert
        assertFalse(result);
        verify(clientRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class));
        verify(employeeRefreshTokenRepository, never()).existsByTokenAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidRefreshToken_ShouldCheckAgainstCurrentTime() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();
        when(employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        refreshTokenService.isValidRefreshToken(testToken, employee);

        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(employeeRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        assertTrue(capturedTime.isAfter(beforeCall.minusSeconds(1)));
        assertTrue(capturedTime.isBefore(beforeCall.plusSeconds(2)));
    }

    @Test
    void generateRefreshToken_WhenNullUser_ShouldThrowException() {
        // Act & Assert
        UserDetailsAreNullException exception = assertThrows(UserDetailsAreNullException.class, () -> {
            refreshTokenService.generateRefreshToken(null);
        });

        assertEquals("UserDetails must be either Employee or Client, but was null", exception.getMessage());
        verify(employeeRefreshTokenRepository, never()).save(any());
        verify(clientRefreshTokenRepository, never()).save(any());
    }

    @Test
    void isValidRefreshToken_WhenNullToken_ShouldNotThrowAndUseNullInRepository() {
        // Arrange
        when(employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(isNull(), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(null, employee);

        // Assert
        assertFalse(result);
        verify(employeeRefreshTokenRepository).existsByTokenAndExpiresAtAfter(isNull(), any(LocalDateTime.class));
    }

    @Test
    void isValidRefreshToken_WhenValidTokenButNullUser_ShouldCallClientRepository() {
        // Arrange
        when(clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = refreshTokenService.isValidRefreshToken(testToken, null);

        // Assert
        assertFalse(result);
        verify(employeeRefreshTokenRepository, never()).existsByTokenAndExpiresAtAfter(any(), any());
        verify(clientRefreshTokenRepository).existsByTokenAndExpiresAtAfter(eq(testToken), any(LocalDateTime.class));
    }
}