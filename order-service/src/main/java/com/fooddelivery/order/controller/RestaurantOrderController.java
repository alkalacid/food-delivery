package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.OrderResponseDTO;
import com.fooddelivery.order.dto.UpdateOrderStatusDTO;
import com.fooddelivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Restaurant endpoints for managing their orders
 * Only accessible by RESTAURANT_OWNER users
 */
@RestController
@RequestMapping("/api/restaurant/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantOrderController {

    private final OrderService orderService;

    /**
     * Get all orders for the restaurant
     */
    @GetMapping
    public Page<OrderResponseDTO> getRestaurantOrders(
            @RequestParam Long restaurantId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("Restaurant owner fetching orders for restaurant: {}", restaurantId);
        return orderService.getRestaurantOrders(restaurantId, pageable);
    }

    /**
     * Get active orders for the restaurant
     */
    @GetMapping("/active")
    public List<OrderResponseDTO> getActiveOrders(@RequestParam Long restaurantId) {
        log.info("Restaurant owner fetching active orders for restaurant: {}", restaurantId);
        return orderService.getActiveOrdersByRestaurant(restaurantId);
    }

    /**
     * Update order status (restaurant can confirm, mark as preparing, ready)
     */
    @PutMapping("/{orderId}/status")
    public OrderResponseDTO updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDTO request) {
        log.info("Restaurant owner updating order {} status to {}", orderId, request.status());
        return orderService.updateOrderStatus(orderId, request);
    }

    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderDetails(@PathVariable Long orderId) {
        log.info("Restaurant owner fetching order details: {}", orderId);
        return orderService.getOrderByIdWithoutOwnershipCheck(orderId);
    }
}

