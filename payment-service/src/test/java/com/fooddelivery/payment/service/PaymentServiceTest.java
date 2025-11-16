package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.ProcessPaymentRequestDTO;
import com.fooddelivery.payment.dto.PaymentResponseDTO;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.enums.PaymentMethod;
import com.fooddelivery.payment.enums.PaymentStatus;
import com.fooddelivery.payment.kafka.PaymentEventProducer;
import com.fooddelivery.payment.mapper.PaymentMapper;
import com.fooddelivery.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private PaymentEventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;
    private ProcessPaymentRequestDTO paymentRequest;
    private PaymentResponseDTO paymentResponse;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(1L);
        testPayment.setUserId(1L);
        testPayment.setAmount(BigDecimal.valueOf(55.00));
        testPayment.setMethod(PaymentMethod.CARD);
        testPayment.setStatus(PaymentStatus.PENDING);

        paymentRequest = new ProcessPaymentRequestDTO(
            1L,
            BigDecimal.valueOf(55.00),
            PaymentMethod.CARD,
            "1234"
        );

        paymentResponse = mock(PaymentResponseDTO.class);
    }

    @Test
    void processPayment_Success() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(paymentResponse);

        PaymentResponseDTO result = paymentService.processPayment(paymentRequest);

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditService).logPaymentAction(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    void getPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponseDTO result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        verify(paymentRepository).findById(1L);
    }
}

