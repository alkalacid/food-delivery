package com.fooddelivery.delivery.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.delivery.dto.CourierRequestDTO;
import com.fooddelivery.delivery.dto.CourierResponseDTO;
import com.fooddelivery.delivery.dto.LocationUpdateDTO;
import com.fooddelivery.delivery.entity.Courier;
import com.fooddelivery.delivery.enums.CourierStatus;
import com.fooddelivery.delivery.exception.CourierAlreadyExistsException;
import com.fooddelivery.delivery.exception.CourierNotFoundException;
import com.fooddelivery.delivery.mapper.CourierMapper;
import com.fooddelivery.delivery.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierService {
    
    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;
    private final LocationTrackingService locationTrackingService;
    
    /**
     * Register new courier
     */
    @Transactional
    public CourierResponseDTO registerCourier(CourierRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Registering new courier for user: {}", userId);
        
        Courier courier = courierMapper.toEntity(request);
        courier.setUserId(userId);
        courier.setStatus(CourierStatus.OFFLINE);
        
        try {
            Courier saved = courierRepository.save(courier);
            log.info("Courier registered with id: {}", saved.getId());
            return courierMapper.toResponse(saved);
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Check if it's due to duplicate userId
            if (courierRepository.existsByUserId(userId)) {
                throw new CourierAlreadyExistsException("User is already registered as courier");
            }
            throw e;
        }
    }
    
    /**
     * Get current courier profile
     */
    @Transactional(readOnly = true)
    public CourierResponseDTO getCurrentCourier() {
        Courier courier = getCurrentUserCourier();
        return courierMapper.toResponse(courier);
    }
    
    /**
     * Get courier by ID
     */
    @Transactional(readOnly = true)
    public CourierResponseDTO getCourierById(Long id) {
        Courier courier = findActiveCourierById(id);
        return courierMapper.toResponse(courier);
    }
    
    /**
     * Update courier profile
     */
    @Transactional
    public CourierResponseDTO updateCourier(CourierRequestDTO request) {
        Courier courier = getCurrentUserCourier();
        
        courierMapper.updateEntity(request, courier);
        Courier updated = courierRepository.save(courier);
        
        log.info("Courier profile updated: {}", courier.getId());
        return courierMapper.toResponse(updated);
    }
    
    /**
     * Update courier status
     */
    @Transactional
    public CourierResponseDTO updateStatus(CourierStatus newStatus) {
        Courier courier = getCurrentUserCourier();
        
        log.info("Updating courier {} status from {} to {}", 
                 courier.getId(), courier.getStatus(), newStatus);
        
        courier.updateStatus(newStatus);
        Courier updated = courierRepository.save(courier);
        
        return courierMapper.toResponse(updated);
    }
    
    /**
     * Update courier location (stores in both Redis and DB)
     */
    @Transactional
    public void updateLocation(LocationUpdateDTO location) {
        Courier courier = getCurrentUserCourier();
        
        // Update in Redis for real-time tracking (async)
        locationTrackingService.updateCourierLocation(
            courier.getId(), 
            location.latitude(), 
            location.longitude()
        );
        
        // Update in DB for reference
        courier.updateLocation(location.latitude(), location.longitude());
        courierRepository.save(courier);
        
        log.debug("Courier {} location updated: ({}, {})", 
                  courier.getId(), location.latitude(), location.longitude());
    }
    
    /**
     * Get all available couriers
     */
    @Transactional(readOnly = true)
    public List<CourierResponseDTO> getAvailableCouriers() {
        return courierRepository.findAvailableCouriers().stream()
                .map(courierMapper::toResponse)
                .toList();
    }
    
    /**
     * Get couriers by status
     */
    @Transactional(readOnly = true)
    public List<CourierResponseDTO> getCouriersByStatus(CourierStatus status) {
        return courierRepository.findByStatus(status).stream()
                .map(courierMapper::toResponse)
                .toList();
    }
    
    /**
     * Delete courier profile (soft delete)
     */
    @Transactional
    public void deleteCourier() {
        Courier courier = getCurrentUserCourier();
        
        courier.softDelete();
        courierRepository.save(courier);
        
        log.info("Courier profile soft deleted: {}", courier.getId());
    }
    
    /**
     * Helper: Get current user's courier profile
     */
    @Transactional(readOnly = true)
    protected Courier getCurrentUserCourier() {
        Long userId = SecurityUtils.getCurrentUserId();
        return courierRepository.findByUserId(userId)
                .orElseThrow(() -> new CourierNotFoundException("Courier profile not found"));
    }
    
    @Transactional(readOnly = true)
    protected Courier findActiveCourierById(Long id) {
        return courierRepository.findActiveById(id)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found with id: " + id));
    }
}

