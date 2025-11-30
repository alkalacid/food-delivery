package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.entity.Courier;
import com.fooddelivery.delivery.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    
    /**
     * Find active courier by ID
     */
    @Query("SELECT c FROM Courier c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Courier> findActiveById(@Param("id") Long id);
    
    /**
     * Find courier by user ID
     */
    @Query("SELECT c FROM Courier c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    Optional<Courier> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find available couriers near location
     */
    @Query("SELECT c FROM Courier c " +
           "WHERE c.status = 'AVAILABLE' " +
           "AND c.deletedAt IS NULL " +
           "AND c.currentLatitude IS NOT NULL " +
           "AND c.currentLongitude IS NOT NULL")
    List<Courier> findAvailableCouriers();
    
    /**
     * Find couriers by status
     */
    @Query("SELECT c FROM Courier c WHERE c.status = :status AND c.deletedAt IS NULL")
    List<Courier> findByStatus(@Param("status") CourierStatus status);
    
    /**
     * Check if user is already registered as courier
     */
    boolean existsByUserId(Long userId);
}

