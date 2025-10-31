package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.enums.MenuCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MenuItemRequestDTO(
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be less than 200 characters")
    String name,
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    String description,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be > 0")
    @DecimalMax(value = "999999.99", message = "Price must be <= 999999.99")
    BigDecimal price,
    
    @NotNull(message = "Category is required")
    MenuCategory category,
    
    String imageUrl,
    Boolean isAvailable,
    Boolean isVegetarian,
    Boolean isVegan,
    Boolean isGlutenFree,
    Boolean isSpicy,
    
    @Min(value = 0, message = "Preparation time must be >= 0")
    Integer preparationTime
) {}

