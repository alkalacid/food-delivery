package com.fooddelivery.notification.kafka;

import com.fooddelivery.common.event.*;
import com.fooddelivery.common.kafka.KafkaConsumerGroups;
import com.fooddelivery.common.kafka.KafkaTopics;
import com.fooddelivery.notification.dto.SendNotificationRequestDTO;
import com.fooddelivery.notification.enums.NotificationChannel;
import com.fooddelivery.notification.enums.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for notification-service
 * Listens to all events to send appropriate notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, eventId={}", 
                     event.getOrderId(), event.getEventId());
            
            sendNotification(
                event.getUserId(),
                NotificationType.ORDER_CREATED,
                "Order Confirmed - #" + event.getOrderId(),
                String.format("Your order #%d has been confirmed! Total amount: $%.2f. " +
                             "We'll notify you when it's ready for delivery.",
                             event.getOrderId(), event.getTotalAmount())
            );
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling OrderCreatedEvent: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handlePaymentProcessed(PaymentProcessedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentProcessedEvent: paymentId={}, orderId={}", 
                     event.getPaymentId(), event.getOrderId());
            
            sendNotification(
                event.getUserId(),
                NotificationType.PAYMENT_PROCESSED,
                "Payment Confirmed - Order #" + event.getOrderId(),
                String.format("Payment of $%.2f has been processed successfully for order #%d. " +
                             "Transaction ID: %s",
                             event.getAmount(), event.getOrderId(), event.getTransactionId())
            );
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling PaymentProcessedEvent: paymentId={}", event.getPaymentId(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentFailedEvent: paymentId={}, orderId={}", 
                     event.getPaymentId(), event.getOrderId());
            
            sendNotification(
                event.getUserId(),
                NotificationType.PAYMENT_FAILED,
                "Payment Failed - Order #" + event.getOrderId(),
                String.format("Payment of $%.2f failed for order #%d. Reason: %s. " +
                             "Please try again or use a different payment method.",
                             event.getAmount(), event.getOrderId(), event.getErrorMessage())
            );
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent: paymentId={}", event.getPaymentId(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.DELIVERY_ASSIGNED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleDeliveryAssigned(DeliveryAssignedEvent event, Acknowledgment ack) {
        try {
            log.info("Received DeliveryAssignedEvent: deliveryId={}, courierId={}", 
                     event.getDeliveryId(), event.getCourierId());
            
            sendNotification(
                event.getUserId(),
                NotificationType.DELIVERY_ASSIGNED,
                "Courier Assigned - Order #" + event.getOrderId(),
                String.format("A courier has been assigned to your order #%d. " +
                             "Estimated delivery time: %d minutes.",
                             event.getOrderId(), event.getEstimatedTimeMinutes())
            );
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling DeliveryAssignedEvent: deliveryId={}", event.getDeliveryId(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = KafkaTopics.DELIVERY_DELIVERED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleDeliveryDelivered(DeliveryDeliveredEvent event, Acknowledgment ack) {
        try {
            log.info("Received DeliveryDeliveredEvent: deliveryId={}, orderId={}", 
                     event.getDeliveryId(), event.getOrderId());
            
            sendNotification(
                event.getUserId(),
                NotificationType.DELIVERY_DELIVERED,
                "Order Delivered - #" + event.getOrderId(),
                String.format("Your order #%d has been delivered! " +
                             "We hope you enjoy your meal. Please rate your experience.",
                             event.getOrderId())
            );
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error handling DeliveryDeliveredEvent: deliveryId={}", event.getDeliveryId(), e);
            throw e;
        }
    }
    
    private void sendNotification(Long userId, NotificationType type, String subject, String content) {
        SendNotificationRequestDTO notification = new SendNotificationRequestDTO(
            userId,
            type,
            NotificationChannel.EMAIL,
            subject,
            content,
            null,
            null
        );
        
        notificationService.sendNotificationAsync(notification);
    }
}
