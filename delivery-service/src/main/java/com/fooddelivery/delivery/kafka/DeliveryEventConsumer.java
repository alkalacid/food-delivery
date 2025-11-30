package com.fooddelivery.delivery.kafka;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.kafka.KafkaConsumerGroups;
import com.fooddelivery.common.kafka.KafkaTopics;
import com.fooddelivery.delivery.dto.CreateDeliveryRequestDTO;
import com.fooddelivery.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for delivery-service
 * Consumes: order.created
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {
    
    private final DeliveryService deliveryService;
    
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = KafkaConsumerGroups.DELIVERY_SERVICE)
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, eventId={}", 
                     event.getOrderId(), event.getEventId());
            
            CreateDeliveryRequestDTO request = new CreateDeliveryRequestDTO(
                event.getOrderId(),
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                event.getDeliveryLatitude(),
                event.getDeliveryLongitude(),
                null
            );
            
            deliveryService.createDeliveryFromEvent(request, event.getUserId());
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling OrderCreatedEvent: orderId={}, error={}", 
                      event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
