package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientUpdateDTO {
    @CorrectName
    private String name;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;
}