package com.fooddelivery.order.dto;

import com.fooddelivery.order.enums.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating/updating promo codes
 */
public record PromoCodeRequestDTO(
        @NotBlank(message = "Code is required")
        @Size(max = 50, message = "Code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be positive")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "Min order amount must be non-negative")
        BigDecimal minOrderAmount,

        @DecimalMin(value = "0.00", message = "Max discount amount must be non-negative")
        BigDecimal maxDiscountAmount,

        @NotNull(message = "Valid from date is required")
        @FutureOrPresent(message = "Valid from must be present or future")
        LocalDateTime validFrom,

        @NotNull(message = "Valid until date is required")
        @Future(message = "Valid until must be in the future")
        LocalDateTime validUntil,

        @Min(value = 1, message = "Max total uses must be at least 1")
        Integer maxTotalUses,

        @Min(value = 1, message = "Max uses per user must be at least 1")
        Integer maxUsesPerUser,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}

