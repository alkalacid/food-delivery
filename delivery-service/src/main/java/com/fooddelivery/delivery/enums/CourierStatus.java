package com.fooddelivery.delivery.enums;

/**
 * Status of courier availability
 */
public enum CourierStatus {
    /**
     * Courier is available to accept orders
     */
    AVAILABLE,
    
    /**
     * Courier is currently delivering an order
     */
    BUSY,
    
    /**
     * Courier is offline (not working)
     */
    OFFLINE,
    
    /**
     * Courier is on break
     */
    ON_BREAK
}

