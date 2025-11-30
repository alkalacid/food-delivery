package com.fooddelivery.delivery.enums;

/**
 * Status of delivery
 */
public enum DeliveryStatus {
    /**
     * Delivery is pending courier assignment
     */
    PENDING,
    
    /**
     * Courier has been assigned
     */
    ASSIGNED,
    
    /**
     * Courier has picked up and is on the way to customer
     */
    IN_TRANSIT,
    
    /**
     * Order has been delivered to customer
     */
    DELIVERED,
    
    /**
     * Delivery was cancelled
     */
    CANCELLED,
    
    /**
     * Delivery failed
     */
    FAILED
}

