package com.fooddelivery.order.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Track promo code usage by users
 */
@Entity
@Table(name = "promo_code_usage", indexes = {
    @Index(name = "idx_usage_user_promo", columnList = "userId,promoCodeId"),
    @Index(name = "idx_usage_order", columnList = "orderId")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"userId", "promoCodeId", "orderId"})
public class PromoCodeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long promoCodeId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}

