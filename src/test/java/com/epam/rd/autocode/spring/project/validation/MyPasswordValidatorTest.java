package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.conf.PasswordSettings;
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
class MyPasswordValidatorTest {

    @Mock
    private PasswordSettings passwordSettings;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @InjectMocks
    private MyPasswordValidator validator;

    @BeforeEach
    void setUp() {
        lenient().when(passwordSettings.getMinLength()).thenReturn(8);
        lenient().when(passwordSettings.getMaxLength()).thenReturn(100);
        lenient().when(passwordSettings.getMinUppercase()).thenReturn(1);
        lenient().when(passwordSettings.getMinLowercase()).thenReturn(1);
        lenient().when(passwordSettings.getMinDigits()).thenReturn(1);
        lenient().when(passwordSettings.getMinSpecial()).thenReturn(1);
        lenient().when(passwordSettings.getMaxRepeatChars()).thenReturn(2);
        lenient().when(passwordSettings.getMaxSequenceLength()).thenReturn(3);
        lenient().when(passwordSettings.isAllowWhitespace()).thenReturn(false);
        lenient().when(passwordSettings.getCommonPasswords()).thenReturn(new String[]{
                "123456", "123456789", "abc123", "password", "qwerty"
        });

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void isValid_WithNullEmptyOrBlankPasswords_ShouldReturnFalse(String password) {
        // Act
        boolean result = validator.isValid(password, context);

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ValidP@ss1",
            "SecureP@ss126",
            "MyStr0ng#Password",
            "TestP@ssw0rd!"
    })
    void isValid_WithValidPasswords_ShouldReturnTrue(String password) {
        // Act
        boolean result = validator.isValid(password, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",              // Too short (7 chars, min 8)
            "password",             // Common password
            "123456",               // Common password + too short
            "qwerty",               // Common password + too short
            "NoDigits!",            // Missing digits
            "nouppercase1!",        // Missing uppercase
            "NOLOWERCASE1!",        // Missing lowercase
            "NoSpecialChar1",       // Missing special character
            "Pass word1!",          // Contains whitespace
            "Passworddd1!",         // Repeats character (more than 2)
            "Password123!",         // Sequential characters (123)
            "PasswordABC!",         // Sequential characters (ABC)
            "Passwordqwer!"         // Sequential keyboard (qwer)
    })
    void isValid_WithInvalidPasswords_ShouldReturnFalse(String password) {
        // Act
        boolean result = validator.isValid(password, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_WithTooLongPassword_ShouldReturnFalse() {
        String longPassword = "A!".repeat(35); // 105 characters

        // Act
        boolean result = validator.isValid(longPassword, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_WithAllowedWhitespace_ShouldReturnTrue() {
        // Arrange
        when(passwordSettings.isAllowWhitespace()).thenReturn(true);
        String passwordWithSpace = "Pass w0rd1!";

        // Act
        boolean result = validator.isValid(passwordWithSpace, context);

        // Assert
        assertTrue(result);
    }
}