package com.fooddelivery.order.repository;

import com.fooddelivery.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    
    /**
     * Find history entries for a specific order, ordered by time
     */
    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}

