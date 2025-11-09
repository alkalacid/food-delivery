package com.fooddelivery.order.kafka;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.event.OrderStatusChangedEvent;
import com.fooddelivery.common.kafka.BaseKafkaEventProducer;
import com.fooddelivery.common.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event producer for order events
 */
@Component
@Slf4j
public class OrderEventProducer extends BaseKafkaEventProducer {
    
    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    /**
     * Publish order created event
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        publishEvent(
            KafkaTopics.ORDER_CREATED,
            event.getOrderId().toString(),
            event,
            "OrderCreatedEvent"
        );
    }
    
    /**
     * Publish order status changed event
     */
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        publishEvent(
            KafkaTopics.ORDER_STATUS_CHANGED,
            event.getOrderId().toString(),
            event,
            "OrderStatusChangedEvent"
        );
    }
}
