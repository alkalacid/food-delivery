package com.fooddelivery.delivery.entity;

import com.fooddelivery.delivery.enums.CourierStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Courier entity for delivery personnel
 */
@Entity
@Table(name = "couriers", indexes = {
    @Index(name = "idx_couriers_status", columnList = "status"),
    @Index(name = "idx_couriers_user_id", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "userId")
public class Courier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Reference to user in user-service
     */
    @Column(nullable = false, unique = true)
    private Long userId;
    
    /**
     * Courier's vehicle type
     */
    @Column(length = 50)
    private String vehicleType;
    
    /**
     * Vehicle registration number
     */
    @Column(length = 50)
    private String vehicleNumber;
    
    /**
     * Current status of courier
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourierStatus status;
    
    /**
     * Current latitude (updated in Redis, stored here for reference)
     */
    @Column
    private Double currentLatitude;
    
    /**
     * Current longitude (updated in Redis, stored here for reference)
     */
    @Column
    private Double currentLongitude;
    
    /**
     * Last location update timestamp
     */
    @Column
    private LocalDateTime lastLocationUpdate;
    
    /**
     * Average rating from customers
     */
    @Column(precision = 3, scale = 2)
    private Double averageRating = 0.0;
    
    /**
     * Total number of completed deliveries
     */
    @Column
    private Integer totalDeliveries = 0;
    
    /**
     * When courier was registered
     */
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    /**
     * Soft delete timestamp
     */
    @Column
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CourierStatus.OFFLINE;
        }
        if (averageRating == null) {
            averageRating = 0.0;
        }
        if (totalDeliveries == null) {
            totalDeliveries = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update courier location
     */
    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.lastLocationUpdate = LocalDateTime.now();
    }
    
    /**
     * Update status
     */
    public void updateStatus(CourierStatus newStatus) {
        this.status = newStatus;
    }
    
    /**
     * Update rating after delivery
     */
    public void updateRating(Double newRating) {
        if (this.totalDeliveries == 0) {
            this.averageRating = newRating;
        } else {
            // Calculate new average
            double totalRating = this.averageRating * this.totalDeliveries;
            this.averageRating = (totalRating + newRating) / (this.totalDeliveries + 1);
        }
        this.totalDeliveries++;
    }
    
    /**
     * Soft delete
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = CourierStatus.OFFLINE;
    }
}

