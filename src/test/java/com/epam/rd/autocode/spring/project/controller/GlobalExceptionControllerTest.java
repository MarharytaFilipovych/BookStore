package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ErrorResponseDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.OrderMustContainClientException;
import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionControllerTest {

    private GlobalExceptionController globalExceptionController;

    @BeforeEach
    void setUp() {
        globalExceptionController = new GlobalExceptionController();
    }

    private void verifyErrorMessage(String expectedMessage, ResponseEntity<ErrorResponseDTO> response, int expectedStatusCode){
        assertEquals(expectedStatusCode, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().message());
    }

    // Test individual not found exceptions
    @Test
    void handleNotFoundException_ShouldReturn404WithErrorMessage() {
        // Arrange
        NotFoundException exception = new NotFoundException("Resource not found");

        // Act
        ResponseEntity<String> response = globalExceptionController.handleNotFoundExceptions(exception);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(exception.getMessage(), response.getBody());
    }

    @Test
    void handleNoHandlerFoundException_ShouldReturn404WithErrorMessage() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/unknown", new HttpHeaders());

        // Act
        ResponseEntity<String> response = globalExceptionController.handleNotFoundExceptions(exception);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(exception.getMessage(), response.getBody());
    }

    // Test conflict exceptions
    @Test
    void handleDataIntegrityViolationException_ShouldReturn409WithErrorMessage() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint violation");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleConflictExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 409);
    }

    @Test
    void handleAlreadyExistException_ShouldReturn409WithErrorMessage() {
        // Arrange
        AlreadyExistException exception = new AlreadyExistException("Resource already exists");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleConflictExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 409);
    }

    // Test bad request exceptions
    @Test
    void handleIllegalArgumentException_ShouldReturn400WithErrorMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleBadRequestExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 400);
    }

    @Test
    void handleOrderMustContainClientException_ShouldReturn400WithErrorMessage() {
        // Arrange
        OrderMustContainClientException exception = new OrderMustContainClientException();

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleBadRequestExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 400);
    }

    // Test unauthorized exceptions
    @Test
    void handleUsernameNotFoundException_ShouldReturn401WithErrorMessage() {
        // Arrange
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleUnauthorizedExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 401);
    }

    @Test
    void handleSecurityException_ShouldReturn401WithErrorMessage() {
        // Arrange
        SecurityException exception = new SecurityException("Invalid or expired token");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleUnauthorizedExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 401);
    }

    @Test
    void handleUserDetailsAreNullException_ShouldReturn401WithErrorMessage() {
        // Arrange
        UserDetailsAreNullException exception = new UserDetailsAreNullException("User details are null");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleUnauthorizedExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 401);
    }

    @Test
    void handleAccessDeniedException_ShouldReturn403WithErrorMessage() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleAccessDeniedException(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 403);
    }

    @Test
    void handleValidationException_ShouldReturn400WithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "field1", "Field1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field2 must be positive");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleValidationException(exception);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Validation failed"));
        assertTrue(response.getBody().message().contains(fieldError1.getField() + ": " + fieldError1.getDefaultMessage()));
        assertTrue(response.getBody().message().contains(fieldError2.getField() + ": " + fieldError2.getDefaultMessage()));
    }

    @Test
    void handleHandlerMethodValidationException_ShouldReturn400WithValidationErrors() {
        // Arrange
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);

        ParameterValidationResult result1 = mock(ParameterValidationResult.class);
        ParameterValidationResult result2 = mock(ParameterValidationResult.class);

        MessageSourceResolvable error1 = mock(MessageSourceResolvable.class);
        MessageSourceResolvable error2 = mock(MessageSourceResolvable.class);
        MessageSourceResolvable error3 = mock(MessageSourceResolvable.class);

        when(error1.getDefaultMessage()).thenReturn("must not be null");
        when(error2.getDefaultMessage()).thenReturn("must be greater than 0");
        when(error3.getDefaultMessage()).thenReturn("size must be between 1 and 100");

        when(result1.getResolvableErrors()).thenReturn(Arrays.asList(error1, error2));
        when(result2.getResolvableErrors()).thenReturn(List.of(error3));

        when(exception.getAllValidationResults()).thenReturn(Arrays.asList(result1, result2));

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleHandlerMethodValidationException(exception);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());

        String expectedMessage = "Validation failed: " + error1.getDefaultMessage() +
                "; " + error2.getDefaultMessage() + "; " + error3.getDefaultMessage();
        assertEquals(expectedMessage, response.getBody().message());
    }

    @Test
    void handleConstraintViolationException_ShouldReturn400WithViolationMessages() {
        // Arrange
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        when(violation1.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation1.getPropertyPath().toString()).thenReturn("field1");
        when(violation1.getMessage()).thenReturn("must not be null");

        when(violation2.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation2.getPropertyPath().toString()).thenReturn("field2");
        when(violation2.getMessage()).thenReturn("must be greater than 0");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Arrays.asList(violation1, violation2));
        when(exception.getConstraintViolations()).thenReturn(violations);

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleConstraintViolationException(exception);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Validation failed"));
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturn400WithGenericMessage() {
        // Arrange
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleBadRequestExceptions(exception);

        // Assert
        verifyErrorMessage(exception.getMessage(), response, 400);
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ShouldReturn400WithParameterName() {
        // Arrange
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("userId");

        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleMethodArgumentTypeMismatchException(exception);

        // Assert
        verifyErrorMessage("Invalid parameter type for: userId", response, 400);
    }

    @Test
    void handleGenericException_ShouldReturn500WithGenericMessage() {
        // Act
        ResponseEntity<ErrorResponseDTO> response = globalExceptionController.handleGenericException();

        // Assert
        verifyErrorMessage("An unexpected error occurred", response, 500);
    }
}