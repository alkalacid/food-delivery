package com.fooddelivery.user.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "user")
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 255)
    private String addressLine;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(length = 100)
    private String country;
    
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @Column(length = 50)
    private String label; // e.g., "Home", "Work", "Other"
    
    @Column(columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public UserAddress(User user, String addressLine, String city) {
        this.user = user;
        this.addressLine = addressLine;
        this.city = city;
    }
}

