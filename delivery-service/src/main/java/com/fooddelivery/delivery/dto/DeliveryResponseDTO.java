package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.enums.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponseDTO(
    Long id,
    Long orderId,
    CourierResponseDTO courier,
    DeliveryStatus status,
    Double pickupLatitude,
    Double pickupLongitude,
    Double deliveryLatitude,
    Double deliveryLongitude,
    Integer estimatedDistanceMeters,
    Integer estimatedTimeMinutes,
    LocalDateTime assignedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime deliveredAt,
    String notes,
    Integer rating,
    String feedback,
    LocalDateTime createdAt
) {}

