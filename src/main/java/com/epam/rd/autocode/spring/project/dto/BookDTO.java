package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.BookTitle;
import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    @BookTitle
    private String name;

    @CorrectName(message = "Genre must contain only letters, spaces, hyphens, and apostrophes")
    private String genre;

    @NotNull(message = "Age group is required")
    @CorrectEnum(enumClass = AgeGroup.class, message = "Invalid age group. Must be one of: CHILD, TEEN, ADULT, OTHER")
    @JsonProperty("age_group")
    private AgeGroup ageGroup;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Publication date is required")
    @Past(message = "Publication date must be in the past")
    @JsonProperty("publication_date")
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

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setGenre(String genre) {
        this.genre = genre != null ? genre.trim() : null;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : null;
    }
}