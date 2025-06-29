package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.request.*;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.AuthService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.testdata.ClientData;
import com.epam.rd.autocode.spring.project.testdata.EmployeeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ClientService clientService;

    private LoginDTO loginDTO;
    private EmployeeDTO employeeDTO;
    private ClientDTO clientDTO;
    private RefreshTokenDTO refreshTokenDTO;
    private ForgotPasswordDTO forgotPasswordDTO;
    private ResetPasswordDto resetPasswordDto;
    private LogoutDTO logoutDTO;
    private TokenResponseDTO tokenResponseDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO("test@example.com", "password123", Role.CLIENT);
        employeeDTO = EmployeeData.getEmployeeDTO();
        clientDTO = ClientData.getClientDTO();
        refreshTokenDTO = new RefreshTokenDTO(UUID.randomUUID(), "test@example.com", Role.CLIENT);
        forgotPasswordDTO = new ForgotPasswordDTO("test@example.com", Role.CLIENT);
        resetPasswordDto = new ResetPasswordDto("test@example.com", "newPassword123!", UUID.randomUUID(), Role.CLIENT);
        logoutDTO = new LogoutDTO("test@example.com", Role.CLIENT);
        tokenResponseDTO = new TokenResponseDTO("access.token.jwt", "refresh-token-uuid", 3600L);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokenResponse() throws Exception {
        // Arrange
        when(authService.loginUser(loginDTO)).thenReturn(tokenResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access.token.jwt"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-uuid"))
                .andExpect(jsonPath("$.expires_in").value(3600L));

        verify(authService).loginUser(loginDTO);
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn500() throws Exception {
        // Arrange
        doThrow(new SecurityException("Invalid credentials"))
                .when(authService).loginUser(loginDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService).loginUser(loginDTO);
    }

    @Test
    void login_WithInvalidRequestBody_ShouldReturn400() throws Exception {
        // Arrange
        LoginDTO invalidLoginDTO = new LoginDTO("", "", null);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).loginUser(loginDTO);
    }

    @Test
    void registerEmployee_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        when(employeeService.addEmployee(employeeDTO)).thenReturn(employeeDTO);

        String validRequestBody = """
                {
                    "email": "mike.stevens@bookstore.com",
                    "password": "pA*ssword3",
                    "name": "Mike Stevens",
                    "phone": "+380971518786",
                    "birthdate": "1985-03-15"
                }""";

        // Act & Assert
        mockMvc.perform(post("/auth/register/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("Successfully registered!"));

        verify(employeeService).addEmployee(employeeDTO);
    }

    @Test
    void registerEmployee_WithExistingEmail_ShouldReturn409() throws Exception {
        // Arrange
        String validRequestBody = """
                {
                    "email": "mike.stevens@bookstore.com",
                    "password": "pA*ssword3",
                    "name": "Mike Stevens",
                    "phone": "+380971518786",
                    "birthdate": "1985-03-15"
                }""";

        doThrow(new AlreadyExistException("Employee with email mike.stevens@bookstore.com already exists"))
                .when(employeeService).addEmployee(employeeDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/register/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isConflict());

        verify(employeeService).addEmployee(employeeDTO);
    }

    @Test
    void registerEmployee_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        String invalidRequestBody = """
                {
                    "email": "invalid-email",
                    "password": "weak",
                    "name": "Valid Name",
                    "phone": "+380123456789",
                    "birthdate": "1990-01-01"
                }""";

        // Act & Assert
        mockMvc.perform(post("/auth/register/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).addEmployee(any(EmployeeDTO.class));
    }

    // Client registration tests
    @Test
    void registerClient_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        when(clientService.addClient(clientDTO)).thenReturn(clientDTO);

        String validRequestBody = """
                {
                    "email": "sarah.johnson@email.com",
                    "password": "passwREord1*",
                    "name": "Sarah Johnson",
                    "balance": 250.75
                }""";

        // Act & Assert
        mockMvc.perform(post("/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("Successfully registered!"));

        verify(clientService).addClient(clientDTO);
    }

    @Test
    void registerClient_WithExistingEmail_ShouldReturn409() throws Exception {
        // Arrange
        String validRequestBody = """
                {
                    "email": "sarah.johnson@email.com",
                    "password": "passwREord1*",
                    "name": "Sarah Johnson",
                    "balance": 250.75
                }""";

        doThrow(new AlreadyExistException("Client with email sarah.johnson@email.com already exists"))
                .when(clientService).addClient(clientDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isConflict());

        verify(clientService).addClient(clientDTO);
    }

    @Test
    void registerClient_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        String invalidRequestBody = """
                {
                    "email": "invalid-email",
                    "password": "weak",
                    "name": "Valid Name",
                    "balance": 100.00
                }""";

        // Act & Assert
        mockMvc.perform(post("/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).addClient(any(ClientDTO.class));
    }

    @Test
    @WithMockUser
    void refreshToken_WithValidToken_ShouldReturnNewTokenResponse() throws Exception {
        // Arrange
        when(authService.refreshToken(refreshTokenDTO)).thenReturn(tokenResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access.token.jwt"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-uuid"))
                .andExpect(jsonPath("$.expires_in").value(3600L));

        verify(authService).refreshToken(refreshTokenDTO);
    }

    @Test
    @WithMockUser
    void refreshToken_WithInvalidToken_ShouldReturn500() throws Exception {
        // Arrange
        doThrow(new SecurityException("Invalid or expired refresh token"))
                .when(authService).refreshToken(refreshTokenDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService).refreshToken(refreshTokenDTO);
    }

    @Test
    void refreshToken_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).refreshToken(refreshTokenDTO);
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnResetCode() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value("The reset code was sent to the email test@example.com"));

        verify(authService).forgotPassword(forgotPasswordDTO);
    }

    @Test
    void forgotPassword_WithNonExistentEmail_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("User not found"))
                .when(authService).forgotPassword(forgotPasswordDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordDTO)))
                .andExpect(status().isNotFound());

        verify(authService).forgotPassword(forgotPasswordDTO);
    }

    @Test
    void forgotPassword_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        ForgotPasswordDTO invalidForgotPasswordDTO = new ForgotPasswordDTO("", null);

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidForgotPasswordDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).forgotPassword(invalidForgotPasswordDTO);
    }

    // Change password tests
    @Test
    void changePassword_WithValidData_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(authService).changePassword(resetPasswordDto);

        // Act & Assert
        mockMvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andExpect(status().isNoContent());

        verify(authService).changePassword(resetPasswordDto);
    }

    @Test
    void changePassword_WithInvalidResetCode_ShouldReturn500() throws Exception {
        // Arrange
        doThrow(new SecurityException("Invalid or expired reset code"))
                .when(authService).changePassword(resetPasswordDto);

        // Act & Assert
        mockMvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andExpect(status().isUnauthorized());

        verify(authService).changePassword(resetPasswordDto);
    }

    @Test
    void changePassword_WithInvalidData_ShouldReturn400() throws Exception {
        ResetPasswordDto invalidResetPasswordDto = new ResetPasswordDto(
                "invalid-email",
                "123",
                UUID.randomUUID(),
                Role.CLIENT
        );

        // Act & Assert
        mockMvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResetPasswordDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));

        verify(authService, never()).changePassword(invalidResetPasswordDto);
    }

    @Test
    @WithMockUser
    void logout_WithValidData_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(authService).logout(logoutDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutDTO)))
                .andExpect(status().isNoContent());

        verify(authService).logout(logoutDTO);
    }

    @Test
    @WithMockUser
    void logout_WithServiceException_ShouldReturn500() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Logout failed"))
                .when(authService).logout(logoutDTO);

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutDTO)))
                .andExpect(status().isInternalServerError());

        verify(authService).logout(logoutDTO);
    }

    @Test
    void logout_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).logout(logoutDTO);
    }

    @Test
    @WithMockUser(roles = {"CLIENT", "EMPLOYEE"})
    void logout_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        LogoutDTO invalidLogoutDTO = new LogoutDTO("", null);

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogoutDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(invalidLogoutDTO);
    }
}