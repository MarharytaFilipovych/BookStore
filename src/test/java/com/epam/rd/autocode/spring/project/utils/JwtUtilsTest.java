package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.conf.JwtSettings;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock private JwtSettings jwtSettings;

    @InjectMocks private JwtUtils jwtUtils;

    private Authentication authentication;
    private String email;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        invalidToken = "invalid.token.here";

        lenient().when(jwtSettings.getSecretKey()).thenReturn("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hbmQtdmFsaWRhdGlvbi10ZXN0aW5n");
        lenient().when(jwtSettings.getExpirationTime()).thenReturn(Duration.ofMinutes(15));
        lenient().when(jwtSettings.getIssuer()).thenReturn("TestIssuer");

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.CLIENT.toString()));
        authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
    }

    @Test
    void generateToken_WithValidAuthentication_ShouldReturnValidToken() {
        // Act
        String token = jwtUtils.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts separated by dots");

        String username = jwtUtils.getUserName(token);
        List<String> roles = jwtUtils.getRoles(token);

        assertEquals(email, username);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(Role.CLIENT.toString()));
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    void generateToken_WithEmployeeRole_ShouldReturnValidToken() {
        // Arrange
        Collection<GrantedAuthority> employeeAuthorities = List.of(
                new SimpleGrantedAuthority(Role.EMPLOYEE.toString())
        );
        Authentication employeeAuth = new UsernamePasswordAuthenticationToken(
                "employee@example.com",
                null,
                employeeAuthorities
        );

        // Act
        String token = jwtUtils.generateToken(employeeAuth);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts separated by dots");

        String username = jwtUtils.getUserName(token);
        List<String> roles = jwtUtils.getRoles(token);

        assertEquals("employee@example.com", username);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(Role.EMPLOYEE.toString()));
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    void getUserName_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtUtils.generateToken(authentication);

        // Act
        String username = jwtUtils.getUserName(token);

        // Assert
        assertEquals(email, username);
    }

    @Test
    void getUserName_WithInvalidToken_ShouldReturnNull() {
        // Act
        String username = jwtUtils.getUserName(invalidToken);

        // Assert
        assertNull(username);
    }

    @Test
    void getRoles_WithValidToken_ShouldReturnRolesList() {
        // Arrange
        String token = jwtUtils.generateToken(authentication);

        // Act
        List<String> roles = jwtUtils.getRoles(token);

        // Assert
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(Role.CLIENT.toString()));
    }

    @Test
    void getRoles_WithInvalidToken_ShouldReturnEmptyList() {
        // Act
        List<String> roles = jwtUtils.getRoles(invalidToken);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtils.generateToken(authentication);

        // Act
        boolean isExpired = jwtUtils.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Arrange - Override expiration time for this specific test
        when(jwtSettings.getExpirationTime()).thenReturn(Duration.ofMillis(1));
        String token = jwtUtils.generateToken(authentication);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtUtils.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        // Act
        boolean isExpired = jwtUtils.isTokenExpired(invalidToken);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    void isTokenValid_WithValidTokenAndMatchingAuthentication_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateToken(authentication);

        // Act
        boolean isValid = jwtUtils.isTokenValid(token, authentication);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithValidTokenAndDifferentAuthentication_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtils.generateToken(authentication);
        Authentication differentAuth = new UsernamePasswordAuthenticationToken(
                "different@example.com", null, List.of()
        );

        // Act
        boolean isValid = jwtUtils.isTokenValid(token, differentAuth);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        when(jwtSettings.getExpirationTime()).thenReturn(Duration.ofMillis(1));
        String token = jwtUtils.generateToken(authentication);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtUtils.isTokenValid(token, authentication);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.isTokenValid(null, authentication);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUserName_WithNullToken_ShouldReturnNull() {
        // Act
        String username = jwtUtils.getUserName(null);

        // Assert
        assertNull(username);
    }

    @Test
    void getRoles_WithNullToken_ShouldReturnEmptyList() {
        // Act
        List<String> roles = jwtUtils.getRoles(null);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }
}