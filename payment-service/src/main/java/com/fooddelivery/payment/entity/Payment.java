package com.fooddelivery.payment.entity;

import com.fooddelivery.payment.enums.PaymentMethod;
import com.fooddelivery.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_order_id", columnList = "orderId"),
    @Index(name = "idx_payments_user_id", columnList = "userId"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "orderId")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Order ID this payment is for
     */
    @Column(nullable = false, unique = true)
    private Long orderId;
    
    /**
     * User who made the payment
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Payment amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Payment method
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;
    
    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    
    /**
     * Transaction ID from payment gateway
     */
    @Column(length = 100, unique = true)
    private String transactionId;
    
    /**
     * Card last 4 digits (if card payment)
     */
    @Column(length = 4)
    private String cardLastFour;
    
    /**
     * Failure reason if payment failed
     */
    @Column(length = 500)
    private String failureReason;
    
    /**
     * When payment was processed
     */
    @Column
    private LocalDateTime processedAt;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<Refund> refunds = new ArrayList<>();
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark payment as completed
     */
    public void markCompleted(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Mark payment as failed
     */
    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Override toString to prevent sensitive data leakage in logs
     * PCI DSS Compliance: Never log sensitive payment data
     */
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", amount=[REDACTED]" +
                ", method=" + method +
                ", status=" + status +
                ", cardLastFour=[REDACTED]" +
                ", transactionId=[REDACTED]" +
                ", createdAt=" + createdAt +
                '}';
    }
}

