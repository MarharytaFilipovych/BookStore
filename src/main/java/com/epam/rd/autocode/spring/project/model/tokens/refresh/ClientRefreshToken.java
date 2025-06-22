package com.epam.rd.autocode.spring.project.model.tokens.refresh;

import com.epam.rd.autocode.spring.project.model.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "client_refresh_tokens", indexes = {@Index(name = "idx_client_refresh_expires", columnList = "expires_at")})
@NoArgsConstructor
public class ClientRefreshToken extends RefreshToken {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public ClientRefreshToken(Client client,LocalDateTime expiresAt) {
        this.client = client;
        setExpiresAt(expiresAt);
    }
}