package com.fooddelivery.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AddressRequestDTO(
    @NotBlank(message = "Address line is required")
    String addressLine,
    
    @NotBlank(message = "City is required")
    String city,
    
    String postalCode,
    String country,
    
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    BigDecimal latitude,
    
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    BigDecimal longitude,
    
    Boolean isDefault,
    String label
) {}

