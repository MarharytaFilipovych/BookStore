package com.epam.rd.autocode.spring.project.dto.request;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class RefreshTokenDTO {
    @NotNull(message = "Refresh token is required!")
    private UUID refreshToken;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please, provide a valid email.")
    private String email;

    @CorrectEnum(enumClass = Role.class)
    private Role role = Role.CLIENT;
}