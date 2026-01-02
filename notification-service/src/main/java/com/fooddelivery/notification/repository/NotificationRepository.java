package com.fooddelivery.notification.repository;

import com.fooddelivery.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notification history by user ID (for audit/tracking)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.status = 'FAILED' " +
           "AND n.retryCount < :maxRetries " +
           "AND n.createdAt > :since " +
           "ORDER BY n.createdAt ASC")
    List<Notification> findFailedForRetry(
            @Param("maxRetries") Integer maxRetries,
            @Param("since") LocalDateTime since
    );
}

