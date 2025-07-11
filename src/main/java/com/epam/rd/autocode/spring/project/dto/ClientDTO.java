package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.annotations.ValidPassword;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @CorrectName
    private String name;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }
}