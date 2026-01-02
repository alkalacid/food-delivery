package com.fooddelivery.notification.enums;

/**
 * Status of notification delivery (EMAIL and SMS)
 */
public enum NotificationStatus {
    /**
     * Notification is queued for sending
     */
    PENDING,
    
    /**
     * Notification is being sent
     */
    SENDING,
    
    /**
     * Notification was sent successfully to the recipient
     */
    SENT,
    
    /**
     * Notification failed to send (will be retried if eligible)
     */
    FAILED
}

