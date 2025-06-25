package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.conf.JwtSettings;
import com.epam.rd.autocode.spring.project.dto.request.ForgotPasswordDTO;
import com.epam.rd.autocode.spring.project.dto.request.LoginDTO;
import com.epam.rd.autocode.spring.project.dto.request.LogoutDTO;
import com.epam.rd.autocode.spring.project.dto.request.ResetPasswordDto;
import com.epam.rd.autocode.spring.project.dto.request.RefreshTokenDTO;
import com.epam.rd.autocode.spring.project.dto.TokenResponseDTO;
import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.ClientRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.reset.ClientResetCode;
import com.epam.rd.autocode.spring.project.model.tokens.reset.EmployeeResetCode;
import com.epam.rd.autocode.spring.project.repo.ClientRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.ClientResetCodeRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeResetCodeRepository;
import com.epam.rd.autocode.spring.project.service.*;
import com.epam.rd.autocode.spring.project.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationProvider authenticationProvider;
    private final JwtUtils jwtUtils;
    private final JwtSettings jwtSettings;
    private final EmployeeService employeeService;
    private final MyUserDetailsService myUserDetailsService;
    private final ClientService clientService;
    private final ClientRefreshTokenRepository clientRefreshTokenRepository;
    private final EmployeeRefreshTokenRepository employeeRefreshTokenRepository;
    private final EmployeeResetCodeRepository employeeResetCodeRepository;
    private final ClientResetCodeRepository clientResetCodeRepository;

    @Value("${refresh-token-expiration-time}")
    private Duration refreshTokenExpirationTime;

    @Value("${reset-code-expiration-time}")
    private Duration resetCodeExpirationTime;

    public AuthServiceImpl(AuthenticationProvider authenticationProvider, JwtUtils jwtUtils, JwtSettings jwtSettings, EmployeeService employeeService, MyUserDetailsService myUserDetailsService, ClientService clientService, ClientRefreshTokenRepository clientRefreshTokenRepository, EmployeeRefreshTokenRepository employeeRefreshTokenRepository, EmployeeResetCodeRepository employeeResetCodeRepository, ClientResetCodeRepository clientResetCodeRepository) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtils = jwtUtils;
        this.jwtSettings = jwtSettings;
        this.employeeService = employeeService;
        this.myUserDetailsService = myUserDetailsService;
        this.clientService = clientService;
        this.clientRefreshTokenRepository = clientRefreshTokenRepository;
        this.employeeRefreshTokenRepository = employeeRefreshTokenRepository;
        this.employeeResetCodeRepository = employeeResetCodeRepository;
        this.clientResetCodeRepository = clientResetCodeRepository;
    }

    public TokenResponseDTO loginUser(LoginDTO request){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword());

        Map<String, Object> details = new HashMap<>();
        details.put("role", request.getRole());
        authenticationToken.setDetails(details);

        Authentication authentication = authenticationProvider.authenticate(authenticationToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.generateToken(authentication);

        UUID refreshToken = generateRefreshToken(userDetails);

        return new TokenResponseDTO(accessToken,
                        refreshToken.toString(),
                        jwtSettings.getExpirationTime().toSeconds());
    }

    public TokenResponseDTO refreshToken(RefreshTokenDTO request){
        if (!isValidRefreshToken(request)) throw new SecurityException("Invalid or expired refresh token");

        UserDetails user = myUserDetailsService.loadUserBasedOnRole(request.getEmail(), request.getRole());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getAuthorities());

        String newAccessToken = jwtUtils.generateToken(auth);
        UUID newRefreshToken = generateRefreshToken(user);
        return new TokenResponseDTO(
                newAccessToken,
                newRefreshToken.toString(),
                jwtSettings.getExpirationTime().toSeconds()
        );
    }

    public UUID forgotPassword(ForgotPasswordDTO request){
        UserDetails user = myUserDetailsService.loadUserBasedOnRole(request.getEmail(), request.getRole());
        return generateResetCode(user);
    }

    public void changePassword(ResetPasswordDto request){
        if(!isValidResetCode(request)){
            throw new SecurityException("Invalid or expired reset code");
        }
        if(request.getRole() == Role.EMPLOYEE) employeeService.updateEmployeePassword(request.getEmail(), request.getPassword());
        else clientService.updateClientPassword(request.getEmail(), request.getPassword());
    }

    public void logout(LogoutDTO request){
        if (request.getRole() == Role.EMPLOYEE) {
            employeeRefreshTokenRepository.deleteEmployeeRefreshTokenByEmployee_Email(request.getEmail());
        }
        else clientRefreshTokenRepository.deleteClientRefreshTokensByClient_Email(request.getEmail());
    }

    private boolean isValidRefreshToken(RefreshTokenDTO request){
        return request.getRole() == Role.EMPLOYEE
                ? employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(request.getRefreshToken(), LocalDateTime.now())
                : clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(request.getRefreshToken(), LocalDateTime.now());
    }

    private UUID generateRefreshToken(UserDetails userDetails) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTokenExpirationTime);
        if (userDetails instanceof Employee employee) {
            employeeRefreshTokenRepository.deleteEmployeeRefreshTokenByEmployee_Email(employee.getEmail());
            return employeeRefreshTokenRepository.save(new EmployeeRefreshToken(employee, expiresAt)).getToken();
        } else if (userDetails instanceof Client client) {
            clientRefreshTokenRepository.deleteClientRefreshTokensByClient_Email(client.getEmail());
            return clientRefreshTokenRepository.save(new ClientRefreshToken(client, expiresAt)).getToken();
        }
        throw new UserDetailsAreNullException(userDetails == null ? "null" : userDetails.getClass().getSimpleName());
    }

    private UUID generateResetCode(UserDetails user){
        LocalDateTime expiresAt = LocalDateTime.now().plus(resetCodeExpirationTime);
        if(user instanceof Employee employee){
            employeeResetCodeRepository.deleteEmployeeResetCodesByEmployee_Id(employee.getId());
            return employeeResetCodeRepository.save(new EmployeeResetCode(employee, expiresAt)).getCode();
        } else if (user instanceof Client client) {
            clientResetCodeRepository.deleteClientResetCodesByClient_Id(client.getId());
            return clientResetCodeRepository.save(new ClientResetCode(client, expiresAt)).getCode();
        }
        throw new UserDetailsAreNullException(user == null ? "null" : user.getClass().getSimpleName());
    }

    private boolean isValidResetCode(ResetPasswordDto request){
        return request.getRole() == Role.EMPLOYEE
                ? employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(request.getResetCode(), LocalDateTime.now())
                : clientResetCodeRepository.existsByCodeAndExpiresAtAfter(request.getResetCode(), LocalDateTime.now());
    }
}
