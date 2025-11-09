package com.fooddelivery.order.dto;

import com.fooddelivery.order.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for promo codes
 */
public record PromoCodeResponseDTO(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Integer maxTotalUses,
        Integer currentUses,
        Integer maxUsesPerUser,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

