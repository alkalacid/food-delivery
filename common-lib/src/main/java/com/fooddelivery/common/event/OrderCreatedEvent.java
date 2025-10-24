package com.fooddelivery.common.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Event published when a new order is created
 * Consumed by: Delivery Service, Notification Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private Long deliveryAddressId;
    private BigDecimal totalAmount;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
}

