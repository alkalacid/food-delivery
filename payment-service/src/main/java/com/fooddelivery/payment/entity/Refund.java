package com.fooddelivery.payment.entity;

import com.fooddelivery.payment.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refunds_payment_id", columnList = "payment_id"),
    @Index(name = "idx_refunds_status", columnList = "status"),
    @Index(name = "idx_refunds_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "payment")
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    /**
     * Refund amount (can be partial)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Reason for refund
     */
    @Column(nullable = false, length = 500)
    private String reason;
    
    /**
     * Refund status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;
    
    /**
     * Transaction ID from payment gateway for refund
     */
    @Column(length = 100)
    private String transactionId;
    
    /**
     * User who requested the refund
     */
    @Column(nullable = false)
    private Long requestedBy;
    
    /**
     * When refund was completed
     */
    @Column
    private LocalDateTime completedAt;
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RefundStatus.REQUESTED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark refund as completed
     * 
     * @param refundTransactionId transaction ID from payment gateway
     */
    public void markCompleted(String refundTransactionId) {
        this.status = RefundStatus.COMPLETED;
        this.transactionId = refundTransactionId;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Mark refund as failed
     */
    public void markFailed() {
        this.status = RefundStatus.REJECTED;
    }
}

