package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ReviewRequestDTO(
    @NotNull(message = "Order ID is required")
    Long orderId,
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be between 1 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 1 and 5")
    BigDecimal rating,
    
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    String comment
) {}

