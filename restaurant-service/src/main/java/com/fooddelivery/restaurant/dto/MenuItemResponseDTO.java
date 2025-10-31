package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.enums.MenuCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuItemResponseDTO(
    Long id,
    Long restaurantId,
    String name,
    String description,
    BigDecimal price,
    MenuCategory category,
    String imageUrl,
    Boolean isAvailable,
    Boolean isVegetarian,
    Boolean isVegan,
    Boolean isGlutenFree,
    Boolean isSpicy,
    Integer preparationTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

