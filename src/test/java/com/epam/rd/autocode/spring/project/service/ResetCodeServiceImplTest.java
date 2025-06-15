package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.tokens.reset.ClientResetCode;
import com.epam.rd.autocode.spring.project.model.tokens.reset.EmployeeResetCode;
import com.epam.rd.autocode.spring.project.repo.ClientResetCodeRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeResetCodeRepository;
import com.epam.rd.autocode.spring.project.service.impl.ResetCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ResetCodeServiceImplTest {

    @Mock
    private EmployeeResetCodeRepository employeeResetCodeRepository;

    @Mock
    private ClientResetCodeRepository clientResetCodeRepository;

    @InjectMocks
    private ResetCodeServiceImpl resetCodeService;

    private Client client;
    private Employee employee;
    private UUID testCode;
    private Duration resetCodeExpirationTime;

    @BeforeEach
    void setUp() {
        client = getClientEntity();
        employee = getEmployeeEntity();
        testCode = UUID.randomUUID();
        resetCodeExpirationTime = Duration.ofMinutes(7);
        ReflectionTestUtils.setField(resetCodeService, "resetCodeExpirationTime", resetCodeExpirationTime);
    }

    @Test
    void generateResetCode_WhenUserIsEmployee_ShouldCreateEmployeeResetCode() {
        // Arrange
        EmployeeResetCode savedCode = new EmployeeResetCode(employee, LocalDateTime.now().plus(resetCodeExpirationTime));
        savedCode.setCode(testCode);
        when(employeeResetCodeRepository.save(any(EmployeeResetCode.class))).thenReturn(savedCode);

        // Act
        UUID result = resetCodeService.generateResetCode(employee);

        // Assert
        assertNotNull(result);
        assertEquals(testCode, result);

        ArgumentCaptor<EmployeeResetCode> codeCaptor = ArgumentCaptor.forClass(EmployeeResetCode.class);
        verify(employeeResetCodeRepository).save(codeCaptor.capture());
        verify(clientResetCodeRepository, never()).save(any());

        EmployeeResetCode capturedCode = codeCaptor.getValue();
        assertEquals(employee, capturedCode.getEmployee());
        assertNotNull(capturedCode.getExpiresAt());
        assertTrue(capturedCode.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void generateResetCode_WhenUserIsClient_ShouldCreateClientResetCode() {
        // Arrange
        ClientResetCode savedCode = new ClientResetCode(client, LocalDateTime.now().plus(resetCodeExpirationTime));
        savedCode.setCode(testCode);
        when(clientResetCodeRepository.save(any(ClientResetCode.class))).thenReturn(savedCode);

        // Act
        UUID result = resetCodeService.generateResetCode(client);

        // Assert
        assertNotNull(result);
        assertEquals(testCode, result);

        ArgumentCaptor<ClientResetCode> codeCaptor = ArgumentCaptor.forClass(ClientResetCode.class);
        verify(clientResetCodeRepository).save(codeCaptor.capture());
        verify(employeeResetCodeRepository, never()).save(any());

        ClientResetCode capturedCode = codeCaptor.getValue();
        assertEquals(client, capturedCode.getClient());
        assertNotNull(capturedCode.getExpiresAt());
        assertTrue(capturedCode.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void generateResetCode_ShouldSetCorrectExpirationTime() {
        // Arrange
        Duration customDuration = Duration.ofMinutes(15);
        ReflectionTestUtils.setField(resetCodeService, "resetCodeExpirationTime", customDuration);

        EmployeeResetCode savedCode = new EmployeeResetCode(employee, LocalDateTime.now().plus(customDuration));
        savedCode.setCode(testCode);
        when(employeeResetCodeRepository.save(any(EmployeeResetCode.class))).thenReturn(savedCode);

        // Act
        resetCodeService.generateResetCode(employee);

        // Assert
        ArgumentCaptor<EmployeeResetCode> codeCaptor = ArgumentCaptor.forClass(EmployeeResetCode.class);
        verify(employeeResetCodeRepository).save(codeCaptor.capture());

        EmployeeResetCode capturedCode = codeCaptor.getValue();
        LocalDateTime expectedExpiration = LocalDateTime.now().plus(customDuration);

        assertTrue(capturedCode.getExpiresAt().isAfter(expectedExpiration.minusSeconds(1)));
        assertTrue(capturedCode.getExpiresAt().isBefore(expectedExpiration.plusSeconds(1)));
    }

    @Test
    void isValidResetCode_WhenUserIsEmployeeAndCodeIsValid_ShouldReturnTrue() {
        // Arrange
        when(employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        boolean result = resetCodeService.isValidResetCode(testCode, employee);

        // Assert
        assertTrue(result);
        verify(employeeResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class));
        verify(clientResetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidResetCode_WhenUserIsEmployeeAndCodeIsInvalid_ShouldReturnFalse() {
        // Arrange
        when(employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = resetCodeService.isValidResetCode(testCode, employee);

        // Assert
        assertFalse(result);
        verify(employeeResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class));
        verify(clientResetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidResetCode_WhenUserIsClientAndCodeIsValid_ShouldReturnTrue() {
        // Arrange
        when(clientResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        boolean result = resetCodeService.isValidResetCode(testCode, client);

        // Assert
        assertTrue(result);
        verify(clientResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class));
        verify(employeeResetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidResetCode_WhenUserIsClientAndCodeIsInvalid_ShouldReturnFalse() {
        // Arrange
        when(clientResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = resetCodeService.isValidResetCode(testCode, client);

        // Assert
        assertFalse(result);
        verify(clientResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class));
        verify(employeeResetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
    }

    @Test
    void isValidResetCode_ShouldCheckAgainstCurrentTime() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();
        when(employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        resetCodeService.isValidResetCode(testCode, employee);

        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(employeeResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        assertTrue(capturedTime.isAfter(beforeCall.minusSeconds(1)));
        assertTrue(capturedTime.isBefore(beforeCall.plusSeconds(2)));
    }

    @Test
    void generateResetCode_WhenNullUser_ShouldThrowException() {
        // Act & Assert
        UserDetailsAreNullException exception = assertThrows(UserDetailsAreNullException.class, () -> {
            resetCodeService.generateResetCode(null);
        });
        assertEquals("UserDetails must be either Employee or Client, but was null", exception.getMessage());
        verify(employeeResetCodeRepository, never()).save(any());
        verify(clientResetCodeRepository, never()).save(any());
    }

    @Test
    void isValidResetCode_WhenNullCode_ShouldNotThrowAndUseNullInRepository() {
        // Arrange
        when(employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(isNull(), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = resetCodeService.isValidResetCode(null, employee);

        // Assert
        assertFalse(result);
        verify(employeeResetCodeRepository).existsByCodeAndExpiresAtAfter(isNull(), any(LocalDateTime.class));
    }

    @Test
    void isValidResetCode_WhenValidCodeButNullUser_ShouldCallClientRepository() {
        // Arrange
        when(clientResetCodeRepository.existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        boolean result = resetCodeService.isValidResetCode(testCode, null);

        // Assert
        assertFalse(result);
        verify(employeeResetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
        verify(clientResetCodeRepository).existsByCodeAndExpiresAtAfter(eq(testCode), any(LocalDateTime.class));
    }
}