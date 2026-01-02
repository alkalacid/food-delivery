package com.fooddelivery.notification.entity;

import com.fooddelivery.notification.enums.NotificationChannel;
import com.fooddelivery.notification.enums.NotificationStatus;
import com.fooddelivery.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Notification entity for tracking sent notifications
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "userId"),
    @Index(name = "idx_notifications_type", columnList = "type"),
    @Index(name = "idx_notifications_status", columnList = "status"),
    @Index(name = "idx_notifications_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Recipient user ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Notification type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    /**
     * Communication channel used
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;
    
    /**
     * Notification subject/title
     */
    @Column(nullable = false, length = 200)
    private String subject;
    
    /**
     * Notification content/body
     */
    @Column(nullable = false, length = 2000)
    private String content;
    
    /**
     * Delivery status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;
    
    /**
     * Recipient email (for email notifications)
     */
    @Column
    private String recipientEmail;
    
    /**
     * Recipient phone (for SMS notifications)
     */
    @Column(length = 20)
    private String recipientPhone;
    
    /**
     * Error message if failed
     */
    @Column(length = 500)
    private String errorMessage;
    
    /**
     * Number of retry attempts
     */
    @Column
    private Integer retryCount = 0;
    
    /**
     * When notification was sent
     */
    @Column
    private LocalDateTime sentAt;
    
    /**
     * When notification was created
     */
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark notification as sent successfully
     */
    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * Mark notification as failed with error message
     */
    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }
}

