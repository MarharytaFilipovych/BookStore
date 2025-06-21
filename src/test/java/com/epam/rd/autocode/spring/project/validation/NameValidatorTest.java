package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NameValidatorTest {

    @Mock
    private CorrectName annotation;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private NameValidator validator;

    @BeforeEach
    void setUp() {
        when(annotation.required()).thenReturn(true);
        validator.initialize(annotation);
    }

    @Test
    void isValid_WithNullValueAndRequired_ShouldReturnFalse() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_WithNullValueAndNotRequired_ShouldReturnTrue() {
        // Arrange
        when(annotation.required()).thenReturn(false);
        validator.initialize(annotation);

        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void isValid_WithBlankValues_ShouldReturnFalse(String name) {
        // Act
        boolean result = validator.isValid(name, context);

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "John",
            "Mary Jane",
            "O'Connor",
            "Jean-Pierre",
            "Smith Jr.",
            "José María",
            "François",
            "Van Der Berg",
            "D'Angelo",
            "Марія Петрова",
            "张三",
            "  John  ",
            "\tMary\t",
            "\nJose\n"
    })
    void isValid_WithValidNames_ShouldReturnTrue(String name) {
        // Act
        boolean result = validator.isValid(name, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "A",                    // Too short (1 char)
            "John123",              // Contains numbers
            "John@Doe",             // Contains special chars
            "John_Doe",             // Contains underscore
            "John#Smith",           // Contains hash
            "John$Dollar",          // Contains dollar sign
            "John%Percent"          // Contains percent
    })
    void isValid_WithInvalidNames_ShouldReturnFalse(String name) {
        // Act
        boolean result = validator.isValid(name, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_WithTooLongName_ShouldReturnFalse() {
        // Arrange
        String longName = "A".repeat(101);

        // Act
        boolean result = validator.isValid(longName, context);

        // Assert
        assertFalse(result);
    }
}