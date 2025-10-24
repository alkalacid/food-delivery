package com.fooddelivery.common.event;

import lombok.*;

/**
 * Event published when order status changes
 * Consumed by: Notification Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OrderStatusChangedEvent extends BaseEvent {
    
    private Long orderId;
    private Long userId;
    private String oldStatus;
    private String newStatus;
    private Long changedBy;
}

