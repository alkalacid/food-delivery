package com.fooddelivery.order.dto;

import com.fooddelivery.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderStatusDTO(
    @NotNull(message = "Status is required")
    OrderStatus status,
    
    @Size(max = 500, message = "Comment must be less than 500 characters")
    String comment
) {}

