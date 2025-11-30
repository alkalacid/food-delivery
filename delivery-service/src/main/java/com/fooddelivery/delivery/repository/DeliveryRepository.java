package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    /**
     * Find delivery by order ID
     */
    Optional<Delivery> findByOrderId(Long orderId);
    
    /**
     * Find deliveries by courier ID
     */
    @Query("SELECT d FROM Delivery d WHERE d.courier.id = :courierId ORDER BY d.createdAt DESC")
    Page<Delivery> findByCourierId(@Param("courierId") Long courierId, Pageable pageable);
    
    /**
     * Find active delivery for courier (in progress)
     */
    @Query("SELECT d FROM Delivery d " +
           "WHERE d.courier.id = :courierId " +
           "AND d.status IN ('ASSIGNED', 'IN_TRANSIT')")
    Optional<Delivery> findActiveDeliveryByCourier(@Param("courierId") Long courierId);
    
    /**
     * Find pending deliveries (awaiting assignment)
     */
    @Query("SELECT d FROM Delivery d " +
           "WHERE d.status = 'PENDING' " +
           "ORDER BY d.createdAt ASC")
    List<Delivery> findPendingDeliveries();
    
    /**
     * Find deliveries by status
     */
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);
    
    /**
     * Count active deliveries for courier
     */
    @Query("SELECT COUNT(d) FROM Delivery d " +
           "WHERE d.courier.id = :courierId " +
           "AND d.status IN ('ASSIGNED', 'IN_TRANSIT')")
    long countActiveDeliveriesByCourier(@Param("courierId") Long courierId);
}

