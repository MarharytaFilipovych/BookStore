package com.epam.rd.autocode.spring.project.conf;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "jwt")
@PropertySource("classpath:jwt.properties")
@Validated
@Getter
@Setter
public class JwtSettings {
    @NotBlank
    private String secretKey;

    @NotNull
    private Duration expirationTime;

    @NotBlank
    private String issuer;
}
