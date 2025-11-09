package com.fooddelivery.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
    Long id,
    Long menuItemId,
    String menuItemName,
    Integer quantity,
    BigDecimal price,
    BigDecimal subtotal,
    String specialInstructions
) {}

