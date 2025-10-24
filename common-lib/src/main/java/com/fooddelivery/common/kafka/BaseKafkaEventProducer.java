package com.fooddelivery.common.kafka;

import com.fooddelivery.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Base class for Kafka event producers
 * Provides common publishing logic with error handling
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseKafkaEventProducer {
    
    protected final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish event to Kafka topic with error handling
     * 
     * @param topic Kafka topic name
     * @param key Message key (for partitioning)
     * @param event Event to publish
     * @param eventType Event type name (for logging)
     */
    protected void publishEvent(String topic, String key, BaseEvent event, String eventType) {
        log.info("Publishing {}: eventId={}, key={}", eventType, event.getEventId(), key);
        
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("{} published successfully: eventId={}, offset={}", 
                         eventType, event.getEventId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish {}: eventId={}, error={}", 
                          eventType, event.getEventId(), ex.getMessage(), ex);
            }
        });
    }
}

