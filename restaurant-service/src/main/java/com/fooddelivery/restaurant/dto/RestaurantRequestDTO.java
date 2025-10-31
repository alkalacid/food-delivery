package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;

public record RestaurantRequestDTO(
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be less than 200 characters")
    String name,
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    String description,
    
    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be less than 500 characters")
    String address,
    
    String city,
    String postalCode,
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    BigDecimal latitude,
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    BigDecimal longitude,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone format (E.164 format expected)")
    String phone,
    
    @Email(message = "Invalid email format", regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    String email,
    
    @Size(max = 100, message = "Cuisine type must be less than 100 characters")
    String cuisineType,
    LocalTime openingTime,
    LocalTime closingTime,
    
    @DecimalMin(value = "0.0", message = "Minimum order must be >= 0")
    @DecimalMax(value = "999999.99", message = "Minimum order must be <= 999999.99")
    BigDecimal minimumOrder,
    
    @DecimalMin(value = "0.0", message = "Delivery fee must be >= 0")
    @DecimalMax(value = "9999.99", message = "Delivery fee must be <= 9999.99")
    BigDecimal deliveryFee,
    
    @Min(value = 0, message = "Estimated delivery time must be >= 0")
    Integer estimatedDeliveryTime,
    
    String imageUrl
) {}

