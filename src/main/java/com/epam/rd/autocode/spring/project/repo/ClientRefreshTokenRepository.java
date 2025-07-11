package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.tokens.refresh.ClientRefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ClientRefreshTokenRepository extends JpaRepository<ClientRefreshToken, Long> {
    boolean existsByTokenAndExpiresAtAfter(UUID token, LocalDateTime expiresAtAfter);

    @Modifying
    @Transactional
    void deleteClientRefreshTokensByClient_Email(String clientEmail);
}
