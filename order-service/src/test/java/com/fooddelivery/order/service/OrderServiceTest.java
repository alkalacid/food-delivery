package com.fooddelivery.order.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.order.dto.OrderResponseDTO;
import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.enums.OrderStatus;
import com.fooddelivery.order.kafka.OrderEventProducer;
import com.fooddelivery.order.mapper.OrderMapper;
import com.fooddelivery.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStateMachine stateMachine;

    @Mock
    private PricingService pricingService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderResponseDTO orderResponse;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setRestaurantId(1L);
        testOrder.setDeliveryAddressId(1L);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setSubtotal(BigDecimal.valueOf(50.00));
        testOrder.setDeliveryFee(BigDecimal.valueOf(5.00));
        testOrder.setDiscount(BigDecimal.ZERO);
        testOrder.setTotalAmount(BigDecimal.valueOf(55.00));
        testOrder.setEstimatedDeliveryTime(30);
        testOrder.setItems(new ArrayList<>());

        orderResponse = mock(OrderResponseDTO.class);
    }

    @Test
    void getOrderById_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

            OrderResponseDTO result = orderService.getOrderById(1L);

            assertNotNull(result);
            verify(orderRepository).findById(1L);
        }
    }

    @Test
    void updateOrderStatusByEvent_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(stateMachine.isValidTransition(any(), any())).thenReturn(true);

        orderService.updateOrderStatusByEvent(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancelOrderByEvent_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(stateMachine.isValidTransition(any(), any())).thenReturn(true);

        orderService.cancelOrderByEvent(1L, "Payment failed");

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }
}

