package com.fooddelivery.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "app.pricing")
@Getter
@Setter
public class PricingProperties {
    
    private BigDecimal baseDeliveryFee = new BigDecimal("2.50");
    private BigDecimal deliveryFeePerKm = new BigDecimal("0.50");
    private Integer basePreparationTime = 20;
    private Integer deliveryTimePerKm = 3;
}

