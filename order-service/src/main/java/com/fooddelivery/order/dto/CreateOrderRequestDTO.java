package com.fooddelivery.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequestDTO(
    @NotNull(message = "Restaurant ID is required")
    Long restaurantId,
    
    @NotNull(message = "Delivery address ID is required")
    Long deliveryAddressId,
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequestDTO> items,
    
    @Size(max = 500, message = "Special instructions must be less than 500 characters")
    String specialInstructions,
    
    @Size(max = 50, message = "Promo code must be less than 50 characters")
    String promoCode
) {}

