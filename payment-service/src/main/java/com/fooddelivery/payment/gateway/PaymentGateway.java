package com.fooddelivery.payment.gateway;

import com.fooddelivery.payment.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class PaymentGateway {
    
    private static final double MOCK_PAYMENT_SUCCESS_RATE = 0.95;
    private static final double MOCK_REFUND_SUCCESS_RATE = 0.98;
    private static final int TRANSACTION_ID_LENGTH = 8;
    
    public PaymentResult processPayment(BigDecimal amount, PaymentMethod method) {
        log.info("Processing payment: amount={}, method={}", amount, method);
        
        boolean success = ThreadLocalRandom.current().nextDouble() < MOCK_PAYMENT_SUCCESS_RATE;
        
        if (success) {
            String transactionId = generateTransactionId();
            log.info("Payment successful: transactionId={}", transactionId);
            return new PaymentResult(true, transactionId, null);
        } else {
            String failureReason = getRandomFailureReason();
            log.warn("Payment failed: reason={}", failureReason);
            return new PaymentResult(false, null, failureReason);
        }
    }
    
    public RefundResult processRefund(String originalTransactionId, BigDecimal amount) {
        log.info("Processing refund: originalTx={}, amount={}", originalTransactionId, amount);
        
        boolean success = ThreadLocalRandom.current().nextDouble() < MOCK_REFUND_SUCCESS_RATE;
        
        if (success) {
            String refundTransactionId = generateTransactionId();
            log.info("Refund successful: refundTx={}", refundTransactionId);
            return new RefundResult(true, refundTransactionId, null);
        } else {
            String failureReason = "Refund processing failed";
            log.warn("Refund failed: reason={}", failureReason);
            return new RefundResult(false, null, failureReason);
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, TRANSACTION_ID_LENGTH).toUpperCase();
    }
    
    private String getRandomFailureReason() {
        String[] reasons = {
            "Insufficient funds",
            "Card declined",
            "Payment gateway timeout",
            "Invalid card details",
            "Transaction limit exceeded"
        };
        return reasons[ThreadLocalRandom.current().nextInt(reasons.length)];
    }
    
    public record PaymentResult(
        boolean success,
        String transactionId,
        String failureReason
    ) {}
    
    public record RefundResult(
        boolean success,
        String refundTransactionId,
        String failureReason
    ) {}
}
