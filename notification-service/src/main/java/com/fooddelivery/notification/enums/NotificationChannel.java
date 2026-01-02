package com.fooddelivery.notification.enums;

/**
 * Communication channel for notifications
 */
public enum NotificationChannel {
    /**
     * Email notification (via SMTP - MailHog for local development)
     */
    EMAIL,
    
    /**
     * SMS notification (mock implementation for development)
     * In production: integrate with Twilio, AWS SNS, or similar service
     */
    SMS
}

