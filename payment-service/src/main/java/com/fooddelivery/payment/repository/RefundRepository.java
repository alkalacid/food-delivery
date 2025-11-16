package com.fooddelivery.payment.repository;

import com.fooddelivery.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    /**
     * Find all refunds for a payment
     */
    List<Refund> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}

