package com.epam.rd.autocode.spring.project.model.tokens.reset;

import com.epam.rd.autocode.spring.project.model.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employee_reset_codes")
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