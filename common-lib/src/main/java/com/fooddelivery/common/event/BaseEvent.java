package com.fooddelivery.common.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Base class for all domain events
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "eventId")
public abstract class BaseEvent {
    
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
    
    protected BaseEvent() {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }
}

