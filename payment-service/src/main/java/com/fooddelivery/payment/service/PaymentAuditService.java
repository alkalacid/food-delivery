package com.fooddelivery.payment.service;

import com.fooddelivery.payment.entity.PaymentAuditLog;
import com.fooddelivery.payment.repository.PaymentAuditLogRepository;
import com.fooddelivery.payment.security.SensitiveDataMask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging of payment operations
 * IMPORTANT: Audit logs must be:
 * 1. Immutable (never delete or modify)
 * 2. Complete (log all operations)
 * 3. Secure (no sensitive data)
 * 4. Separate transaction (don't rollback with payment)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAuditService {
    
    private final PaymentAuditLogRepository auditRepository;
    
    /**
     * Log payment action
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentAction(
            Long paymentId,
            Long userId,
            String action,
            String previousStatus,
            String newStatus,
            Boolean success,
            String errorMessage,
            String ipAddress,
            String userAgent,
            String details) {
        
        try {
            PaymentAuditLog auditLog = new PaymentAuditLog();
            auditLog.setPaymentId(paymentId);
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setPreviousStatus(previousStatus);
            auditLog.setNewStatus(newStatus);
            auditLog.setSuccess(success);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setDetails(maskSensitiveData(details));
            auditLog.setErrorMessage(maskSensitiveData(errorMessage));
            
            auditRepository.save(auditLog);
            
            log.debug("Audit log created: action={}, paymentId={}, userId={}", 
                action, paymentId, userId);
                
        } catch (Exception e) {
            // CRITICAL: Audit logging must NEVER fail the main transaction
            log.error("Failed to create audit log: action={}, paymentId={}, error={}", 
                action, paymentId, e.getMessage());
            // Don't throw exception - let payment continue
        }
    }
    
    /**
     * Check for suspicious activity (fraud detection)
     * Returns true if user has too many failed attempts
     */
    @Transactional(readOnly = true)
    public boolean hasReachedFailureLimit(Long userId, int maxFailures, int withinMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(withinMinutes);
        List<PaymentAuditLog> failedAttempts = auditRepository
            .findByUserIdAndSuccessFalseAndTimestampAfter(userId, since);
        
        boolean suspicious = failedAttempts.size() >= maxFailures;
        
        if (suspicious) {
            log.warn("FRAUD ALERT: User {} has {} failed payment attempts in last {} minutes",
                userId, failedAttempts.size(), withinMinutes);
        }
        
        return suspicious;
    }
    
    /**
     * Mask sensitive data in strings for audit logging
     * 
     * @param data string that may contain sensitive data
     * @return original string if safe, redacted message otherwise
     */
    private String maskSensitiveData(String data) {
        if (data != null && !SensitiveDataMask.isSafeToLog(data)) {
            return "[REDACTED - Contains sensitive data]";
        }
        return data;
    }
}

