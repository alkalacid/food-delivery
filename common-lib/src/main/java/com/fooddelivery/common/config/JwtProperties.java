package com.fooddelivery.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe JWT configuration properties.
 * Binds to app.jwt.* properties from application.yml
 */
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Getter
@Setter
public class JwtProperties {
    
    private static final long MILLIS_TO_SECONDS = 1000L;
    private static final long DEFAULT_EXPIRATION_MS = 86400000L;
    
    @NotBlank(message = "JWT secret is required")
    private String secret;
    
    @Positive(message = "JWT expiration must be positive")
    private long expiration = DEFAULT_EXPIRATION_MS;
    
    private String issuer = "food-delivery-system";
    
    public long getExpirationSeconds() {
        return expiration / MILLIS_TO_SECONDS;
    }
}

