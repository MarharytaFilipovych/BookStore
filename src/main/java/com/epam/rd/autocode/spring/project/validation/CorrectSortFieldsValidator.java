package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.conf.SortOptionsSettings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CorrectSortFieldsValidator implements ConstraintValidator<CorrectSortFields, Pageable> {
    private Set<String> sortOptions;
    private Map<String, String> sortMappings;
    private final SortOptionsSettings sortOptionsSettings;

    public CorrectSortFieldsValidator(SortOptionsSettings sortOptionsSettings) {
        this.sortOptionsSettings = sortOptionsSettings;
    }

    private Map<String, String> parseSortMappings(CorrectSortFields.SortMapping[] mappingArray){
        Map<String, String> mappings = new HashMap<>();
        for(CorrectSortFields.SortMapping mapping : mappingArray){
            mappings.put(mapping.from(), mapping.to());
        }
        return mappings;
    }

    @Override
    public void initialize(CorrectSortFields constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        String entityType = constraintAnnotation.entityType().toString();
        this.sortOptions = new HashSet<>(sortOptionsSettings.getEntitySortOptions(entityType));
        Map<String, String> propertiesMappings = sortOptionsSettings.getEntityMappings(entityType);
        Map<String, String> annotationMappings = parseSortMappings(constraintAnnotation.sortMappings());
        this.sortMappings = new HashMap<>(propertiesMappings);
        this.sortMappings.putAll(annotationMappings);
    }

    @Override
    public boolean isValid(Pageable pageable, ConstraintValidatorContext context) {
        if (pageable == null || pageable.getSort().isUnsorted()) return true;

        Sort sort = pageable.getSort();

        if (sort.isUnsorted()) return true;
        for(Sort.Order order : sort){
            String requestedField = order.getProperty();

            String entityField = sortMappings.getOrDefault(requestedField, requestedField);

            if(!sortOptions.contains(entityField)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Invalid sort field: '" + order.getProperty() +
                                "'. Allowed fields: " + sortMappings.keySet()
                ).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
