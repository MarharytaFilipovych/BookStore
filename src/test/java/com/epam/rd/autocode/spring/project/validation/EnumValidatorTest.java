package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnumValidatorTest {

    @Mock
    private CorrectEnum annotation;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private EnumValidator validator;

    @BeforeEach
    void setUp() {
        when(annotation.enumClass()).thenReturn((Class) AgeGroup.class);
        validator.initialize(annotation);
    }

    @Test
    void isValid_WithNullValue_ShouldReturnFalse() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @EnumSource(AgeGroup.class)
    void isValid_WithCorrectEnumType_ShouldReturnTrue(AgeGroup ageGroup) {
        // Act
        boolean result = validator.isValid(ageGroup, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void isValid_WithDifferentEnumType_ShouldReturnFalse(Language language) {
        // Act
        boolean result = validator.isValid(language, context);

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void isValid_WithAnotherEnumType_ShouldReturnFalse(Role role) {
        // Act
        boolean result = validator.isValid(role, context);

        // Assert
        assertFalse(result);
    }
}