package com.fooddelivery.order.dto;

import com.fooddelivery.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
    Long id,
    Long userId,
    Long restaurantId,
    Long deliveryAddressId,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal deliveryFee,
    BigDecimal discount,
    BigDecimal totalAmount,
    String promoCode,
    String specialInstructions,
    Integer estimatedDeliveryTime,
    LocalDateTime createdAt,
    LocalDateTime deliveredAt,
    List<OrderItemResponseDTO> items
) {}

