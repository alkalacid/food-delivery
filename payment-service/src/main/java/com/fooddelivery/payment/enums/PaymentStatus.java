package com.fooddelivery.payment.enums;

/**
 * Payment status enum
 */
public enum PaymentStatus {
    /**
     * Payment initiated but not yet processed
     */
    PENDING,
    
    /**
     * Payment successfully processed
     */
    COMPLETED,
    
    /**
     * Payment failed
     */
    FAILED,
    
    /**
     * Payment refunded
     */
    REFUNDED
}

