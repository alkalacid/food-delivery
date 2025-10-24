package com.fooddelivery.common.kafka;

/**
 * Centralized Kafka consumer group IDs
 */
public final class KafkaConsumerGroups {
    
    private KafkaConsumerGroups() {
        // Utility class
    }
    
    public static final String ORDER_SERVICE = "order-service-group";
    public static final String DELIVERY_SERVICE = "delivery-service-group";
    public static final String NOTIFICATION_SERVICE = "notification-service-group";
}

