package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.BookTitle;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookTitleValidatorTest {

    @Mock
    private BookTitle bookTitleAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    private BookTitleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BookTitleValidator();
    }

    @Test
    void isValid_WithNullAndRequiredTrue_ShouldReturnFalse() {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(true);
        validator.initialize(bookTitleAnnotation);

        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_WithNullAndRequiredFalse_ShouldReturnTrue() {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(false);
        validator.initialize(bookTitleAnnotation);

        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    void isValid_WithBlankStrings_ShouldReturnFalse(String title) {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(true);
        validator.initialize(bookTitleAnnotation);

        // Act
        boolean result = validator.isValid(title, context);

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Valid Book Title",
            "A",
            "Book",
            "The Great Adventure of Learning Java Programming",
            "Book Title: A Journey Through C++ & Java! (2024)",
            "książka 📚 تاب کتاب 本"
    })
    void isValid_WithValidTitles_ShouldReturnTrue(String title) {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(true);
        validator.initialize(bookTitleAnnotation);

        // Act
        boolean result = validator.isValid(title, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_WithExactly255Characters_ShouldReturnTrue() {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(true);
        validator.initialize(bookTitleAnnotation);
        String title255 = "A".repeat(255);

        // Act
        boolean result = validator.isValid(title255, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_With256Characters_ShouldReturnFalse() {
        // Arrange
        when(bookTitleAnnotation.required()).thenReturn(true);
        validator.initialize(bookTitleAnnotation);
        String title256 = "A".repeat(256);

        // Act
        boolean result = validator.isValid(title256, context);

        // Assert
        assertFalse(result);
    }
}