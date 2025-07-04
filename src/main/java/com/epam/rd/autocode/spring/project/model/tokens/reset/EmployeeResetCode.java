package com.epam.rd.autocode.spring.project.model.tokens.reset;

import com.epam.rd.autocode.spring.project.model.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employee_reset_codes", indexes = {@Index(name = "idx_employee_reset_expires", columnList = "expires_at")})
@NoArgsConstructor
public class EmployeeResetCode extends ResetCode {
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public EmployeeResetCode(Employee employee, LocalDateTime expiresAt) {
        this.employee = employee;
        setExpiresAt(expiresAt);
    }
}