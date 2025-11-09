package com.fooddelivery.order.service;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.order.config.ExternalServicesProperties;
import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.enums.OrderStatus;
import com.fooddelivery.order.exception.InvalidOrderDataException;
import com.fooddelivery.order.exception.OrderNotFoundException;
import com.fooddelivery.order.exception.UnauthorizedOrderAccessException;
import com.fooddelivery.order.mapper.OrderHistoryMapper;
import com.fooddelivery.order.mapper.OrderMapper;
import com.fooddelivery.order.repository.OrderHistoryRepository;
import com.fooddelivery.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderMapper orderMapper;
    private final OrderHistoryMapper orderHistoryMapper;
    private final OrderStateMachine stateMachine;
    private final PricingService pricingService;
    private final PromoCodeService promoCodeService;
    private final RestTemplate restTemplate;
    private final ExternalServicesProperties servicesProperties;
    private final com.fooddelivery.order.kafka.OrderEventProducer orderEventProducer;
    
    /**
     * Create new order
     */
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating order for user: {} at restaurant: {}", userId, request.restaurantId());
        
        validateCreateOrderRequest(request);
        
        MenuItemsData menuItemsData = fetchMenuItemsData(request.items());
        
        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(request.restaurantId());
        order.setDeliveryAddressId(request.deliveryAddressId());
        order.setSpecialInstructions(request.specialInstructions());
        order.setPromoCode(request.promoCode());
        order.setStatus(OrderStatus.CREATED);
        
        for (OrderItemRequestDTO itemRequest : request.items()) {
            Map<String, Object> menuItemData = menuItemsData.items().get(itemRequest.menuItemId());
            if (menuItemData == null) {
                throw new InvalidOrderDataException("Menu item not found: " + itemRequest.menuItemId());
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemRequest.menuItemId());
            orderItem.setMenuItemName((String) menuItemData.get("name"));
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPrice(BigDecimal.valueOf(((Number) menuItemData.get("price")).doubleValue()));
            orderItem.setSpecialInstructions(itemRequest.specialInstructions());
            
            order.addItem(orderItem);
        }
        
        BigDecimal deliveryFee = pricingService.calculateDeliveryFee(
            request.restaurantId(), 
            request.deliveryAddressId()
        );
        order.setDeliveryFee(deliveryFee);
        
        com.fooddelivery.order.entity.PromoCode promoCode = null;
        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            promoCode = promoCodeService.validatePromoCode(request.promoCode(), userId, order.getSubtotal());
            BigDecimal discount = promoCodeService.calculateDiscount(promoCode, order.getSubtotal(), deliveryFee);
            order.setDiscount(discount);
        } else {
            order.setDiscount(BigDecimal.ZERO);
        }
        
        order.calculateTotal();
        
        Integer estimatedTime = pricingService.calculateEstimatedDeliveryTime(
            request.restaurantId(),
            request.deliveryAddressId()
        );
        order.setEstimatedDeliveryTime(estimatedTime);
        
        order.addHistory(OrderStatus.CREATED, userId, "Order created");
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());
        
        if (promoCode != null) {
            promoCodeService.recordUsage(promoCode, userId, savedOrder.getId());
        }

        publishOrderCreatedEvent(savedOrder);
        
        return orderMapper.toResponse(savedOrder);
    }
    
    /**
     * Get order by ID (with ownership check)
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Order order = findOrderById(orderId);
        validateUserAccess(order, currentUserId);
        return orderMapper.toResponse(order);
    }

    /**
     * Get order by ID (without ownership check - for restaurant/admin)
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByIdWithoutOwnershipCheck(Long orderId) {
        Order order = findOrderById(orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getRestaurantOrders(Long restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrdersByUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderRepository.findActiveOrdersByUser(userId, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED))
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findActiveOrdersByRestaurant(restaurantId, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED))
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Updating order {} status to {}", orderId, request.status());
        
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        stateMachine.validateTransition(order.getStatus(), request.status());
        
        order.addHistory(request.status(), currentUserId, request.comment());
        
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, request.status());
        
        publishOrderStatusChangedEvent(updated, oldStatus, currentUserId);
        
        return orderMapper.toResponse(updated);
    }
    
    /**
     * Update order status from event (internal use by Kafka consumers)
     */
    @Transactional
    public void updateOrderStatusByEvent(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {} from event", orderId, newStatus);
        
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        stateMachine.validateTransition(order.getStatus(), newStatus);
        
        order.addHistory(newStatus, null, "Status updated from event");
        
        orderRepository.save(order);
        log.info("Order {} status updated to {} from event", orderId, newStatus);
        
        publishOrderStatusChangedEvent(order, oldStatus, null);
    }
    
    /**
     * Cancel order from event (internal use by Kafka consumers)
     */
    @Transactional
    public void cancelOrderByEvent(Long orderId, String reason) {
        log.info("Cancelling order {} from event: {}", orderId, reason);
        
        Order order = findOrderById(orderId);
        
        order.addHistory(OrderStatus.CANCELLED, null, reason);
        
        orderRepository.save(order);
        log.info("Order {} cancelled from event", orderId);
    }
    
    /**
     * Cancel order
     */
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId, String reason) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Cancelling order: {}", orderId);
        
        Order order = findOrderById(orderId);
        validateUserAccess(order, currentUserId);

        stateMachine.validateTransition(order.getStatus(), OrderStatus.CANCELLED);
        
        order.addHistory(OrderStatus.CANCELLED, currentUserId, reason);
        
        Order cancelled = orderRepository.save(order);
        log.info("Order cancelled: {}", orderId);
        
        return orderMapper.toResponse(cancelled);
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponseDTO> getOrderHistory(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Order order = findOrderById(orderId);
        validateUserAccess(order, currentUserId);
        
        return orderHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId).stream()
                .map(orderHistoryMapper::toResponse)
                .toList();
    }
    
    private void validateCreateOrderRequest(CreateOrderRequestDTO request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOrderDataException("Order must contain at least one item");
        }
    }
    
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "fetchMenuItemsDataFallback")
    @Retry(name = "restaurantService")
    private MenuItemsData fetchMenuItemsData(List<OrderItemRequestDTO> items) {
        List<Long> menuItemIds = items.stream()
                .map(OrderItemRequestDTO::menuItemId)
                .toList();
        
        try {
            String url = servicesProperties.getRestaurant().getBaseUrl() + 
                         servicesProperties.getRestaurant().getMenuItemsBatchEndpoint() + 
                         "?ids=" + String.join(",", menuItemIds.stream().map(String::valueOf).toList());
            
            log.debug("Fetching menu items from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<Long, Map<String, Object>> response = restTemplate.getForObject(url, Map.class);
            
            return new MenuItemsData(response);
        } catch (Exception e) {
            log.error("Failed to fetch menu items data", e);
            throw new InvalidOrderDataException("Failed to fetch menu items data");
        }
    }
    
    private MenuItemsData fetchMenuItemsDataFallback(List<OrderItemRequestDTO> items, Exception e) {
        log.error("Circuit breaker fallback: Failed to fetch menu items data after retries", e);
        throw new InvalidOrderDataException("Restaurant service is temporarily unavailable. Please try again later.");
    }
    
    @Transactional(readOnly = true)
    protected Order findOrderById(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }
    
    private void validateUserAccess(Order order, Long currentUserId) {
        if (!order.getUserId().equals(currentUserId)) {
            throw new UnauthorizedOrderAccessException("You are not authorized to access this order");
        }
    }
    
    /**
     * Publish OrderCreatedEvent to Kafka
     */
    private void publishOrderCreatedEvent(Order order) {
        try {
            Map<String, Object> restaurantData = fetchRestaurantData(order.getRestaurantId());
            Map<String, Object> addressData = fetchAddressData(order.getDeliveryAddressId());
            
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .restaurantId(order.getRestaurantId())
                    .deliveryAddressId(order.getDeliveryAddressId())
                    .totalAmount(order.getTotalAmount())
                    .pickupLatitude(getDouble(restaurantData, "latitude"))
                    .pickupLongitude(getDouble(restaurantData, "longitude"))
                    .deliveryLatitude(getDouble(addressData, "latitude"))
                    .deliveryLongitude(getDouble(addressData, "longitude"))
                    .build();
            
            orderEventProducer.publishOrderCreated(event);
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for order {}", order.getId(), e);
        }
    }
    
    /**
     * Publish OrderStatusChangedEvent to Kafka
     */
    private void publishOrderStatusChangedEvent(Order order, OrderStatus oldStatus, Long changedBy) {
        try {
            com.fooddelivery.common.event.OrderStatusChangedEvent event = com.fooddelivery.common.event.OrderStatusChangedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .oldStatus(oldStatus.name())
                    .newStatus(order.getStatus().name())
                    .changedBy(changedBy)
                    .build();
            
            orderEventProducer.publishOrderStatusChanged(event);
        } catch (Exception e) {
            log.error("Failed to publish OrderStatusChangedEvent for order {}", order.getId(), e);
        }
    }
    
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "fetchRestaurantDataFallback")
    @Retry(name = "restaurantService")
    private Map<String, Object> fetchRestaurantData(Long restaurantId) {
        try {
            String url = servicesProperties.getRestaurant().getBaseUrl() + 
                         servicesProperties.getRestaurant().getRestaurantsEndpoint() + 
                         "/" + restaurantId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch restaurant data for restaurant {}", restaurantId, e);
            return Map.of();
        }
    }
    
    private Map<String, Object> fetchRestaurantDataFallback(Long restaurantId, Exception e) {
        log.warn("Circuit breaker fallback: Using empty restaurant data for restaurant {}", restaurantId);
        return Map.of();
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "fetchAddressDataFallback")
    @Retry(name = "userService")
    private Map<String, Object> fetchAddressData(Long addressId) {
        try {
            String url = servicesProperties.getUser().getBaseUrl() + 
                         servicesProperties.getUser().getAddressesEndpoint() + 
                         "/" + addressId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch address data for address {}", addressId, e);
            return Map.of();
        }
    }
    
    private Map<String, Object> fetchAddressDataFallback(Long addressId, Exception e) {
        log.warn("Circuit breaker fallback: Using empty address data for address {}", addressId);
        return Map.of();
    }
    
    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    private record MenuItemsData(Map<Long, Map<String, Object>> items) {}
}

