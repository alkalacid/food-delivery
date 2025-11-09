package com.fooddelivery.order.service;

import com.fooddelivery.order.enums.OrderStatus;
import com.fooddelivery.order.exception.InvalidOrderStateException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Order State Machine managing valid status transitions.
 * Valid flow:
 * CREATED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
 *      ↓
 *   CANCELLED
 * Thread-safe and immutable after construction.
 */
@Component
public class OrderStateMachine {
    
    private final Map<OrderStatus, Set<OrderStatus>> validTransitions;
    
    public OrderStateMachine() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        
        transitions.put(OrderStatus.CREATED, 
            EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        
        transitions.put(OrderStatus.CONFIRMED, 
            EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        
        transitions.put(OrderStatus.PREPARING, 
            EnumSet.of(OrderStatus.READY, OrderStatus.CANCELLED));
        
        transitions.put(OrderStatus.READY, 
            EnumSet.of(OrderStatus.PICKED_UP, OrderStatus.CANCELLED));
        
        transitions.put(OrderStatus.PICKED_UP, 
            EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        
        transitions.put(OrderStatus.DELIVERED, 
            EnumSet.noneOf(OrderStatus.class));
        
        transitions.put(OrderStatus.CANCELLED, 
            EnumSet.noneOf(OrderStatus.class));
        
        this.validTransitions = Collections.unmodifiableMap(transitions);
    }
    
    /**
     * Check if transition from current status to new status is valid
     * 
     * @param currentStatus current order status (must not be null)
     * @param newStatus desired new status (must not be null)
     * @return true if transition is valid
     * @throws IllegalArgumentException if any parameter is null
     */
    public boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new IllegalArgumentException("Order statuses cannot be null");
        }
        
        Set<OrderStatus> allowedTransitions = validTransitions.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }
    
    /**
     * Validate transition and throw exception if invalid
     * 
     * @param currentStatus current order status (must not be null)
     * @param newStatus desired new status (must not be null)
     * @throws InvalidOrderStateException if transition is not valid
     * @throws IllegalArgumentException if any parameter is null
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStateException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }
}

