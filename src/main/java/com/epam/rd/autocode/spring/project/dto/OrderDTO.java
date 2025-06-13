package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @Email(message = "Please provide a valid employee email address")
    private String employeeEmail;

    @NotBlank(message = "Client email is required")
    @Email(message = "Please provide a valid client email address")
    private String clientEmail;

    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDateTime orderDate;

    @NotNull(message = "Order price is required")
    @DecimalMin(value = "0.01", message = "Order price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Book items list is required")
    @NotEmpty(message = "Order must contain at least one book item")
    @Valid
    private List<BookItemDTO> bookItems = new ArrayList<>();
}