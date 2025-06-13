package com.epam.rd.autocode.spring.project.dto;

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
public class OrderDTO{

    private String employeeEmail;

    private String clientEmail;

    private LocalDateTime orderDate;

    private BigDecimal price;

    private List<BookItemDTO> bookItems = new ArrayList<>();
}
