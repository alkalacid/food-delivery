package com.fooddelivery.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderItemRequestDTO(
    @NotNull(message = "Menu item ID is required")
    Long menuItemId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,
    
    @Size(max = 200, message = "Special instructions must be less than 200 characters")
    String specialInstructions
) {}

