package com.fooddelivery.order.entity;

import com.fooddelivery.order.enums.DiscountType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Promo code entity for order discounts
 */
@Entity
@Table(name = "promo_codes", indexes = {
    @Index(name = "idx_promo_code", columnList = "code", unique = true),
    @Index(name = "idx_promo_active", columnList = "active"),
    @Index(name = "idx_promo_valid_dates", columnList = "validFrom,validUntil")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "code")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    /**
     * Discount value (percentage or fixed amount depending on discountType)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * Minimum order amount to use this promo code
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * Maximum discount amount (for percentage discounts)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    /**
     * Valid from date
     */
    @Column(nullable = false)
    private LocalDateTime validFrom;

    /**
     * Valid until date
     */
    @Column(nullable = false)
    private LocalDateTime validUntil;

    /**
     * Maximum total uses across all users (null = unlimited)
     */
    @Column
    private Integer maxTotalUses;

    /**
     * Current usage count
     */
    @Column(nullable = false)
    private Integer currentUses = 0;

    /**
     * Maximum uses per user (null = unlimited)
     */
    @Column
    private Integer maxUsesPerUser;

    /**
     * Is this promo code active
     */
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if promo code is currently valid
     */
    public boolean isValid() {
        if (!active || deletedAt != null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }

        return maxTotalUses == null || currentUses < maxTotalUses;
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.currentUses++;
    }
}

