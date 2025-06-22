package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ErrorResponseDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.OrderMustContainClientException;
import com.epam.rd.autocode.spring.project.exception.UserDetailsAreNullException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handleAlreadyExistException(AlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(OrderMustContainClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrderMustContainClientException(OrderMustContainClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(UserDetailsAreNullException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserDetailsAreNullException(UserDetailsAreNullException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Authentication error occurred"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Validation failed: " + errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Validation failed: " + errorMessage));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO("Data integrity constraint violation"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Invalid request body format"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Invalid parameter type for: " + ex.getName()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Such path url is not supported!");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        String errorMessage = ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Validation failed: " + errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("An unexpected error occurred"));
    }
}