package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.BookTitle;
import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Year;

@Data
@NoArgsConstructor
public class SearchBookDTO {
    private String name;

    private String genre;

    @CorrectEnum(enumClass = AgeGroup.class, message = "Invalid age group. Must be one of: CHILD, TEEN, ADULT, OTHER", required = false)
    @JsonProperty("age_group")
    private AgeGroup ageGroup;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @JsonProperty("min_price")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @JsonProperty("max_price")
    private BigDecimal maxPrice;

    @Past(message = "Publication year must be in the past")
    @JsonProperty("publication_year")
    private Year publicationYear;

    private String author;

    @Min(value = 1, message = "Number of pages must be at least 1")
    @JsonProperty("min_pages")
    private Integer minPages;

    @Min(value = 1, message = "Number of pages must be at least 1")
    @JsonProperty("max_pages")
    private Integer maxPages;

    @CorrectEnum(required = false, enumClass = Language.class, message = "Invalid language. Must be one of: ENGLISH, SPANISH, FRENCH, GERMAN, UKRAINIAN, JAPANESE, OTHER")
    private Language language;
}
