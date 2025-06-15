package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.ClientRefreshToken;
import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import com.epam.rd.autocode.spring.project.repo.ClientRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final ClientRefreshTokenRepository clientRefreshTokenRepository;
    private final EmployeeRefreshTokenRepository employeeRefreshTokenRepository;

    @Value("${refresh-token-expiration-time}")
    private Duration refreshTokenExpirationTime;

    public RefreshTokenServiceImpl(ClientRefreshTokenRepository clientRefreshTokenRepository, EmployeeRefreshTokenRepository employeeRefreshTokenRepository) {
        this.clientRefreshTokenRepository = clientRefreshTokenRepository;
        this.employeeRefreshTokenRepository = employeeRefreshTokenRepository;
    }

    public UUID generateRefreshToken(UserDetails userDetails) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTokenExpirationTime);

        if (userDetails instanceof Employee employee) {
            return employeeRefreshTokenRepository.save(new EmployeeRefreshToken(employee, expiresAt)).getToken();
        } else if (userDetails instanceof Client client) {
            return clientRefreshTokenRepository.save(new ClientRefreshToken(client, expiresAt)).getToken();
        }
        throw new UserDetailsAreNullException(userDetails == null ? "null" : userDetails.getClass().getSimpleName());
    }

    public boolean isValidRefreshToken(UUID token, UserDetails user){
        return user instanceof Employee
                ? employeeRefreshTokenRepository.existsByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                : clientRefreshTokenRepository.existsByTokenAndExpiresAtAfter(token, LocalDateTime.now());
    }
}
