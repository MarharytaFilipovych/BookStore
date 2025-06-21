package com.epam.rd.autocode.spring.project.conf;

import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sort")
@PropertySource("classpath:sort.properties")
@Validated
@Setter
public class SortOptionsSettings {
    @NotNull
    private Map<String, List<String>> entities = new HashMap<>();

    @NotNull
    private Map<String, Map<String, String>> mappings = new HashMap<>();

    public List<String> getEntitySortOptions(String entityType) {
        return entityType == null || entityType.isEmpty()
                ? List.of()
                : List.copyOf(entities.getOrDefault(entityType, List.of()));
    }

    public Map<String, String> getEntityMappings(String entityType) {
        return entityType == null || entityType.isEmpty()
                ? Map.of()
                : Map.copyOf(mappings.getOrDefault(entityType, Map.of()));
    }
}
