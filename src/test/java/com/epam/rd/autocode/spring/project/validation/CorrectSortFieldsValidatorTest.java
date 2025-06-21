package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.conf.SortOptionsSettings;
import com.epam.rd.autocode.spring.project.model.enums.SortableEntity;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectSortFieldsValidatorTest {

    @Mock
    private SortOptionsSettings sortOptionsSettings;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private CorrectSortFields annotation;

    @InjectMocks
    private CorrectSortFieldsValidator validator;

    @BeforeEach
    void setUp() {
        when(annotation.entityType()).thenReturn(SortableEntity.BOOK);
        when(annotation.sortMappings()).thenReturn(new CorrectSortFields.SortMapping[0]);

        when(sortOptionsSettings.getEntitySortOptions("book"))
                .thenReturn(List.of("name", "author", "price", "publicationDate"));
        when(sortOptionsSettings.getEntityMappings("book"))
                .thenReturn(Map.of("publication_date", "publicationDate"));

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);

        validator.initialize(annotation);
    }

    @Test
    void isValid_WithNullPageable_ShouldReturnTrue() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_WithUnsortedPageable_ShouldReturnTrue() {
        // Arrange
        Pageable unsorted = PageRequest.of(0, 10);

        // Act
        boolean result = validator.isValid(unsorted, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "author", "price"})
    void isValid_WithValidSortFields_ShouldReturnTrue(String sortField) {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(sortField));

        // Act
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_WithMappedSortField_ShouldReturnTrue() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("publication_date"));

        // Act
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_field", "unknown", "nonexistent"})
    void isValid_WithInvalidSortFields_ShouldReturnFalse(String invalidField) {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(invalidField));

        // Act
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(
                contains("Invalid sort field: '" + invalidField + "'"));
    }

    @Test
    void isValid_WithMultipleSortFields_AllValid_ShouldReturnTrue() {
        // Arrange
        Sort multiSort = Sort.by("name").and(Sort.by("price")).and(Sort.by("author"));
        Pageable pageable = PageRequest.of(0, 10, multiSort);

        // Act
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_WithMultipleSortFields_OneInvalid_ShouldReturnFalse() {
        // Arrange
        Sort multiSort = Sort.by("name").and(Sort.by("invalid_field"));
        Pageable pageable = PageRequest.of(0, 10, multiSort);

        // Act
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void initialize_WithAnnotationMappings_ShouldCombineMappings() {
        // Arrange
        CorrectSortFields.SortMapping[] annotationMappings = new CorrectSortFields.SortMapping[1];
        CorrectSortFields.SortMapping mapping = mock(CorrectSortFields.SortMapping.class);
        when(mapping.from()).thenReturn("custom_field");
        when(mapping.to()).thenReturn("name");
        annotationMappings[0] = mapping;

        when(annotation.sortMappings()).thenReturn(annotationMappings);

        // Act
        validator.initialize(annotation);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("custom_field"));
        boolean result = validator.isValid(pageable, context);

        // Assert
        assertTrue(result);
    }
}