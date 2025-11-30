package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.CreateDeliveryRequestDTO;
import com.fooddelivery.delivery.dto.DeliveryRatingDTO;
import com.fooddelivery.delivery.dto.DeliveryResponseDTO;
import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.enums.CourierStatus;
import com.fooddelivery.delivery.enums.DeliveryStatus;
import com.fooddelivery.delivery.exception.DeliveryAlreadyExistsException;
import com.fooddelivery.delivery.exception.DeliveryNotFoundException;
import com.fooddelivery.delivery.exception.InvalidDeliveryStateException;
import com.fooddelivery.delivery.mapper.DeliveryMapper;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryAssignmentService assignmentService;
    private final com.fooddelivery.delivery.kafka.DeliveryEventProducer deliveryEventProducer;
    
    /**
     * Create new delivery request
     */
    @Transactional
    public DeliveryResponseDTO createDelivery(CreateDeliveryRequestDTO request) {
        log.info("Creating delivery for order: {}", request.orderId());
        
        // Check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new DeliveryAlreadyExistsException(
                "Delivery already exists for order: " + request.orderId()
            );
        }
        
        Delivery delivery = deliveryMapper.toEntity(request);
        delivery.setStatus(DeliveryStatus.PENDING);
        
        Delivery saved = deliveryRepository.save(delivery);
        
        // Try to assign courier immediately
        boolean assigned = assignmentService.assignCourierToDelivery(saved);
        
        if (assigned) {
            log.info("Delivery {} automatically assigned to courier {}", 
                     saved.getId(), saved.getCourier().getId());
            // Publish DeliveryAssignedEvent
            publishDeliveryAssignedEvent(saved);
        } else {
            log.warn("Delivery {} pending - no available courier found", saved.getId());
        }
        
        return deliveryMapper.toResponse(saved);
    }
    
    /**
     * Get delivery by ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponseDTO getDeliveryById(Long id) {
        Delivery delivery = findDeliveryById(id);
        return deliveryMapper.toResponse(delivery);
    }
    
    /**
     * Get delivery by order ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponseDTO getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException(
                    "Delivery not found for order: " + orderId
                ));
        
        return deliveryMapper.toResponse(delivery);
    }
    
    /**
     * Get deliveries for courier
     */
    @Transactional(readOnly = true)
    public Page<DeliveryResponseDTO> getDeliveriesForCourier(Long courierId, Pageable pageable) {
        return deliveryRepository.findByCourierId(courierId, pageable)
                .map(deliveryMapper::toResponse);
    }
    
    /**
     * Get deliveries by status
     */
    @Transactional(readOnly = true)
    public Page<DeliveryResponseDTO> getDeliveriesByStatus(DeliveryStatus status, Pageable pageable) {
        return deliveryRepository.findByStatus(status, pageable)
                .map(deliveryMapper::toResponse);
    }
    
    /**
     * Mark delivery as picked up (courier picked up from restaurant)
     */
    @Transactional
    public DeliveryResponseDTO markPickedUp(Long deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        
        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new InvalidDeliveryStateException(
                "Delivery must be in ASSIGNED state to mark as picked up"
            );
        }
        
        delivery.markPickedUp();
        Delivery updated = deliveryRepository.save(delivery);
        
        log.info("Delivery {} marked as picked up by courier {}", 
                 deliveryId, delivery.getCourier().getId());
        
        return deliveryMapper.toResponse(updated);
    }
    
    /**
     * Mark delivery as delivered
     */
    @Transactional
    public DeliveryResponseDTO markDelivered(Long deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        
        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
            throw new InvalidDeliveryStateException(
                "Delivery must be in IN_TRANSIT state to mark as delivered"
            );
        }
        
        delivery.markDelivered();
        releaseCourier(delivery);
        
        Delivery updated = deliveryRepository.save(delivery);
        
        log.info("Delivery {} marked as delivered by courier {}", 
                 deliveryId, delivery.getCourier().getId());
        
        // Publish DeliveryDeliveredEvent
        publishDeliveryDeliveredEvent(updated);
        
        return deliveryMapper.toResponse(updated);
    }
    
    /**
     * Create delivery from OrderCreatedEvent (internal use by Kafka consumer)
     */
    @Transactional
    public void createDeliveryFromEvent(CreateDeliveryRequestDTO request, Long userId) {
        log.info("Creating delivery from event for order: {}, userId: {}", request.orderId(), userId);
        
        // Check if delivery already exists
        if (deliveryRepository.findByOrderId(request.orderId()).isPresent()) {
            log.warn("Delivery already exists for order: {}, skipping", request.orderId());
            return;
        }
        
        Delivery delivery = deliveryMapper.toEntity(request);
        delivery.setUserId(userId);
        delivery.setStatus(DeliveryStatus.PENDING);
        
        Delivery saved = deliveryRepository.save(delivery);
        
        // Try to assign courier immediately
        boolean assigned = assignmentService.assignCourierToDelivery(saved);
        
        if (assigned) {
            log.info("Delivery {} automatically assigned to courier {} (from event)", 
                     saved.getId(), saved.getCourier().getId());
            // Publish DeliveryAssignedEvent
            publishDeliveryAssignedEvent(saved);
        } else {
            log.warn("Delivery {} pending - no available courier found (from event)", saved.getId());
        }
    }
    
    /**
     * Cancel delivery
     */
    @Transactional
    public DeliveryResponseDTO cancelDelivery(Long deliveryId, String reason) {
        Delivery delivery = findDeliveryById(deliveryId);
        
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new InvalidDeliveryStateException("Cannot cancel delivered delivery");
        }
        
        delivery.markCancelled();
        releaseCourier(delivery);
        
        Delivery updated = deliveryRepository.save(delivery);
        
        log.info("Delivery {} cancelled. Reason: {}", deliveryId, reason);
        
        return deliveryMapper.toResponse(updated);
    }
    
    /**
     * Add customer rating to delivery
     */
    @Transactional
    public DeliveryResponseDTO addRating(Long deliveryId, DeliveryRatingDTO ratingDTO) {
        Delivery delivery = findDeliveryById(deliveryId);
        
        if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new InvalidDeliveryStateException(
                "Can only rate completed deliveries"
            );
        }
        
        delivery.addRating(ratingDTO.rating(), ratingDTO.feedback());
        
        // Update courier rating separately (separation of concerns)
        if (delivery.getCourier() != null) {
            delivery.getCourier().updateRating(ratingDTO.rating().doubleValue());
        }
        
        Delivery updated = deliveryRepository.save(delivery);
        
        log.info("Rating added to delivery {}: {} stars", deliveryId, ratingDTO.rating());
        
        return deliveryMapper.toResponse(updated);
    }
    
    /**
     * Retry assignment for pending deliveries
     */
    @Transactional
    public void retryPendingAssignments() {
        List<Delivery> pendingDeliveries = deliveryRepository.findPendingDeliveries();
        
        log.info("Retrying assignment for {} pending deliveries", pendingDeliveries.size());
        
        for (Delivery delivery : pendingDeliveries) {
            boolean assigned = assignmentService.assignCourierToDelivery(delivery);
            if (assigned) {
                log.info("Pending delivery {} assigned to courier {}", 
                         delivery.getId(), delivery.getCourier().getId());
            }
        }
    }
    
    /**
     * Helper: Release courier (set back to AVAILABLE)
     */
    private void releaseCourier(Delivery delivery) {
        if (delivery.getCourier() != null) {
            delivery.getCourier().updateStatus(CourierStatus.AVAILABLE);
        }
    }
    
    @Transactional(readOnly = true)
    protected Delivery findDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(
                    "Delivery not found with id: " + id
                ));
    }
    
    /**
     * Publish DeliveryAssignedEvent to Kafka
     */
    private void publishDeliveryAssignedEvent(Delivery delivery) {
        try {
            com.fooddelivery.common.event.DeliveryAssignedEvent event = com.fooddelivery.common.event.DeliveryAssignedEvent.builder()
                    .deliveryId(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .courierId(delivery.getCourier().getId())
                    .userId(delivery.getUserId())
                    .estimatedTimeMinutes(delivery.getEstimatedTimeMinutes())
                    .build();
            
            deliveryEventProducer.publishDeliveryAssigned(event);
        } catch (Exception e) {
            log.error("Failed to publish DeliveryAssignedEvent for delivery {}", delivery.getId(), e);
        }
    }
    
    /**
     * Publish DeliveryDeliveredEvent to Kafka
     */
    private void publishDeliveryDeliveredEvent(Delivery delivery) {
        try {
            com.fooddelivery.common.event.DeliveryDeliveredEvent event = com.fooddelivery.common.event.DeliveryDeliveredEvent.builder()
                    .deliveryId(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .courierId(delivery.getCourier().getId())
                    .userId(delivery.getUserId())
                    .build();
            
            deliveryEventProducer.publishDeliveryDelivered(event);
        } catch (Exception e) {
            log.error("Failed to publish DeliveryDeliveredEvent for delivery {}", delivery.getId(), e);
        }
    }
}

