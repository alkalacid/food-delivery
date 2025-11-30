package com.fooddelivery.delivery.entity;

import com.fooddelivery.delivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Delivery entity tracking order delivery
 */
@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_deliveries_order_id", columnList = "orderId"),
    @Index(name = "idx_deliveries_courier_id", columnList = "courier_id"),
    @Index(name = "idx_deliveries_status", columnList = "status"),
    @Index(name = "idx_deliveries_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "orderId")
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Reference to order in order-service
     */
    @Column(nullable = false, unique = true)
    private Long orderId;
    
    /**
     * Customer user ID (from order)
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Assigned courier
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;
    
    /**
     * Current delivery status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;
    
    /**
     * Restaurant location (latitude)
     */
    @Column(nullable = false)
    private Double pickupLatitude;
    
    /**
     * Restaurant location (longitude)
     */
    @Column(nullable = false)
    private Double pickupLongitude;
    
    /**
     * Delivery destination (latitude)
     */
    @Column(nullable = false)
    private Double deliveryLatitude;
    
    /**
     * Delivery destination (longitude)
     */
    @Column(nullable = false)
    private Double deliveryLongitude;
    
    /**
     * Estimated distance in meters
     */
    @Column
    private Integer estimatedDistanceMeters;
    
    /**
     * Estimated delivery time in minutes
     */
    @Column
    private Integer estimatedTimeMinutes;
    
    /**
     * When courier was assigned
     */
    @Column
    private LocalDateTime assignedAt;
    
    /**
     * When courier picked up the order
     */
    @Column
    private LocalDateTime pickedUpAt;
    
    /**
     * When order was delivered
     */
    @Column
    private LocalDateTime deliveredAt;
    
    /**
     * Delivery notes (special instructions)
     */
    @Column(length = 500)
    private String notes;
    
    /**
     * Customer rating for delivery (1-5)
     */
    @Column
    private Integer rating;
    
    /**
     * Customer feedback
     */
    @Column(length = 500)
    private String feedback;
    
    /**
     * When delivery was created
     */
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DeliveryStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Assign courier to delivery
     */
    public void assignCourier(Courier courier) {
        this.courier = courier;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }
    
    /**
     * Mark as picked up
     */
    public void markPickedUp() {
        this.status = DeliveryStatus.IN_TRANSIT;
        this.pickedUpAt = LocalDateTime.now();
    }
    
    /**
     * Mark as delivered
     */
    public void markDelivered() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    /**
     * Mark as cancelled
     */
    public void markCancelled() {
        this.status = DeliveryStatus.CANCELLED;
    }

    /**
     * Add customer rating
     */
    public void addRating(Integer rating, String feedback) {
        this.rating = rating;
        this.feedback = feedback;
    }
}

