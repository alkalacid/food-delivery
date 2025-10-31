package com.fooddelivery.restaurant.entity;

import com.fooddelivery.restaurant.enums.MenuCategory;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items", indexes = {
    @Index(name = "idx_menu_items_restaurant_id", columnList = "restaurant_id"),
    @Index(name = "idx_menu_items_category", columnList = "category"),
    @Index(name = "idx_menu_items_is_available", columnList = "is_available")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "restaurant")
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MenuCategory category;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(nullable = false)
    private Boolean isAvailable = true;
    
    @Column
    private Boolean isVegetarian = false;
    
    @Column
    private Boolean isVegan = false;
    
    @Column
    private Boolean isGlutenFree = false;
    
    @Column
    private Boolean isSpicy = false;
    
    @Column
    private Integer preparationTime;
    
    @Column(columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAvailable == null) {
            isAvailable = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

