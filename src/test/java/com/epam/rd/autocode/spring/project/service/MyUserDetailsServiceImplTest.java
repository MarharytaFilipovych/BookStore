package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.MyUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.getClientEntity;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.getEmployeeEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BlockedClientRepository blockedClientRepository;

    @InjectMocks
    private MyUserDetailsServiceImpl userDetailsService;

    private Client client;
    private Employee employee;
    private String testEmail;
    private String nonExistentEmail;

    @BeforeEach
    void setUp() {
        client = getClientEntity();
        employee = getEmployeeEntity();
        testEmail = "test@example.com";
        nonExistentEmail = "nonexistent@example.com";
        client.setEmail(testEmail);
        employee.setEmail(testEmail);
    }

    private void mockSuccessfulClientLoad(String email, Client clientToReturn) {
        when(clientRepository.getByEmail(email)).thenReturn(Optional.of(clientToReturn));
        when(blockedClientRepository.existsByClient_Email(email)).thenReturn(false);
    }

    private void verifySuccessfulClientLoad(String email) {
        verify(clientRepository).getByEmail(email);
        verify(blockedClientRepository).existsByClient_Email(email);
        verify(employeeRepository, never()).getByEmail(anyString());
    }

    private void verifySuccessfulEmployeeLoad(String email) {
        verify(employeeRepository).getByEmail(email);
        verify(clientRepository, never()).getByEmail(anyString());
        verify(blockedClientRepository, never()).existsByClient_Email(anyString());
    }

    private void verifyClientNotFoundScenario(String email) {
        verify(clientRepository).getByEmail(email);
        verify(blockedClientRepository, never()).existsByClient_Email(anyString());
        verify(employeeRepository, never()).getByEmail(anyString());
    }

    private void verifyEmployeeNotFoundScenario(String email) {
        verify(employeeRepository).getByEmail(email);
        verify(clientRepository, never()).getByEmail(anyString());
        verify(blockedClientRepository, never()).existsByClient_Email(anyString());
    }

    private void verifyClientBlockedScenario(String email) {
        verify(clientRepository).getByEmail(email);
        verify(blockedClientRepository).existsByClient_Email(email);
        verify(employeeRepository, never()).getByEmail(anyString());
    }

    private void assertUserDetailsMatch(UserDetails result, UserDetails expected) {
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getAuthorities(), result.getAuthorities());
    }

    private void assertExceptionMessage(Exception exception, String expectedMessageFragment) {
        assertTrue(exception.getMessage().contains(expectedMessageFragment),
                "Expected exception message to contain: " + expectedMessageFragment +
                        ", but was: " + exception.getMessage());
    }

    @Test
    void loadUserByUsername_WhenClientExistsAndNotBlocked_ShouldReturnClient() {
        // Arrange
        mockSuccessfulClientLoad(client.getEmail(), client);

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(client.getEmail());

        // Assert
        assertUserDetailsMatch(result, client);
        verifySuccessfulClientLoad(client.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonexistent@example.com", "another@test.com", "missing@email.com"})
    void loadUserByUsername_WhenClientDoesNotExist_ShouldThrowUsernameNotFoundException(String email) {
        // Arrange
        when(clientRepository.getByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));

        assertExceptionMessage(exception, "Client with email " + email + " was not found!");
        verifyClientNotFoundScenario(email);
    }

    @Test
    void loadUserByUsername_WhenClientIsBlocked_ShouldThrowLockedException() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(true);

        // Act & Assert
        LockedException exception = assertThrows(LockedException.class,
                () -> userDetailsService.loadUserByUsername(client.getEmail()));

        assertEquals("Account is blocked!", exception.getMessage());
        verifyClientBlockedScenario(client.getEmail());
    }

    @Test
    void loadEmployeeByUsername_WhenEmployeeExists_ShouldReturnEmployee() {
        // Arrange
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        // Act
        UserDetails result = userDetailsService.loadEmployeeByUsername(employee.getEmail());

        // Assert
        assertUserDetailsMatch(result, employee);
        verifySuccessfulEmployeeLoad(employee.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonexistent@example.com", "missing@employee.com", "invalid@test.com"})
    void loadEmployeeByUsername_WhenEmployeeDoesNotExist_ShouldThrowUsernameNotFoundException(String email) {
        // Arrange
        when(employeeRepository.getByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadEmployeeByUsername(email));

        assertExceptionMessage(exception, "Employee with email " + email + " was not found!");
        verifyEmployeeNotFoundScenario(email);
    }

    @Test
    void loadUserBasedOnRole_WhenRoleIsEmployee_ShouldReturnEmployee() {
        // Arrange
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE);

        // Assert
        assertUserDetailsMatch(result, employee);
        verifySuccessfulEmployeeLoad(employee.getEmail());
    }

    @Test
    void loadUserBasedOnRole_WhenRoleIsClient_ShouldReturnClient() {
        // Arrange
        mockSuccessfulClientLoad(client.getEmail(), client);

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(client.getEmail(), Role.CLIENT);

        // Assert
        assertUserDetailsMatch(result, client);
        verifySuccessfulClientLoad(client.getEmail());
    }

    @ParameterizedTest
    @NullSource
    void loadUserBasedOnRole_WhenRoleIsNull_ShouldDefaultToClientLoad(Role role) {
        // Arrange
        mockSuccessfulClientLoad(client.getEmail(), client);

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(client.getEmail(), role);

        // Assert
        assertUserDetailsMatch(result, client);
        verifySuccessfulClientLoad(client.getEmail());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void loadUserBasedOnRole_WithValidRoles_ShouldRouteCorrectly(Role role) {
        // Arrange
        if (role == Role.EMPLOYEE) when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        else mockSuccessfulClientLoad(testEmail, client);

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(testEmail, role);

        // Assert
        if (role == Role.EMPLOYEE) {
            assertUserDetailsMatch(result, employee);
            verifySuccessfulEmployeeLoad(testEmail);
        } else {
            assertUserDetailsMatch(result, client);
            verifySuccessfulClientLoad(testEmail);
        }
    }

    @Test
    void loadUserBasedOnRole_WhenEmployeeRoleButEmployeeNotFound_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.getByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserBasedOnRole(nonExistentEmail, Role.EMPLOYEE));

        assertExceptionMessage(exception, "Employee with email " + nonExistentEmail + " was not found!");
        verifyEmployeeNotFoundScenario(nonExistentEmail);
    }

    @Test
    void loadUserBasedOnRole_WhenClientRoleButClientNotFound_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(clientRepository.getByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserBasedOnRole(nonExistentEmail, Role.CLIENT));

        assertExceptionMessage(exception, "Client with email " + nonExistentEmail + " was not found!");
        verifyClientNotFoundScenario(nonExistentEmail);
    }

    @Test
    void loadUserBasedOnRole_WhenClientRoleButClientIsBlocked_ShouldThrowLockedException() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(true);

        // Act & Assert
        LockedException exception = assertThrows(LockedException.class,
                () -> userDetailsService.loadUserBasedOnRole(client.getEmail(), Role.CLIENT));

        assertEquals("Account is blocked!", exception.getMessage());
        verifyClientBlockedScenario(client.getEmail());
    }

    @Test
    void loadUserBasedOnRole_WhenNullRoleButClientIsBlocked_ShouldThrowLockedException() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(true);

        // Act & Assert
        LockedException exception = assertThrows(LockedException.class,
                () -> userDetailsService.loadUserBasedOnRole(client.getEmail(), null));

        assertEquals("Account is blocked!", exception.getMessage());
        verifyClientBlockedScenario(client.getEmail());
    }
}