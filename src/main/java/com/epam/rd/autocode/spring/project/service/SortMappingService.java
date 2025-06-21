package com.epam.rd.autocode.spring.project.service;

import org.springframework.data.domain.Pageable;

public interface SortMappingService {
    Pageable applyMappings(Pageable pageable, String entityType);
}
