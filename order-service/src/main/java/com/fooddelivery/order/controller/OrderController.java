package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create new order
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        return orderService.createOrder(request);
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
    
    /**
     * Get current user's orders
     */
    @GetMapping("/my-orders")
    public Page<OrderResponseDTO> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getUserOrders(pageable);
    }
    
    /**
     * Get active orders for current user
     */
    @GetMapping("/my-orders/active")
    public List<OrderResponseDTO> getMyActiveOrders() {
        return orderService.getActiveOrdersByUser();
    }
    
    /**
     * Get orders for a restaurant (restaurant owner only)
     */
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public Page<OrderResponseDTO> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getRestaurantOrders(restaurantId, pageable);
    }
    
    /**
     * Get active orders for a restaurant (restaurant owner only)
     */
    @GetMapping("/restaurant/{restaurantId}/active")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public List<OrderResponseDTO> getRestaurantActiveOrders(@PathVariable Long restaurantId) {
        return orderService.getActiveOrdersByRestaurant(restaurantId);
    }
    
    /**
     * Update order status
     */
    @PatchMapping("/{orderId}/status")
    public OrderResponseDTO updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDTO request) {
        return orderService.updateOrderStatus(orderId, request);
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public OrderResponseDTO cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        return orderService.cancelOrder(orderId, reason);
    }
    
    /**
     * Get order history (status changes)
     */
    @GetMapping("/{orderId}/history")
    public List<OrderHistoryResponseDTO> getOrderHistory(@PathVariable Long orderId) {
        return orderService.getOrderHistory(orderId);
    }
}

