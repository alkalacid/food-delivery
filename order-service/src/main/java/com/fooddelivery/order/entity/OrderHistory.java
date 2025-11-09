package com.fooddelivery.order.entity;

import com.fooddelivery.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history", indexes = {
    @Index(name = "idx_order_history_order_id", columnList = "order_id"),
    @Index(name = "idx_order_history_changed_at", columnList = "changedAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "order")
public class OrderHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    /**
     * Status the order changed to
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    
    /**
     * User ID who changed the status (customer, restaurant owner, courier, or system)
     */
    @Column(nullable = false)
    private Long changedBy;
    
    /**
     * Optional comment about the status change
     */
    @Column(length = 500)
    private String comment;
    
    /**
     * Timestamp when status changed
     */
    @Column(nullable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime changedAt;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}

