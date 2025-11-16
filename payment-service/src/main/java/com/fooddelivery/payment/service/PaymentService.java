package com.fooddelivery.payment.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.payment.config.PaymentSecurityProperties;
import com.fooddelivery.payment.dto.PaymentResponseDTO;
import com.fooddelivery.payment.dto.ProcessPaymentRequestDTO;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.enums.PaymentStatus;
import com.fooddelivery.payment.exception.PaymentAlreadyExistsException;
import com.fooddelivery.payment.exception.PaymentNotFoundException;
import com.fooddelivery.payment.gateway.PaymentGateway;
import com.fooddelivery.payment.mapper.PaymentMapper;
import com.fooddelivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;
    private final PaymentAuditService auditService;
    private final PaymentSecurityProperties securityProperties;
    private final com.fooddelivery.payment.kafka.PaymentEventProducer paymentEventProducer;
    
    @Transactional
    public PaymentResponseDTO processPayment(ProcessPaymentRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Audit: Payment initiated
        auditService.logPaymentAction(
            null, 
            userId, 
            "PAYMENT_INITIATED", 
            null, 
            PaymentStatus.PENDING.name(),
            true, 
            null,
            null,  // IP should come from request context
            null,  // User-Agent should come from request context
            "orderId=" + request.orderId() + ", method=" + request.method()
        );
        
        // Security: Check for fraud (too many failed attempts)
        if (auditService.hasReachedFailureLimit(
                userId, 
                securityProperties.getMaxFailedAttempts(), 
                securityProperties.getFailureWindowMinutes())) {
            log.warn("SECURITY: Payment blocked for user {} - too many failed attempts", userId);
            
            auditService.logPaymentAction(
                null,
                userId,
                "PAYMENT_BLOCKED",
                null,
                null,
                false,
                "Too many failed payment attempts",
                null,
                null,
                "orderId=" + request.orderId()
            );
            
            throw new IllegalStateException("Payment temporarily blocked due to multiple failed attempts. Please contact support.");
        }
        
        log.info("Processing payment for order: {} (amount: [REDACTED], method: {})", 
            request.orderId(), request.method());
        
        // Idempotency: Check if payment already exists
        if (paymentRepository.existsByOrderId(request.orderId())) {
            log.warn("Duplicate payment attempt for order: {}", request.orderId());
            throw new PaymentAlreadyExistsException("Payment already exists for order: " + request.orderId());
        }
        
        Payment payment = createPayment(request, userId);
        
        PaymentGateway.PaymentResult result = paymentGateway.processPayment(
            request.amount(),
            request.method(),
            request.cardLastFour()
        );
        
        if (result.success()) {
            payment.markCompleted(result.transactionId());
            log.info("Payment completed: orderId={}, status=COMPLETED", request.orderId());
            
            // Audit: Success
            auditService.logPaymentAction(
                payment.getId(),
                userId,
                "PAYMENT_COMPLETED",
                PaymentStatus.PENDING.name(),
                PaymentStatus.COMPLETED.name(),
                true,
                null,
                null,
                null,
                "orderId=" + request.orderId()
            );
        } else {
            payment.markFailed(result.failureReason());
            log.warn("Payment failed: orderId={}, status=FAILED", request.orderId());
            
            // Audit: Failure
            auditService.logPaymentAction(
                payment.getId(),
                userId,
                "PAYMENT_FAILED",
                PaymentStatus.PENDING.name(),
                PaymentStatus.FAILED.name(),
                false,
                result.failureReason(),
                null,
                null,
                "orderId=" + request.orderId()
            );
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Publish Kafka events
        if (savedPayment.getStatus() == PaymentStatus.COMPLETED) {
            publishPaymentProcessedEvent(savedPayment);
        } else if (savedPayment.getStatus() == PaymentStatus.FAILED) {
            publishPaymentFailedEvent(savedPayment, result.failureReason());
        }
        
        return paymentMapper.toResponse(savedPayment);
    }
    
    @Transactional
    protected Payment createPayment(ProcessPaymentRequestDTO request, Long userId) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(userId);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setCardLastFour(request.cardLastFour());
        payment.setStatus(PaymentStatus.PENDING);
        
        return payment;
    }
    
    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        return paymentMapper.toResponse(payment);
    }
    
    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        return paymentMapper.toResponse(payment);
    }
    
    /**
     * Get user's payment history
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getUserPayments(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(paymentMapper::toResponse);
    }
    
    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(paymentMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    protected Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
    }
    
    /**
     * Publish PaymentProcessedEvent to Kafka
     */
    private void publishPaymentProcessedEvent(Payment payment) {
        try {
            com.fooddelivery.common.event.PaymentProcessedEvent event = com.fooddelivery.common.event.PaymentProcessedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getMethod().name())
                    .transactionId(payment.getTransactionId())
                    .build();
            
            paymentEventProducer.publishPaymentProcessed(event);
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent for payment {}", payment.getId(), e);
        }
    }
    
    /**
     * Publish PaymentFailedEvent to Kafka
     */
    private void publishPaymentFailedEvent(Payment payment, String errorMessage) {
        try {
            com.fooddelivery.common.event.PaymentFailedEvent event = com.fooddelivery.common.event.PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getMethod().name())
                    .errorMessage(errorMessage)
                    .build();
            
            paymentEventProducer.publishPaymentFailed(event);
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailedEvent for payment {}", payment.getId(), e);
        }
    }
}

