package com.fooddelivery.restaurant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RestaurantResponseDTO(
    Long id,
    String name,
    String description,
    Long ownerId,
    String address,
    String city,
    String postalCode,
    BigDecimal latitude,
    BigDecimal longitude,
    String phone,
    String email,
    String cuisineType,
    BigDecimal averageRating,
    Integer totalReviews,
    Boolean isActive,
    LocalTime openingTime,
    LocalTime closingTime,
    BigDecimal minimumOrder,
    BigDecimal deliveryFee,
    Integer estimatedDeliveryTime,
    String imageUrl,
    LocalDateTime createdAt
) {}

