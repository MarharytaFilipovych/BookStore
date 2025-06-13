package com.epam.rd.autocode.spring.project.conf;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "constants")
@Data
@Validated
public  class Constants {
    @Min(2)
    @Max(100)
    private Integer maxNameLength;

    @Min(1)
    @Max(255)
    private Integer maxBookTitleLength;

    @Min(1)
    @Max(100)
    private Integer maxPageSize;
}
