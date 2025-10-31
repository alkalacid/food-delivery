package com.fooddelivery.restaurant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewResponseDTO(
    Long id,
    Long restaurantId,
    Long userId,
    Long orderId,
    BigDecimal rating,
    String comment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

