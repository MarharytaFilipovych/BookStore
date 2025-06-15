package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.tokens.reset.ClientResetCode;
import com.epam.rd.autocode.spring.project.model.tokens.reset.EmployeeResetCode;
import com.epam.rd.autocode.spring.project.repo.ClientResetCodeRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeResetCodeRepository;
import com.epam.rd.autocode.spring.project.service.ResetCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResetCodeServiceImpl implements ResetCodeService {
    private final EmployeeResetCodeRepository employeeResetCodeRepository;
    private final ClientResetCodeRepository clientResetCodeRepository;

    public ResetCodeServiceImpl(EmployeeResetCodeRepository employeeResetCodeRepository, ClientResetCodeRepository clientResetCodeRepository) {
        this.employeeResetCodeRepository = employeeResetCodeRepository;
        this.clientResetCodeRepository = clientResetCodeRepository;
    }

    @Value("${reset-code-expiration-time}")
    private Duration resetCodeExpirationTime;

    public UUID generateResetCode(UserDetails user){
        LocalDateTime expiresAt = LocalDateTime.now().plus(resetCodeExpirationTime);
        return user instanceof Employee employee
                ? employeeResetCodeRepository.save(new EmployeeResetCode(employee, expiresAt)).getCode()
                : clientResetCodeRepository.save(new ClientResetCode((Client) user, expiresAt)).getCode();
    }

    public boolean isValidResetCode(UUID code, UserDetails user){
        return user instanceof Employee employee
                ? employeeResetCodeRepository.existsByCodeAndExpiresAtAfter(code, LocalDateTime.now())
                : clientResetCodeRepository.existsByCodeAndExpiresAtAfter(code, LocalDateTime.now());

    }
}
