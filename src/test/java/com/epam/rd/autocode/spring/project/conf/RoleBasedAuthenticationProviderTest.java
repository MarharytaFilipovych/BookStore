package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.MyUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.getClientEntity;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.getEmployeeEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleBasedAuthenticationProviderTest {

    @Mock
    private MyUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RoleBasedAuthenticationProvider authProvider;

    private Employee employee;
    private Client client;
    private String email;
    private String password;
    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        employee = getEmployeeEntity();
        client = getClientEntity();
        email = "test@example.com";
        password = "password123";
        authToken = new UsernamePasswordAuthenticationToken(email, password);
        employee.setPassword("$2a$10$encodedPassword");
        client.setPassword("$2a$10$encodedPassword");
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "manager", "unknown", ""})
    void authenticate_WithValidRoles_ShouldRouteToCorrectService(String inputRole, String expectedServiceRole) {
        // Arrange
        authToken.setDetails(Map.of("role", inputRole));
        UserDetails expectedUser = "employee".equals(expectedServiceRole) ? employee : client;
        when(userDetailsService.loadUserBasedOnRole(email, Role.fromString(expectedServiceRole)))
                .thenReturn(expectedUser);
        when(passwordEncoder.matches(password, expectedUser.getPassword()))
                .thenReturn(true);

        // Act
        Authentication result = authProvider.authenticate(authToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(expectedUser, result.getPrincipal());
        assertNull(result.getCredentials(), "Credentials should be cleared for security");
        assertEquals(expectedUser.getAuthorities(), result.getAuthorities());

        verify(userDetailsService).loadUserBasedOnRole(email, Role.fromString(expectedServiceRole));
        verify(passwordEncoder).matches(password, expectedUser.getPassword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "manager", "unknown", ""})
    void authenticate_WithInvalidRoles_ShouldThrowBadCredentialsException(String invalidRole) {
        // Arrange
        authToken.setDetails(Map.of("role", invalidRole));
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenReturn(client);
        when(passwordEncoder.matches(password, client.getPassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authToken));

        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder).matches(password, client.getPassword());
    }

    @Test
    void authenticate_WithNullRole_ShouldThrowBadCredentialsException() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("role", null);
        authToken.setDetails(details);
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenReturn(client);
        when(passwordEncoder.matches(password, client.getPassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authToken));

        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder).matches(password, client.getPassword());
    }

    @Test
    void authenticate_WithNoRoleInDetails_ShouldThrowBadCredentialsException() {
        // Arrange
        authToken.setDetails(Map.of("otherField", "value"));
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenReturn(client);
        when(passwordEncoder.matches(password, client.getPassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authToken));

        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder).matches(password, client.getPassword());
    }

    @Test
    void authenticate_WithNonMapDetails_ShouldThrowBadCredentialsException() {
        // Arrange
        authToken.setDetails("someStringDetails");
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenReturn(client);
        when(passwordEncoder.matches(password, client.getPassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authToken));

        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder).matches(password, client.getPassword());
    }

    @Test
    void authenticate_WithNullDetails_ShouldThrowBadCredentialsException() {
        // Arrange
        authToken.setDetails(null);
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenReturn(client);
        when(passwordEncoder.matches(password, client.getPassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authToken));

        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder).matches(password, client.getPassword());
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Arrange
        UsernamePasswordAuthenticationToken wrongToken =
                new UsernamePasswordAuthenticationToken(email, "wrongPassword");
        wrongToken.setDetails(Map.of("role", Role.EMPLOYEE));
        when(userDetailsService.loadUserBasedOnRole(email, Role.EMPLOYEE))
                .thenReturn(employee);
        when(passwordEncoder.matches("wrongPassword", employee.getPassword()))
                .thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authProvider.authenticate(wrongToken));

        assertEquals("Invalid credentials!", exception.getMessage());
        verify(userDetailsService).loadUserBasedOnRole(email, Role.EMPLOYEE);
        verify(passwordEncoder).matches("wrongPassword", employee.getPassword());
    }

    @Test
    void authenticate_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String nonExistentEmail = "nonexistent@email.com";
        UsernamePasswordAuthenticationToken wrongToken =
                new UsernamePasswordAuthenticationToken(nonExistentEmail, password);
        wrongToken.setDetails(Map.of("role", Role.EMPLOYEE));

        when(userDetailsService.loadUserBasedOnRole(nonExistentEmail, Role.EMPLOYEE))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authProvider.authenticate(wrongToken));

        assertEquals("User not found", exception.getMessage());
        verify(userDetailsService).loadUserBasedOnRole(nonExistentEmail, Role.EMPLOYEE);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_WithBlockedClient_ShouldThrowLockedException() {
        // Arrange
        authToken.setDetails(Map.of("role", "client"));
        when(userDetailsService.loadUserBasedOnRole(email, Role.CLIENT))
                .thenThrow(new LockedException("Account is blocked!"));

        // Act & Assert
        LockedException exception = assertThrows(LockedException.class,
                () -> authProvider.authenticate(authToken));

        assertEquals("Account is blocked!", exception.getMessage());
        verify(userDetailsService).loadUserBasedOnRole(email, Role.CLIENT);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void supports_WithUsernamePasswordAuthenticationToken_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(authProvider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @ParameterizedTest
    @ValueSource(classes = {Authentication.class, Object.class})
    @DisplayName("Provider should not support other authentication types")
    void supports_WithOtherAuthenticationTypes_ShouldReturnFalse(Class<?> authType) {
        // Act & Assert
        assertFalse(authProvider.supports(authType));
    }
}