package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", 
       indexes = {
           @Index(name = "idx_reviews_restaurant_id", columnList = "restaurant_id"),
           @Index(name = "idx_reviews_user_id", columnList = "user_id"),
           @Index(name = "idx_reviews_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_reviews_user_order", columnNames = {"user_id", "order_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "restaurant")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;
    
    @Column(length = 2000)
    private String comment;
    
    @Column(columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

