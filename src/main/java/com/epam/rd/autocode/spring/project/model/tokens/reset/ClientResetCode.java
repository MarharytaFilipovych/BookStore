package com.epam.rd.autocode.spring.project.model.tokens.reset;

import com.epam.rd.autocode.spring.project.model.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "client_reset_codes", indexes = {@Index(name = "idx_client_reset_expires", columnList = "expires_at")})
@NoArgsConstructor
public class ClientResetCode extends ResetCode {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public ClientResetCode(Client client, LocalDateTime expiresAt) {
        this.client = client;
        setExpiresAt(expiresAt);
    }
}