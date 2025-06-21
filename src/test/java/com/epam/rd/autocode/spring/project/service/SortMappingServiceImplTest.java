package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.conf.SortOptionsSettings;
import com.epam.rd.autocode.spring.project.service.impl.SortMappingServiceImpl;
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
class SortMappingServiceImplTest {

    @Mock
    private SortOptionsSettings sortOptionsSettings;

    @InjectMocks
    private SortMappingServiceImpl sortMappingService;

    private Map<String, String> testMappings;

    @BeforeEach
    void setUp() {
        testMappings = Map.of(
                "client_email", "client.email",
                "employee_email", "employee.email",
                "order_date", "orderDate",
                "publication_date", "publicationDate"
        );
    }

    @Test
    void applyMappings_WithNullPageable_ShouldReturnNull() {
        // Act
        Pageable result = sortMappingService.applyMappings(null, "order");

        // Assert
        assertNull(result);
        verify(sortOptionsSettings, never()).getEntityMappings(anyString());
    }

    @Test
    void applyMappings_WithUnsortedPageable_ShouldReturnOriginalPageable() {
        // Arrange
        Pageable unsortedPageable = PageRequest.of(0, 10);

        // Act
        Pageable result = sortMappingService.applyMappings(unsortedPageable, "order");

        // Assert
        assertEquals(unsortedPageable, result);
        verify(sortOptionsSettings, never()).getEntityMappings(anyString());
    }

    @Test
    void applyMappings_WithEmptyMappings_ShouldReturnOriginalPageable() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(sortOptionsSettings.getEntityMappings("book")).thenReturn(Map.of());

        // Act
        Pageable result = sortMappingService.applyMappings(pageable, "book");

        // Assert
        assertEquals(pageable, result);
        verify(sortOptionsSettings).getEntityMappings("book");
    }

    @Test
    void applyMappings_WithSingleMappedField_ShouldReturnMappedPageable() {
        // Arrange
        Pageable originalPageable = PageRequest.of(0, 10, Sort.by("order_date"));
        when(sortOptionsSettings.getEntityMappings("order")).thenReturn(testMappings);

        // Act
        Pageable result = sortMappingService.applyMappings(originalPageable, "order");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertFalse(result.getSort().isUnsorted());
        
        List<Sort.Order> orders = result.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("orderDate", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
    }

    @Test
    void applyMappings_WithUnmappedField_ShouldKeepOriginalField() {
        // Arrange
        Pageable originalPageable = PageRequest.of(0, 10, Sort.by("price"));
        when(sortOptionsSettings.getEntityMappings("order")).thenReturn(testMappings);

        // Act
        Pageable result = sortMappingService.applyMappings(originalPageable, "order");

        // Assert
        assertNotNull(result);
        List<Sort.Order> orders = result.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("price", orders.get(0).getProperty());
    }

    @Test
    void applyMappings_WithMultipleMixedFields_ShouldMapCorrectly() {
        // Arrange
        Sort multiSort = Sort.by("order_date").descending()
                .and(Sort.by("price").ascending())
                .and(Sort.by("client_email").descending());
        Pageable originalPageable = PageRequest.of(1, 20, multiSort);
        when(sortOptionsSettings.getEntityMappings("order")).thenReturn(testMappings);

        // Act
        Pageable result = sortMappingService.applyMappings(originalPageable, "order");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        
        List<Sort.Order> orders = result.getSort().toList();
        assertEquals(3, orders.size());
        
        assertEquals("orderDate", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());
        
        assertEquals("price", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
        
        assertEquals("client.email", orders.get(2).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(2).getDirection());
    }

    @Test
    void applyMappings_WithNullHandling_ShouldPreserveNullHandling() {
        // Arrange
        Sort.Order orderWithNullHandling = new Sort.Order(Sort.Direction.ASC, "order_date", Sort.NullHandling.NULLS_LAST);
        Pageable originalPageable = PageRequest.of(0, 10, Sort.by(orderWithNullHandling));
        when(sortOptionsSettings.getEntityMappings("order")).thenReturn(testMappings);

        // Act
        Pageable result = sortMappingService.applyMappings(originalPageable, "order");

        // Assert
        assertNotNull(result);
        List<Sort.Order> orders = result.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("orderDate", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals(Sort.NullHandling.NULLS_LAST, orders.get(0).getNullHandling());
    }

    @ParameterizedTest
    @ValueSource(strings = {"book", "client", "employee", "order"})
    void applyMappings_WithDifferentEntityTypes_ShouldCallCorrectMapping(String entityType) {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("test_field"));
        when(sortOptionsSettings.getEntityMappings(entityType)).thenReturn(Map.of("test_field", "mappedField"));

        // Act
        sortMappingService.applyMappings(pageable, entityType);

        // Assert
        verify(sortOptionsSettings).getEntityMappings(entityType);
    }
}