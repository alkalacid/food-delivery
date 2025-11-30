package com.fooddelivery.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourierRequestDTO(
    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must be less than 50 characters")
    String vehicleType,
    
    @Size(max = 50, message = "Vehicle number must be less than 50 characters")
    String vehicleNumber
) {}

