package com.fooddelivery.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddressResponseDTO(
    Long id,
    String addressLine,
    String city,
    String postalCode,
    String country,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isDefault,
    String label,
    LocalDateTime createdAt
) {}

