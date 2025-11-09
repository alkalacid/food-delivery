package com.fooddelivery.order.dto;

import com.fooddelivery.order.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderHistoryResponseDTO(
    Long id,
    OrderStatus status,
    Long changedBy,
    String comment,
    LocalDateTime changedAt
) {}

