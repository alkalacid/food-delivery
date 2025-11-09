package com.fooddelivery.order.enums;

/**
 * Order status enum representing order lifecycle states.
 * Flow: CREATED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
 * Can also transition to CANCELLED at any time
 */
public enum OrderStatus {
    /**
     * Order created by customer, waiting for restaurant confirmation
     */
    CREATED,
    
    /**
     * Restaurant confirmed the order
     */
    CONFIRMED,
    
    /**
     * Restaurant is preparing the order
     */
    PREPARING,
    
    /**
     * Order is ready for pickup by courier
     */
    READY,
    
    /**
     * Order picked up by courier, on the way to customer
     */
    PICKED_UP,
    
    /**
     * Order delivered to customer
     */
    DELIVERED,
    
    /**
     * Order cancelled (by customer, restaurant, or system)
     */
    CANCELLED
}

