package com.fooddelivery.payment.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.payment.dto.RefundRequestDTO;
import com.fooddelivery.payment.dto.RefundResponseDTO;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.entity.Refund;
import com.fooddelivery.payment.enums.PaymentStatus;
import com.fooddelivery.payment.enums.RefundStatus;
import com.fooddelivery.payment.exception.InvalidRefundException;
import com.fooddelivery.payment.exception.PaymentNotFoundException;
import com.fooddelivery.payment.gateway.PaymentGateway;
import com.fooddelivery.payment.mapper.RefundMapper;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {
    
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RefundMapper refundMapper;
    private final PaymentGateway paymentGateway;
    
    /**
     * Process refund for a payment
     */
    @Transactional
    public RefundResponseDTO processRefund(Long paymentId, RefundRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Processing refund for payment: {}", paymentId);
        
        Payment payment = findPaymentById(paymentId);
        
        validateRefundRequest(payment, request.amount());
        
        Refund refund = createRefund(payment, request, userId);
        
        PaymentGateway.RefundResult result = paymentGateway.processRefund(
            payment.getTransactionId(),
            request.amount()
        );
        
        if (result.success()) {
            refund.markCompleted(result.refundTransactionId());
            
            BigDecimal totalRefunded = calculateTotalRefunded(payment);
            if (totalRefunded.add(request.amount()).compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            }
            
            log.info("Refund completed: paymentId={}, refundTx={}", paymentId, result.refundTransactionId());
        } else {
            refund.markFailed();
            log.warn("Refund failed: paymentId={}, reason={}", paymentId, result.failureReason());
        }
        
        Refund savedRefund = refundRepository.save(refund);
        return refundMapper.toResponse(savedRefund);
    }
    
    /**
     * Get refunds for a payment
     */
    @Transactional(readOnly = true)
    public List<RefundResponseDTO> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId).stream()
                .map(refundMapper::toResponse)
                .toList();
    }
    
    private void validateRefundRequest(Payment payment, BigDecimal refundAmount) {
        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new InvalidRefundException("Cannot refund payment with status: " + payment.getStatus());
        }
        
        BigDecimal totalRefunded = calculateTotalRefunded(payment);
        BigDecimal availableForRefund = payment.getAmount().subtract(totalRefunded);
        
        if (refundAmount.compareTo(availableForRefund) > 0) {
            throw new InvalidRefundException(
                String.format("Refund amount %.2f exceeds available amount %.2f", 
                    refundAmount, availableForRefund)
            );
        }
    }
    
    private BigDecimal calculateTotalRefunded(Payment payment) {
        return refundRepository.findByPaymentIdOrderByCreatedAtDesc(payment.getId()).stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Transactional
    protected Refund createRefund(Payment payment, RefundRequestDTO request, Long userId) {
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(request.amount());
        refund.setReason(request.reason());
        refund.setRequestedBy(userId);
        refund.setStatus(RefundStatus.PROCESSING);
        
        return refund;
    }
    
    @Transactional(readOnly = true)
    protected Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
    }
}

