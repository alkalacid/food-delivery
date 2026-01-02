package com.fooddelivery.notification.enums;

/**
 * Type of notification event
 */
public enum NotificationType {
    /**
     * Order status notifications
     */
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_PREPARING,
    ORDER_READY,
    ORDER_PICKED_UP,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    
    /**
     * Payment notifications
     */
    PAYMENT_COMPLETED,
    PAYMENT_PROCESSED,
    PAYMENT_FAILED,
    REFUND_PROCESSED,
    
    /**
     * Delivery notifications
     */
    DELIVERY_ASSIGNED,
    COURIER_ARRIVING,
    DELIVERY_DELAYED,
    DELIVERY_DELIVERED,
    
    /**
     * User notifications
     */
    WELCOME,
    PASSWORD_RESET,
    ACCOUNT_VERIFIED,
    
    /**
     * Promotional notifications
     */
    PROMO_CODE,
    SPECIAL_OFFER
}

