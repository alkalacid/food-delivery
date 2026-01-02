package com.fooddelivery.notification.controller;

import com.fooddelivery.notification.dto.NotificationResponseDTO;
import com.fooddelivery.notification.dto.SendNotificationRequestDTO;
import com.fooddelivery.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for notification operations
 * Supports EMAIL and SMS notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Send notification (internal service call)
     * Accepts EMAIL or SMS channel
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendNotification(@Valid @RequestBody SendNotificationRequestDTO request) {
        notificationService.sendNotificationAsync(request);
    }
    
    /**
     * Get notification history for current user
     * Shows all sent EMAIL and SMS notifications (audit trail)
     */
    @GetMapping("/my")
    public Page<NotificationResponseDTO> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.getMyNotifications(pageable);
    }
    
    /**
     * Retry failed notifications (admin only)
     * Attempts to resend notifications that failed to deliver
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void retryFailedNotifications() {
        notificationService.retryFailedNotifications();
    }
}

