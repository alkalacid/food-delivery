package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.enums.CourierStatus;

import java.time.LocalDateTime;

public record CourierResponseDTO(
    Long id,
    Long userId,
    String vehicleType,
    String vehicleNumber,
    CourierStatus status,
    Double currentLatitude,
    Double currentLongitude,
    LocalDateTime lastLocationUpdate,
    Double averageRating,
    Integer totalDeliveries,
    LocalDateTime createdAt
) {}

