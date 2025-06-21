package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.conf.JwtSettings;
import com.epam.rd.autocode.spring.project.dto.TokenResponseDTO;
import com.epam.rd.autocode.spring.project.dto.request.*;
import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.ClientRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.reset.ClientResetCode;
import com.epam.rd.autocode.spring.project.model.tokens.reset.EmployeeResetCode;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.impl.AuthServiceImpl;
import com.epam.rd.autocode.spring.project.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.getClientEntity;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.getEmployeeEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationProvider authenticationProvider;
    @Mock private JwtUtils jwtUtils;
    @Mock private JwtSettings jwtSettings;
    @Mock private EmployeeService employeeService;
    @Mock private MyUserDetailsService myUserDetailsService;
    @Mock private ClientService clientService;
    @Mock private ClientRefreshTokenRepository clientRefreshTokenRepository;
    @Mock private EmployeeRefreshTokenRepository employeeRefreshTokenRepository;
    @Mock private EmployeeResetCodeRepository employeeResetCodeRepository;
    @Mock private ClientResetCodeRepository clientResetCodeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private Employee employee;
    private Client client;
    private String email;
    private String password;
    private String accessToken;
    private UUID refreshTokenUuid;
    private UUID resetCodeUuid;
    private Duration jwtExpirationTime;

    @BeforeEach
    void setUp() {
        employee = getEmployeeEntity();
        client = getClientEntity();
        email = "test@example.com";
        employee.setEmail(email);
        client.setEmail(email);
        password = "password123";
        accessToken = "jwt.access.token";
        refreshTokenUuid = UUID.randomUUID();
        resetCodeUuid = UUID.randomUUID();
        jwtExpirationTime = Duration.ofMinutes(15);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationTime", Duration.ofDays(7));
        ReflectionTestUtils.setField(authService, "resetCodeExpirationTime", Duration.ofMinutes(7));
        lenient().when(jwtSettings.getExpirationTime()).thenReturn(jwtExpirationTime);
    }

    private void mockSuccessfulAuthentication(Role role, UserDetails userDetails) {
        Authentication authenticatedResult = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedResult);
        when(jwtUtils.generateToken(authenticatedResult)).thenReturn(accessToken);
    }

    private void mockRefreshTokenCreation(Role role, UUID tokenUuid) {
        if (role == Role.EMPLOYEE) {
            when(employeeRefreshTokenRepository.save(any(EmployeeRefreshToken.class)))
                    .thenAnswer(invocation -> {
                        EmployeeRefreshToken token = invocation.getArgument(0);
                        token.setToken(tokenUuid);
                        return token;
                    });
        } else {
            when(clientRefreshTokenRepository.save(any(ClientRefreshToken.class)))
                    .thenAnswer(invocation -> {
                        ClientRefreshToken token = invocation.getArgument(0);
                        token.setToken(tokenUuid);
                        return token;
                    });
        }
    }

    private void mockResetCodeCreation(Role role, UUID codeUuid) {
        if (role == Role.EMPLOYEE) {
            when(employeeResetCodeRepository.save(any(EmployeeResetCode.class)))
                    .thenAnswer(invocation -> {
                        EmployeeResetCode resetCode = invocation.getArgument(0);
                        resetCode.setCode(codeUuid);
                        return resetCode;
                    });
        } else {
            when(clientResetCodeRepository.save(any(ClientResetCode.class)))
                    .thenAnswer(invocation -> {
                        ClientResetCode resetCode = invocation.getArgument(0);
                        resetCode.setCode(codeUuid);
                        return resetCode;
                    });
        }
    }

    private void mockRefreshTokenValidation(Role role, UUID tokenUuid, boolean isValid) {
        if (role == Role.EMPLOYEE) {
            when(employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(
                    eq(tokenUuid), any(LocalDateTime.class))).thenReturn(isValid);
        } else {
            when(clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(
                    eq(tokenUuid), any(LocalDateTime.class))).thenReturn(isValid);
        }
    }

    private void mockResetCodeValidation(Role role, UUID codeUuid, boolean isValid) {
        if (role == Role.EMPLOYEE) {
            when(employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(
                    eq(codeUuid), any(LocalDateTime.class))).thenReturn(isValid);
        } else {
            when(clientResetCodeRepository.existsByCodeAndExpiresAtAfter(
                    eq(codeUuid), any(LocalDateTime.class))).thenReturn(isValid);
        }
    }

    private void verifyTokenResponse(TokenResponseDTO result, String expectedAccessToken,
                                     UUID expectedRefreshToken, long expectedExpiresIn) {
        assertNotNull(result);
        assertEquals(expectedAccessToken, result.getAccessToken());
        assertEquals(expectedRefreshToken.toString(), result.getRefreshToken());
        assertEquals(expectedExpiresIn, result.getExpiresIn());
    }

    private void verifyAuthenticationDetails(Role role) {
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationProvider).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals(email, capturedAuth.getName());
        assertEquals(password, capturedAuth.getCredentials());

        @SuppressWarnings("unchecked")
        Map<String, Object> capturedDetails = (Map<String, Object>) capturedAuth.getDetails();
        assertEquals(role, capturedDetails.get("role"));
    }

    private void verifyRefreshTokenOperations(Role role, String email) {
        if (role == Role.EMPLOYEE) {
            verify(employeeRefreshTokenRepository).deleteEmployeeRefreshTokenByEmployee_Email(email);
            verify(employeeRefreshTokenRepository).save(any(EmployeeRefreshToken.class));
        } else {
            verify(clientRefreshTokenRepository).deleteClientRefreshTokensByClient_Email(email);
            verify(clientRefreshTokenRepository).save(any(ClientRefreshToken.class));
        }
    }

    private void verifyResetCodeOperations(Role role, Long userId) {
        if (role == Role.EMPLOYEE) {
            verify(employeeResetCodeRepository).deleteEmployeeResetCodesByEmployee_Id(userId);
            verify(employeeResetCodeRepository).save(any(EmployeeResetCode.class));
        } else {
            verify(clientResetCodeRepository).deleteClientResetCodesByClient_Id(userId);
            verify(clientResetCodeRepository).save(any(ClientResetCode.class));
        }
    }

    private void verifyPasswordUpdate(Role role, String email, String newPassword) {
        if (role == Role.EMPLOYEE) verify(employeeService).updateEmployeePassword(email, newPassword);
        else verify(clientService).updateClientPassword(email, newPassword);
    }

    private void verifyLogoutOperations(Role role, String email) {
        if (role == Role.EMPLOYEE) {
            verify(employeeRefreshTokenRepository).deleteEmployeeRefreshTokenByEmployee_Email(email);
            verify(clientRefreshTokenRepository, never()).deleteClientRefreshTokensByClient_Email(email);
        } else {
            verify(clientRefreshTokenRepository).deleteClientRefreshTokensByClient_Email(email);
            verify(employeeRefreshTokenRepository, never()).deleteEmployeeRefreshTokenByEmployee_Email(email);
        }
    }

    private void verifyRefreshTokenCreation(Role role, UserDetails userDetails) {
        if (role == Role.EMPLOYEE) {
            ArgumentCaptor<EmployeeRefreshToken> tokenCaptor = ArgumentCaptor.forClass(EmployeeRefreshToken.class);
            verify(employeeRefreshTokenRepository).save(tokenCaptor.capture());

            EmployeeRefreshToken savedToken = tokenCaptor.getValue();
            assertEquals(userDetails, savedToken.getEmployee());
            assertTokenTimestamp(savedToken.getExpiresAt());
        } else {
            ArgumentCaptor<ClientRefreshToken> tokenCaptor = ArgumentCaptor.forClass(ClientRefreshToken.class);
            verify(clientRefreshTokenRepository).save(tokenCaptor.capture());

            ClientRefreshToken savedToken = tokenCaptor.getValue();
            assertEquals(userDetails, savedToken.getClient());
            assertTokenTimestamp(savedToken.getExpiresAt());
        }
    }

    private void verifyResetCodeCreation(Role role, UserDetails userDetails) {
        if (role == Role.EMPLOYEE) {
            ArgumentCaptor<EmployeeResetCode> resetCodeCaptor = ArgumentCaptor.forClass(EmployeeResetCode.class);
            verify(employeeResetCodeRepository).save(resetCodeCaptor.capture());

            EmployeeResetCode savedResetCode = resetCodeCaptor.getValue();
            assertEquals(userDetails, savedResetCode.getEmployee());
            assertTokenTimestamp(savedResetCode.getExpiresAt());
        } else {
            ArgumentCaptor<ClientResetCode> resetCodeCaptor = ArgumentCaptor.forClass(ClientResetCode.class);
            verify(clientResetCodeRepository).save(resetCodeCaptor.capture());

            ClientResetCode savedResetCode = resetCodeCaptor.getValue();
            assertEquals(userDetails, savedResetCode.getClient());
            assertTokenTimestamp(savedResetCode.getExpiresAt());
        }
    }

    private void assertTokenTimestamp(LocalDateTime expiresAt) {
        assertNotNull(expiresAt);
        assertTrue(expiresAt.isAfter(LocalDateTime.now()));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void loginUser_WithValidCredentials_ShouldReturnTokenResponse(Role role) {
        // Arrange
        LoginDTO loginRequest = new LoginDTO(email, password, role);
        UserDetails userDetails = role == Role.EMPLOYEE ? employee : client;

        mockSuccessfulAuthentication(role, userDetails);
        mockRefreshTokenCreation(role, refreshTokenUuid);

        // Act
        TokenResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        verifyTokenResponse(result, accessToken, refreshTokenUuid, jwtExpirationTime.toSeconds());
        verifyAuthenticationDetails(role);
        verify(jwtUtils).generateToken(any(Authentication.class));
        verifyRefreshTokenOperations(role, email);
    }

    @Test
    void loginUser_WithEmployeeCredentials_ShouldCreateEmployeeRefreshToken() {
        // Arrange
        LoginDTO loginRequest = new LoginDTO(employee.getEmail(), password, Role.EMPLOYEE);
        mockSuccessfulAuthentication(Role.EMPLOYEE, employee);
        mockRefreshTokenCreation(Role.EMPLOYEE, refreshTokenUuid);

        // Act
        TokenResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRefreshTokenRepository).deleteEmployeeRefreshTokenByEmployee_Email(employee.getEmail());
        verifyRefreshTokenCreation(Role.EMPLOYEE, employee);
    }

    @Test
    void loginUser_WithClientCredentials_ShouldCreateClientRefreshToken() {
        // Arrange
        LoginDTO loginRequest = new LoginDTO(client.getEmail(), password, Role.CLIENT);
        mockSuccessfulAuthentication(Role.CLIENT, client);
        mockRefreshTokenCreation(Role.CLIENT, refreshTokenUuid);

        // Act
        TokenResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        verify(clientRefreshTokenRepository).deleteClientRefreshTokensByClient_Email(client.getEmail());
        verifyRefreshTokenCreation(Role.CLIENT, client);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void refreshToken_WithValidToken_ShouldReturnNewTokenResponse(Role role) {
        // Arrange
        RefreshTokenDTO refreshRequest = new RefreshTokenDTO(refreshTokenUuid, email, role);
        UserDetails userDetails = role == Role.EMPLOYEE ? employee : client;
        String newAccessToken = "new.jwt.access.token";
        UUID newRefreshTokenUuid = UUID.randomUUID();

        mockRefreshTokenValidation(role, refreshTokenUuid, true);
        mockRefreshTokenCreation(role, newRefreshTokenUuid);
        when(myUserDetailsService.loadUserBasedOnRole(email, role)).thenReturn(userDetails);
        when(jwtUtils.generateToken(any(Authentication.class))).thenReturn(newAccessToken);

        // Act
        TokenResponseDTO result = authService.refreshToken(refreshRequest);

        // Assert
        verifyTokenResponse(result, newAccessToken, newRefreshTokenUuid, jwtExpirationTime.toSeconds());
        verify(myUserDetailsService).loadUserBasedOnRole(email, role);
        verify(jwtUtils).generateToken(any(Authentication.class));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void refreshToken_WithInvalidToken_ShouldThrowSecurityException(Role role) {
        // Arrange
        RefreshTokenDTO refreshRequest = new RefreshTokenDTO(refreshTokenUuid, email, role);
        mockRefreshTokenValidation(role, refreshTokenUuid, false);

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authService.refreshToken(refreshRequest));

        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(myUserDetailsService, never()).loadUserBasedOnRole(email, role);
        verify(jwtUtils, never()).generateToken(any());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void forgotPassword_WithValidUser_ShouldReturnResetCode(Role role) {
        // Arrange
        ForgotPasswordDTO forgotRequest = new ForgotPasswordDTO(email, role);
        UserDetails userDetails = role == Role.EMPLOYEE ? employee : client;

        when(myUserDetailsService.loadUserBasedOnRole(email, role)).thenReturn(userDetails);
        mockResetCodeCreation(role, resetCodeUuid);

        // Act
        UUID result = authService.forgotPassword(forgotRequest);

        // Assert
        assertNotNull(result);
        assertEquals(resetCodeUuid, result);
        verify(myUserDetailsService).loadUserBasedOnRole(email, role);
        long id = role == Role.EMPLOYEE ? ((Employee)userDetails).getId() : ((Client)userDetails).getId();
        verifyResetCodeOperations(role, id);
    }

    @Test
    void forgotPassword_WithEmployeeRole_ShouldCreateEmployeeResetCode() {
        // Arrange
        ForgotPasswordDTO forgotRequest = new ForgotPasswordDTO(employee.getEmail(), Role.EMPLOYEE);
        when(myUserDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE))
                .thenReturn(employee);
        mockResetCodeCreation(Role.EMPLOYEE, resetCodeUuid);

        // Act
        UUID result = authService.forgotPassword(forgotRequest);

        // Assert
        assertNotNull(result);
        verify(employeeResetCodeRepository).deleteEmployeeResetCodesByEmployee_Id(employee.getId());
        verifyResetCodeCreation(Role.EMPLOYEE, employee);
    }

    @Test
    void forgotPassword_WithClientRole_ShouldCreateClientResetCode() {
        // Arrange
        ForgotPasswordDTO forgotRequest = new ForgotPasswordDTO(client.getEmail(), Role.CLIENT);
        when(myUserDetailsService.loadUserBasedOnRole(client.getEmail(), Role.CLIENT))
                .thenReturn(client);
        mockResetCodeCreation(Role.CLIENT, resetCodeUuid);

        // Act
        UUID result = authService.forgotPassword(forgotRequest);

        // Assert
        assertNotNull(result);
        verify(clientResetCodeRepository).deleteClientResetCodesByClient_Id(client.getId());
        verifyResetCodeCreation(Role.CLIENT, client);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void changePassword_WithValidResetCode_ShouldUpdatePassword(Role role) {
        // Arrange
        String newPassword = "newPassword123";
        ResetPasswordDto resetRequest = new ResetPasswordDto(email, newPassword, resetCodeUuid, role);
        mockResetCodeValidation(role, resetCodeUuid, true);

        // Act
        authService.changePassword(resetRequest);

        // Assert
        verifyPasswordUpdate(role, email, newPassword);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void changePassword_WithInvalidResetCode_ShouldThrowSecurityException(Role role) {
        // Arrange
        String newPassword = "newPassword123";
        ResetPasswordDto resetRequest = new ResetPasswordDto(email, newPassword, resetCodeUuid, role);
        mockResetCodeValidation(role, resetCodeUuid, false);

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authService.changePassword(resetRequest));

        assertEquals("Invalid or expired reset code", exception.getMessage());
        verify(employeeService, never()).updateEmployeePassword(email, newPassword);
        verify(clientService, never()).updateClientPassword(email, newPassword);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void logout_ShouldDeleteRefreshTokens(Role role) {
        // Arrange
        LogoutDTO logoutRequest = new LogoutDTO(email, role);

        // Act
        authService.logout(logoutRequest);

        // Assert
        verifyLogoutOperations(role, email);
    }

    @Test
    void generateRefreshToken_WithInvalidUserDetails_ShouldThrowUserDetailsAreNullException() {
        // Arrange
        LoginDTO loginRequest = new LoginDTO(email, password, Role.CLIENT);
        Authentication authenticatedResult = new UsernamePasswordAuthenticationToken(null, null, null);
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedResult);

        // Act & Assert
        UserDetailsAreNullException exception = assertThrows(UserDetailsAreNullException.class,
                () -> authService.loginUser(loginRequest));

        assertTrue(exception.getMessage().contains("UserDetails must be either Employee or Client"));
    }

    @Test
    void generateResetCode_WithNullUserDetails_ShouldThrowUserDetailsAreNullException() {
        // Arrange
        ForgotPasswordDTO forgotRequest = new ForgotPasswordDTO(email, Role.CLIENT);
        when(myUserDetailsService.loadUserBasedOnRole(email, Role.CLIENT)).thenReturn(null);

        // Act & Assert
        UserDetailsAreNullException exception = assertThrows(UserDetailsAreNullException.class,
                () -> authService.forgotPassword(forgotRequest));

        assertEquals("UserDetails must be either Employee or Client, but was null", exception.getMessage());
    }

    @Test
    void refreshToken_ShouldDeleteOldRefreshTokenBeforeCreatingNew() {
        // Arrange
        RefreshTokenDTO refreshRequest = new RefreshTokenDTO();
        refreshRequest.setRefreshToken(refreshTokenUuid);
        refreshRequest.setEmail(employee.getEmail());
        refreshRequest.setRole(Role.EMPLOYEE);

        UUID newRefreshTokenUuid = UUID.randomUUID();
        mockRefreshTokenValidation(Role.EMPLOYEE, refreshTokenUuid, true);
        when(myUserDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE))
                .thenReturn(employee);
        when(jwtUtils.generateToken(any(Authentication.class))).thenReturn("new.token");
        mockRefreshTokenCreation(Role.EMPLOYEE, newRefreshTokenUuid);

        // Act
        authService.refreshToken(refreshRequest);

        // Assert
        verify(employeeRefreshTokenRepository).deleteEmployeeRefreshTokenByEmployee_Email(employee.getEmail());
        verify(employeeRefreshTokenRepository).save(any(EmployeeRefreshToken.class));
    }

    @Test
    void loginUser_ShouldDeleteOldRefreshTokenBeforeCreatingNew() {
        // Arrange
        LoginDTO loginRequest = new LoginDTO(client.getEmail(), password, Role.CLIENT);
        mockSuccessfulAuthentication(Role.CLIENT, client);
        mockRefreshTokenCreation(Role.CLIENT, refreshTokenUuid);

        // Act
        authService.loginUser(loginRequest);

        // Assert
        verify(clientRefreshTokenRepository).deleteClientRefreshTokensByClient_Email(client.getEmail());
        verify(clientRefreshTokenRepository).save(any(ClientRefreshToken.class));
    }

    @Test
    void forgotPassword_ShouldDeleteOldResetCodeBeforeCreatingNew() {
        // Arrange
        ForgotPasswordDTO forgotRequest = new ForgotPasswordDTO(employee.getEmail(), Role.EMPLOYEE);
        when(myUserDetailsService.loadUserBasedOnRole(employee.getEmail(), Role.EMPLOYEE))
                .thenReturn(employee);
        mockResetCodeCreation(Role.EMPLOYEE, resetCodeUuid);

        // Act
        authService.forgotPassword(forgotRequest);

        // Assert
        verify(employeeResetCodeRepository).deleteEmployeeResetCodesByEmployee_Id(employee.getId());
        verify(employeeResetCodeRepository).save(any(EmployeeResetCode.class));
    }
}