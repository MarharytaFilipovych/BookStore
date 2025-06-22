package com.epam.rd.autocode.spring.project.model.tokens.refresh;

import com.epam.rd.autocode.spring.project.model.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employee_refresh_tokens", indexes = {@Index(name = "idx_employee_refresh_expires", columnList = "expires_at")})
@NoArgsConstructor
public class EmployeeRefreshToken extends RefreshToken {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Employee employee;

    public EmployeeRefreshToken(Employee employee, LocalDateTime expiresAt) {
        this.employee = employee;
        setExpiresAt(expiresAt);
    }
}