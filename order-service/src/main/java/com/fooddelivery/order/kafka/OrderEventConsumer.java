package com.fooddelivery.order.kafka;

import com.fooddelivery.common.event.DeliveryDeliveredEvent;
import com.fooddelivery.common.event.PaymentFailedEvent;
import com.fooddelivery.common.event.PaymentProcessedEvent;
import com.fooddelivery.common.kafka.KafkaConsumerGroups;
import com.fooddelivery.common.kafka.KafkaTopics;
import com.fooddelivery.order.enums.OrderStatus;
import com.fooddelivery.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for order-service
 * Consumes: payment.processed, payment.failed, delivery.delivered
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    
    private final OrderService orderService;
    
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = KafkaConsumerGroups.ORDER_SERVICE)
    public void handlePaymentProcessed(PaymentProcessedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentProcessedEvent: orderId={}, paymentId={}, eventId={}", 
                     event.getOrderId(), event.getPaymentId(), event.getEventId());
            
            orderService.updateOrderStatusByEvent(event.getOrderId(), OrderStatus.CONFIRMED);
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling PaymentProcessedEvent: orderId={}, error={}", 
                      event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = KafkaConsumerGroups.ORDER_SERVICE)
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentFailedEvent: orderId={}, paymentId={}, reason={}, eventId={}", 
                     event.getOrderId(), event.getPaymentId(), event.getErrorMessage(), event.getEventId());
            
            orderService.cancelOrderByEvent(event.getOrderId(), "Payment failed: " + event.getErrorMessage());
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent: orderId={}, error={}", 
                      event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.DELIVERY_DELIVERED, groupId = KafkaConsumerGroups.ORDER_SERVICE)
    public void handleDeliveryDelivered(DeliveryDeliveredEvent event, Acknowledgment ack) {
        try {
            log.info("Received DeliveryDeliveredEvent: orderId={}, deliveryId={}, eventId={}", 
                     event.getOrderId(), event.getDeliveryId(), event.getEventId());
            
            orderService.updateOrderStatusByEvent(event.getOrderId(), OrderStatus.DELIVERED);
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling DeliveryDeliveredEvent: orderId={}, error={}", 
                      event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
