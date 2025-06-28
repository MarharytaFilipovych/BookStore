package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRefreshTokenRepository extends JpaRepository<EmployeeRefreshToken, Long> {
    boolean existsByTokenAndExpiresAtAfter(UUID token, LocalDateTime expiresAtAfter);

    @Modifying
    @Transactional
    void deleteEmployeeRefreshTokenByEmployee_Email(String employeeEmail);
}
