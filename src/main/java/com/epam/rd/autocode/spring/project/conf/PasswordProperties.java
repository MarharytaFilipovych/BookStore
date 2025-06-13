package com.epam.rd.autocode.spring.project.conf;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@PropertySource("classpath:password.properties")
@Validated
@Getter
@Setter
public class PasswordProperties {

    @NotNull @Min(4) @Max(50)
    private Integer minLength = 8;

    @NotNull @Min(8) @Max(100)
    private Integer maxLength;

    @Min(0) @Max(10)
    private Integer minUppercase;

    @Min(0) @Max(10)
    private Integer minLowercase;

    @Min(0) @Max(10)
    private Integer minDigits ;

    @Min(0) @Max(10)
    private Integer minSpecial;

    @Min(1) @Max(10)
    private Integer maxRepeatChars;

    @Min(1) @Max(10)
    private Integer maxSequenceLength;

    private boolean allowWhitespace = false;

    private String[] commonPasswords = new String[0];
}