package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurants_owner_id", columnList = "owner_id"),
    @Index(name = "idx_restaurants_cuisine_type", columnList = "cuisine_type"),
    @Index(name = "idx_restaurants_is_active", columnList = "is_active"),
    @Index(name = "idx_restaurants_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"menuItems", "reviews"})
public class Restaurant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 100)
    private String cuisineType;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;
    
    @Column
    private Integer totalReviews = 0;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column
    private LocalTime openingTime;
    
    @Column
    private LocalTime closingTime;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal minimumOrder;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column
    private Integer estimatedDeliveryTime;
    
    @Column(length = 500)
    private String imageUrl;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (totalReviews == null) {
            totalReviews = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }
    
    public void updateRating(BigDecimal newRating) {
        if (this.averageRating == null) {
            this.averageRating = newRating;
            this.totalReviews = 1;
        } else {
            BigDecimal totalScore = this.averageRating.multiply(new BigDecimal(this.totalReviews));
            totalScore = totalScore.add(newRating);
            this.totalReviews++;
            this.averageRating = totalScore.divide(new BigDecimal(this.totalReviews), 2, RoundingMode.HALF_UP);
        }
    }
}

