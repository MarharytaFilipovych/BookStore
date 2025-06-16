package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;
import java.time.Year;

@Data
@NoArgsConstructor
public class SearchBookDTO {
    @Length(min = 1, max = 255, message = "Book name must be between 1 and 255 characters")
    private String name;

    @CorrectName(required = false, message = "Genre must contain only letters, spaces, hyphens, and apostrophes")
    private String genre;

    @CorrectEnum(enumClass = AgeGroup.class, message = "Invalid age group. Must be one of: CHILD, TEEN, ADULT, OTHER")
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

    @CorrectName(required = false)
    private String author;

    @Min(value = 1, message = "Number of pages must be at least 1")
    @JsonProperty("min_pages")
    private Integer minPages;

    @Min(value = 1, message = "Number of pages must be at least 1")
    @JsonProperty("max_pages")
    private Integer maxPages;

    @CorrectEnum(enumClass = Language.class, message = "Invalid language. Must be one of: ENGLISH, SPANISH, FRENCH, GERMAN, UKRAINIAN, JAPANESE, OTHER")
    private Language language;
}
