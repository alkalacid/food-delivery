package com.fooddelivery.user.dto;

import com.fooddelivery.user.enums.UserRole;
import com.fooddelivery.user.enums.UserStatus;

import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String email,
    String phone,
    UserRole role,
    UserStatus status,
    String firstName,
    String lastName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

