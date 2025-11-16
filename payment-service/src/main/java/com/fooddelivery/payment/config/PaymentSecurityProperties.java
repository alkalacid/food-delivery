package com.fooddelivery.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for payment security and fraud detection
 */
@Configuration
@ConfigurationProperties(prefix = "app.payment.fraud-detection")
@Getter
@Setter
public class PaymentSecurityProperties {
    
    /**
     * Maximum number of failed payment attempts before blocking user
     */
    private int maxFailedAttempts = 3;
    
    /**
     * Time window in minutes for counting failed attempts
     */
    private int failureWindowMinutes = 30;
}

