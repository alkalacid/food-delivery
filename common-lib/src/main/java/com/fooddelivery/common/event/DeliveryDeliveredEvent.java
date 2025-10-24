package com.fooddelivery.common.event;

import lombok.*;

/**
 * Event published when order is delivered
 * Consumed by: Order Service, Notification Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeliveryDeliveredEvent extends BaseEvent {
    
    private Long deliveryId;
    private Long orderId;
    private Long courierId;
    private Long userId;
}

