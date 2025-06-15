package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.tokens.refresh.EmployeeRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRefreshTokenRepository extends JpaRepository<EmployeeRefreshToken, Long> {
    Optional<EmployeeRefreshToken> findByTokenAndExpiresAtAfter(UUID token, LocalDateTime expiresAtAfter);

    boolean existsByTokenAndExpiresAtAfter(UUID token, LocalDateTime expiresAtAfter);
}
