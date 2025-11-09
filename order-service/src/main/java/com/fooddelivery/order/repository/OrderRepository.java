package com.fooddelivery.order.repository;

import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user ID with pagination
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find orders by restaurant ID with pagination
     */
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);
    
    /**
     * Find active orders for a user (not delivered or cancelled)
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId " +
           "AND o.status NOT IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUser(
        @Param("userId") Long userId,
        @Param("statuses") List<OrderStatus> statuses
    );
    
    /**
     * Find active orders for a restaurant (not delivered or cancelled)
     */
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
           "AND o.status NOT IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByRestaurant(
        @Param("restaurantId") Long restaurantId,
        @Param("statuses") List<OrderStatus> statuses
    );

    /**
     * Find order with items eagerly loaded
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}

