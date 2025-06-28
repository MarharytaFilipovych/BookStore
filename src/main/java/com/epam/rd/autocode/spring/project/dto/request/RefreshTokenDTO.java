package com.epam.rd.autocode.spring.project.dto.request;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDTO {
    @JsonProperty("refresh_token")
    @NotNull(message = "Refresh token is required!")
    private UUID refreshToken;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please, provide a valid email.")
    private String email;

    @CorrectEnum(enumClass = Role.class)
    private Role role = Role.CLIENT;

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }
}