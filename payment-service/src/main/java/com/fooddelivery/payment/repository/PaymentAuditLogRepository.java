package com.fooddelivery.payment.repository;

import com.fooddelivery.payment.entity.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {

    /**
     * Find failed payment attempts (fraud detection)
     */
    List<PaymentAuditLog> findByUserIdAndSuccessFalseAndTimestampAfter(
        Long userId, 
        LocalDateTime after
    );
}

