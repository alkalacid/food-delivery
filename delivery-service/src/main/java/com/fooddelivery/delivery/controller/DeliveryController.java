package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.CreateDeliveryRequestDTO;
import com.fooddelivery.delivery.dto.DeliveryRatingDTO;
import com.fooddelivery.delivery.dto.DeliveryResponseDTO;
import com.fooddelivery.delivery.enums.DeliveryStatus;
import com.fooddelivery.delivery.service.DeliveryService;
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

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    /**
     * Create delivery (from order-service)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponseDTO createDelivery(@Valid @RequestBody CreateDeliveryRequestDTO request) {
        return deliveryService.createDelivery(request);
    }
    
    /**
     * Get delivery by ID
     */
    @GetMapping("/{id}")
    public DeliveryResponseDTO getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id);
    }
    
    /**
     * Get delivery by order ID
     */
    @GetMapping("/order/{orderId}")
    public DeliveryResponseDTO getDeliveryByOrderId(@PathVariable Long orderId) {
        return deliveryService.getDeliveryByOrderId(orderId);
    }
    
    /**
     * Get deliveries for courier
     */
    @GetMapping("/courier/{courierId}")
    @PreAuthorize("hasAnyRole('COURIER', 'ADMIN')")
    public Page<DeliveryResponseDTO> getDeliveriesForCourier(
            @PathVariable Long courierId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return deliveryService.getDeliveriesForCourier(courierId, pageable);
    }
    
    /**
     * Get deliveries by status (admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<DeliveryResponseDTO> getDeliveriesByStatus(
            @PathVariable DeliveryStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return deliveryService.getDeliveriesByStatus(status, pageable);
    }
    
    /**
     * Mark delivery as picked up (courier action)
     */
    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasRole('COURIER')")
    public DeliveryResponseDTO markPickedUp(@PathVariable Long id) {
        return deliveryService.markPickedUp(id);
    }
    
    /**
     * Mark delivery as delivered (courier action)
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('COURIER')")
    public DeliveryResponseDTO markDelivered(@PathVariable Long id) {
        return deliveryService.markDelivered(id);
    }
    
    /**
     * Cancel delivery
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public DeliveryResponseDTO cancelDelivery(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return deliveryService.cancelDelivery(id, reason);
    }
    
    /**
     * Add rating to delivery (customer action)
     */
    @PostMapping("/{id}/rating")
    @PreAuthorize("hasRole('CUSTOMER')")
    public DeliveryResponseDTO addRating(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryRatingDTO rating) {
        return deliveryService.addRating(id, rating);
    }
    
    /**
     * Retry pending assignments (admin/system action)
     */
    @PostMapping("/retry-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retryPendingAssignments() {
        deliveryService.retryPendingAssignments();
    }
}

