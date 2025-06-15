package com.epam.rd.autocode.spring.project.model.tokens.reset;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class ResetCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID code;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;
}

