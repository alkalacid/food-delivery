package com.fooddelivery.user.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration properties for address management.
 * Binds to app.address.* properties from application.yml
 */
@ConfigurationProperties(prefix = "app.address")
@Validated
@Getter
@Setter
public class AddressProperties {
    
    /**
     * Maximum number of addresses allowed per user.
     * Default: 10
     */
    @Min(value = 1, message = "Max addresses per user must be at least 1")
    @Max(value = 100, message = "Max addresses per user must not exceed 100")
    private int maxPerUser = 10;
    
    /**
     * Whether to validate address coordinates
     * Default: true
     */
    private boolean validateCoordinates = true;
}

