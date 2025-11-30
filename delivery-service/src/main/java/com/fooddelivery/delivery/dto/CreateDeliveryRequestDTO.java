package com.fooddelivery.delivery.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDeliveryRequestDTO(
    @NotNull(message = "Order ID is required")
    Long orderId,
    
    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double pickupLatitude,
    
    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double pickupLongitude,
    
    @NotNull(message = "Delivery latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double deliveryLatitude,
    
    @NotNull(message = "Delivery longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double deliveryLongitude,
    
    @Size(max = 500, message = "Notes must be less than 500 characters")
    String notes
) {}

