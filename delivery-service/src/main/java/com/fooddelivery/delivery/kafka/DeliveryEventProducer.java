package com.fooddelivery.delivery.kafka;

import com.fooddelivery.common.event.DeliveryAssignedEvent;
import com.fooddelivery.common.event.DeliveryDeliveredEvent;
import com.fooddelivery.common.kafka.BaseKafkaEventProducer;
import com.fooddelivery.common.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event producer for delivery events
 */
@Component
@Slf4j
public class DeliveryEventProducer extends BaseKafkaEventProducer {
    
    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    /**
     * Publish delivery assigned event
     */
    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        publishEvent(
            KafkaTopics.DELIVERY_ASSIGNED,
            event.getDeliveryId().toString(),
            event,
            "DeliveryAssignedEvent"
        );
    }
    
    /**
     * Publish delivery delivered event
     */
    public void publishDeliveryDelivered(DeliveryDeliveredEvent event) {
        publishEvent(
            KafkaTopics.DELIVERY_DELIVERED,
            event.getDeliveryId().toString(),
            event,
            "DeliveryDeliveredEvent"
        );
    }
}
