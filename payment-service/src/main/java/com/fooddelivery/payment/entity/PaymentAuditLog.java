package com.fooddelivery.payment.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log for all payment-related operations
 * Required for compliance (PCI DSS, SOX, GDPR)
 */
@Entity
@Table(name = "payment_audit_logs", indexes = {
    @Index(name = "idx_audit_payment_id", columnList = "paymentId"),
    @Index(name = "idx_audit_user_id", columnList = "userId"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class PaymentAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Payment ID (can be null for failed attempts)
     */
    @Column
    private Long paymentId;
    
    /**
     * User who performed the action
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Action performed
     */
    @Column(nullable = false, length = 50)
    private String action;  // PAYMENT_INITIATED, PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_REQUESTED, etc.
    
    /**
     * Previous status (if applicable)
     */
    @Column(length = 20)
    private String previousStatus;
    
    /**
     * New status
     */
    @Column(length = 20)
    private String newStatus;
    
    /**
     * Request IP address
     */
    @Column(length = 45)  // IPv6 max length
    private String ipAddress;
    
    /**
     * User agent
     */
    @Column(length = 500)
    private String userAgent;
    
    /**
     * Additional details (JSON format, no sensitive data!)
     */
    @Column(columnDefinition = "TEXT")
    private String details;
    
    /**
     * Success flag
     */
    @Column(nullable = false)
    private Boolean success;
    
    /**
     * Error message (if failed)
     */
    @Column(length = 1000)
    private String errorMessage;
    
    /**
     * Timestamp of the action
     */
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

