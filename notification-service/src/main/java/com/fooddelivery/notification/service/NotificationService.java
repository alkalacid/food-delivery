package com.fooddelivery.notification.service;

import com.fooddelivery.notification.dto.NotificationResponseDTO;
import com.fooddelivery.notification.dto.SendNotificationRequestDTO;
import com.fooddelivery.notification.entity.Notification;
import com.fooddelivery.notification.enums.NotificationStatus;
import com.fooddelivery.notification.mapper.NotificationMapper;
import com.fooddelivery.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main notification service with async processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SmsService smsService;
    
    private static final int MAX_RETRIES = 3;

    @Async("notificationExecutor")
    @Transactional
    public void sendNotificationAsync(SendNotificationRequestDTO request) {
        log.info("Processing notification: type={}, channel={}, userId={}", 
                 request.type(), request.channel(), request.userId());
        
        try {
            Notification notification = notificationMapper.toEntity(request);
            notification.setStatus(NotificationStatus.SENDING);
            Notification saved = notificationRepository.save(notification);
            
            boolean sent = sendThroughChannel(saved);
            
            if (sent) {
                saved.markSent();
                log.info("Notification {} sent successfully", saved.getId());
            } else {
                saved.markFailed("Failed to send through channel");
                log.warn("Notification {} failed to send", saved.getId());
            }
            
            notificationRepository.save(saved);
            
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send notification through appropriate channel
     */
    private boolean sendThroughChannel(Notification notification) {
        return switch (notification.getChannel()) {
            case EMAIL -> {
                if (notification.getRecipientEmail() == null) {
                    log.error("Cannot send email notification: recipient email is null");
                    yield false;
                }
                yield emailService.sendEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getContent()
                );
            }
            case SMS -> {
                if (notification.getRecipientPhone() == null) {
                    log.error("Cannot send SMS notification: recipient phone is null");
                    yield false;
                }
                yield smsService.sendSms(
                    notification.getRecipientPhone(),
                    notification.getContent()
                );
            }
        };
    }
    
    /**
     * Get notification history for current user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getMyNotifications(Pageable pageable) {
        Long userId = com.fooddelivery.common.security.SecurityUtils.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }
    
    /**
     * Retry failed notifications
     */
    @Async("notificationExecutor")
    @Transactional
    public void retryFailedNotifications() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Notification> failedNotifications = notificationRepository
                .findFailedForRetry(MAX_RETRIES, since);
        
        log.info("Retrying {} failed notifications", failedNotifications.size());
        
        for (Notification notification : failedNotifications) {
            try {
                notification.setStatus(NotificationStatus.SENDING);
                boolean sent = sendThroughChannel(notification);
                
                if (sent) {
                    notification.markSent();
                    log.info("Retry successful for notification {}", notification.getId());
                } else {
                    notification.markFailed("Retry failed");
                    log.warn("Retry failed for notification {}", notification.getId());
                }
                
                notificationRepository.save(notification);
                
            } catch (Exception e) {
                log.error("Error retrying notification {}: {}", 
                          notification.getId(), e.getMessage());
                notification.markFailed("Retry error: " + e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }
}

