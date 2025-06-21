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

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BlockedClientRepository blockedClientRepository;

    @InjectMocks
    private MyUserDetailsServiceImpl userDetailsService;

    private Client client;
    private Employee employee;
    private String email;

    @BeforeEach
    void setUp() {
        client = getClientEntity();
        employee = getEmployeeEntity();
        email = "test@example.com";
    }

    @Test
    void loadUserByUsername_WhenClientExistsAndTheyAreNotBlocked_ShouldReturnClient() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(false);

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(client.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        assertEquals(client.getEmail(), result.getUsername());
        verify(clientRepository).getByEmail(client.getEmail());
        verify(blockedClientRepository).existsByClient_Email(client.getEmail());
    }

    @Test
    void loadUserByUsername_WhenClientDoesNotExist_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(clientRepository.getByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));
        assertEquals(exception.getMessage(), "Client with email " + email + " was not found!");
        verify(clientRepository).getByEmail(email);
        verify(blockedClientRepository, never()).existsByClient_Email(anyString());
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
        verify(clientRepository).getByEmail(client.getEmail());
        verify(blockedClientRepository).existsByClient_Email(client.getEmail());
    }

    @Test
    void loadEmployeeByUsername_WhenEmployeeExists_ShouldReturnEmployee() {
        // Arrange
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        // Act
        UserDetails result = userDetailsService.loadEmployeeByUsername(employee.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(employee, result);
        assertEquals(employee.getEmail(), result.getUsername());
        verify(employeeRepository).getByEmail(employee.getEmail());
    }

    @Test
    void loadEmployeeByUsername_WhenEmployeeDoesNotExist_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.getByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadEmployeeByUsername(email));

        assertTrue(exception.getMessage().contains("Employee with email " + email + " was not found!"));
        verify(employeeRepository).getByEmail(email);
    }

    @Test
    void loadUserBasedOnRole_WhenRoleIsEmployee_ShouldCallLoadEmployeeByUsername() {
        // Arrange
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE);

        // Assert
        assertNotNull(result);
        assertEquals(employee, result);
        verify(employeeRepository).getByEmail(employee.getEmail());
        verify(clientRepository, never()).getByEmail(anyString());
        verify(blockedClientRepository, never()).existsByClient_Email(employee.getEmail());
    }

    @Test
    void loadUserBasedOnRole_WhenRoleIsClient_ShouldCallLoadUserByUsername() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(false);

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(client.getEmail(), Role.CLIENT);

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        verify(clientRepository).getByEmail(client.getEmail());
        verify(blockedClientRepository).existsByClient_Email(client.getEmail());
        verify(employeeRepository, never()).getByEmail(client.getEmail());
    }

    @Test
    void loadUserBasedOnRole_WhenRoleIsNull_ShouldCallLoadUserByUsername() {
        // Arrange
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByClient_Email(client.getEmail())).thenReturn(false);

        // Act
        UserDetails result = userDetailsService.loadUserBasedOnRole(client.getEmail(), null);

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        verify(clientRepository).getByEmail(client.getEmail());
        verify(blockedClientRepository).existsByClient_Email(client.getEmail());
        verify(employeeRepository, never()).getByEmail(client.getEmail());
    }

    @Test
    void loadUserBasedOnRole_WhenEmployeeRoleButEmployeeNotFound_ShouldThrowException() {
        // Arrange
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE));

        assertTrue(exception.getMessage().contains("Employee with email " + employee.getEmail() + " was not found!"));
        verify(employeeRepository).getByEmail(employee.getEmail());
        verify(clientRepository, never()).getByEmail(employee.getEmail());
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
        verify(clientRepository).getByEmail(client.getEmail());
        verify(blockedClientRepository).existsByClient_Email(client.getEmail());
        verify(employeeRepository, never()).getByEmail(client.getEmail());
    }
}