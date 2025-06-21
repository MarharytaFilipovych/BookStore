package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.annotations.ValidPassword;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword(message = "Password does not meet security requirements")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Name is required")
    @CorrectName(message = "Name must contain only letters, spaces, hyphens, and apostrophes")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+\\d{1,4}\\d{6,14}$",
            message = "Telephone must be in format +[country code][number] (e.g., +380123456789)")
    private String phone;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    @JsonProperty("birthdate")
    private LocalDate birthDate;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }
}