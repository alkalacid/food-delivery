package com.fooddelivery.common.event;

import lombok.*;

/**
 * Event published when courier is assigned to delivery
 * Consumed by: Notification Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeliveryAssignedEvent extends BaseEvent {
    
    private Long deliveryId;
    private Long orderId;
    private Long courierId;
    private Long userId;
    private Integer estimatedTimeMinutes;
}

