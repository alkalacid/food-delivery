package com.fooddelivery.common.kafka;

/**
 * Centralized Kafka topic names
 * Pattern: {service}.{entity}.{event}
 */
public final class KafkaTopics {
    
    private KafkaTopics() {
        // Utility class
    }
    
    // Order events
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_STATUS_CHANGED = "order.status.changed";
    
    // Payment events
    public static final String PAYMENT_PROCESSED = "payment.processed";
    public static final String PAYMENT_FAILED = "payment.failed";
    
    // Delivery events
    public static final String DELIVERY_ASSIGNED = "delivery.assigned";
    public static final String DELIVERY_DELIVERED = "delivery.delivered";
    
    // Dead Letter Queue (DLQ)
    public static final String DLQ_SUFFIX = ".dlq";
    
    public static String getDlqTopic(String originalTopic) {
        return originalTopic + DLQ_SUFFIX;
    }
}

