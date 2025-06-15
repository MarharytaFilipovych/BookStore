package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.tokens.reset.ClientResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ClientResetCodeRepository extends JpaRepository<ClientResetCode, UUID> {
    boolean existsByCodeAndExpiresAtAfter(UUID code, LocalDateTime expiresAtAfter);
}
