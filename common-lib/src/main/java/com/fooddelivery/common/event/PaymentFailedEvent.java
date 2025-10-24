package com.fooddelivery.common.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Event published when payment fails
 * Consumed by: Order Service, Notification Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends BaseEvent {
    
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String errorMessage;
}

