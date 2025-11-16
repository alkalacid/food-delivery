package com.fooddelivery.payment.repository;

import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderId(Long orderId);
    
    /**
     * Find payments by user ID
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find payments by status
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderId(Long orderId);
}

