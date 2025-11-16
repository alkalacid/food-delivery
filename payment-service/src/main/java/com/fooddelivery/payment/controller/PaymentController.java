package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.dto.PaymentResponseDTO;
import com.fooddelivery.payment.dto.ProcessPaymentRequestDTO;
import com.fooddelivery.payment.dto.RefundRequestDTO;
import com.fooddelivery.payment.dto.RefundResponseDTO;
import com.fooddelivery.payment.enums.PaymentStatus;
import com.fooddelivery.payment.service.PaymentService;
import com.fooddelivery.payment.service.RefundService;
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

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final RefundService refundService;
    
    /**
     * Process payment for an order
     */
    @PostMapping("/process")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDTO processPayment(@Valid @RequestBody ProcessPaymentRequestDTO request) {
        return paymentService.processPayment(request);
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }
    
    /**
     * Get payment by order ID
     */
    @GetMapping("/orders/{orderId}")
    public PaymentResponseDTO getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }
    
    /**
     * Get user's payment history
     */
    @GetMapping("/my-payments")
    public Page<PaymentResponseDTO> getMyPayments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.getUserPayments(pageable);
    }
    
    /**
     * Get payments by status (admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponseDTO> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.getPaymentsByStatus(status, pageable);
    }
    
    /**
     * Request refund for a payment
     */
    @PostMapping("/{paymentId}/refund")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponseDTO requestRefund(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequestDTO request) {
        return refundService.processRefund(paymentId, request);
    }
    
    /**
     * Get refunds for a payment
     */
    @GetMapping("/{paymentId}/refunds")
    public List<RefundResponseDTO> getRefundsByPaymentId(@PathVariable Long paymentId) {
        return refundService.getRefundsByPaymentId(paymentId);
    }
}

