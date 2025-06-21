package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.conf.SortOptionsSettings;
import com.epam.rd.autocode.spring.project.service.SortMappingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SortMappingServiceImpl implements SortMappingService {
    
    private final SortOptionsSettings sortOptionsSettings;
    
    public SortMappingServiceImpl(SortOptionsSettings sortOptionsSettings) {
        this.sortOptionsSettings = sortOptionsSettings;
    }

    public Pageable applyMappings(Pageable pageable, String entityType) {
        if (pageable == null || pageable.getSort().isUnsorted()) return pageable;
        
        Map<String, String> mappings = sortOptionsSettings.getEntityMappings(entityType);
        if (mappings.isEmpty()) return pageable;
        
        Sort mappedSort = mapSort(pageable.getSort(), mappings);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
    }

    private Sort mapSort(Sort originalSort, Map<String, String> mappings) {
        List<Sort.Order> mappedOrders = originalSort.stream()
                .map(order -> {
                    String originalProperty = order.getProperty();
                    String mappedProperty = mappings.getOrDefault(originalProperty, originalProperty);
                    return new Sort.Order(order.getDirection(), mappedProperty, order.getNullHandling());
                })
                .collect(Collectors.toList());

        return Sort.by(mappedOrders);
    }
}