package com.fooddelivery.payment.kafka;

import com.fooddelivery.common.event.PaymentFailedEvent;
import com.fooddelivery.common.event.PaymentProcessedEvent;
import com.fooddelivery.common.kafka.BaseKafkaEventProducer;
import com.fooddelivery.common.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event producer for payment events
 */
@Component
@Slf4j
public class PaymentEventProducer extends BaseKafkaEventProducer {
    
    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    /**
     * Publish payment processed event
     */
    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        publishEvent(
            KafkaTopics.PAYMENT_PROCESSED,
            event.getPaymentId().toString(),
            event,
            "PaymentProcessedEvent"
        );
    }
    
    /**
     * Publish payment failed event
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        publishEvent(
            KafkaTopics.PAYMENT_FAILED,
            event.getPaymentId().toString(),
            event,
            "PaymentFailedEvent"
        );
    }
}
