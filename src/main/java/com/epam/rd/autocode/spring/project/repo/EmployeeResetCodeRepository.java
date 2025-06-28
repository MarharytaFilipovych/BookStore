package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.tokens.reset.EmployeeResetCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface EmployeeResetCodeRepository extends JpaRepository<EmployeeResetCode, UUID> {
    boolean existsByCodeAndExpiresAtAfter(UUID code, LocalDateTime expiresAtAfter);

    @Modifying
    @Transactional
    void deleteEmployeeResetCodesByEmployee_Id(Long employeeId);
}
