package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    @NotBlank(message = "Book name is required")
    @Length(min = 1, max = 255, message = "Book name must be between 1 and 255 characters")
    private String name;

    @CorrectName(message = "Genre must contain only letters, spaces, hyphens, and apostrophes")
    private String genre;

    @NotNull(message = "Age group is required")
    @CorrectEnum(enumClass = AgeGroup.class, message = "Invalid age group. Must be one of: CHILD, TEEN, ADULT, OTHER")
    private AgeGroup ageGroup;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Publication date is required")
    @Past(message = "Publication date must be in the past")
    private LocalDate publicationDate;

    @CorrectName
    private String author;

    @NotNull(message = "Number of pages is required")
    @Min(value = 1, message = "Number of pages must be at least 1")
    private Integer pages;

    @NotBlank(message = "Book characteristics are required")
    private String characteristics;

    @NotBlank(message = "Book description is required")
    private String description;

    @NotNull(message = "Language is required")
    @CorrectEnum(enumClass = Language.class, message = "Invalid language. Must be one of: ENGLISH, SPANISH, FRENCH, GERMAN, UKRAINIAN, JAPANESE, OTHER")
    private Language language;
}