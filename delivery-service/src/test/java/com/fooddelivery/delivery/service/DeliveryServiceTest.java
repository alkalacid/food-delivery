package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.CreateDeliveryRequestDTO;
import com.fooddelivery.delivery.dto.DeliveryResponseDTO;
import com.fooddelivery.delivery.entity.Courier;
import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.enums.CourierStatus;
import com.fooddelivery.delivery.enums.DeliveryStatus;
import com.fooddelivery.delivery.kafka.DeliveryEventProducer;
import com.fooddelivery.delivery.mapper.DeliveryMapper;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private DeliveryAssignmentService assignmentService;

    @Mock
    private DeliveryEventProducer eventProducer;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery testDelivery;
    private Courier testCourier;
    private DeliveryResponseDTO deliveryResponse;

    @BeforeEach
    void setUp() {
        testCourier = new Courier();
        testCourier.setId(1L);
        testCourier.setUserId(100L);
        testCourier.setStatus(CourierStatus.AVAILABLE);

        testDelivery = new Delivery();
        testDelivery.setId(1L);
        testDelivery.setOrderId(1L);
        testDelivery.setUserId(1L);
        testDelivery.setStatus(DeliveryStatus.PENDING);
        testDelivery.setPickupLatitude(40.7128);
        testDelivery.setPickupLongitude(-74.0060);
        testDelivery.setDeliveryLatitude(40.7589);
        testDelivery.setDeliveryLongitude(-73.9851);

        deliveryResponse = mock(DeliveryResponseDTO.class);
    }

    @Test
    void createDeliveryFromEvent_Success() {
        CreateDeliveryRequestDTO request = new CreateDeliveryRequestDTO(
            1L, 40.7128, -74.0060, 40.7589, -73.9851, null
        );

        when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(deliveryMapper.toEntity(any(CreateDeliveryRequestDTO.class))).thenReturn(testDelivery);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(testDelivery);
        when(assignmentService.assignCourierToDelivery(any(Delivery.class))).thenReturn(true);

        deliveryService.createDeliveryFromEvent(request, 1L);

        verify(deliveryRepository).save(any(Delivery.class));
        assertEquals(1L, testDelivery.getUserId());
    }

    @Test
    void markDelivered_Success() {
        testDelivery.setCourier(testCourier);
        testDelivery.setStatus(DeliveryStatus.IN_TRANSIT);

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(testDelivery);
        when(deliveryMapper.toResponse(any(Delivery.class))).thenReturn(deliveryResponse);

        DeliveryResponseDTO result = deliveryService.markDelivered(1L);

        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, testDelivery.getStatus());
        verify(deliveryRepository).save(testDelivery);
    }
}

