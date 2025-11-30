package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.entity.Courier;
import com.fooddelivery.delivery.enums.CourierStatus;
import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.repository.CourierRepository;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for intelligent delivery assignment to couriers
 * Algorithm considers:
 * - Courier proximity to pickup location
 * - Courier availability status
 * - Courier rating
 * - Current workload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAssignmentService {
    
    private static final double MAX_RATING = 5.0;
    private static final int MAX_EXPERIENCE_DELIVERIES = 100;
    private static final double METERS_IN_KM = 1000.0;
    private static final double WEIGHT_DISTANCE = 0.5;
    private static final double WEIGHT_RATING = 0.3;
    private static final double WEIGHT_EXPERIENCE = 0.2;
    private static final double AVERAGE_SPEED_KMH = 20.0;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int BUFFER_TIME_MINUTES = 10;
    
    private final CourierRepository courierRepository;
    private final DeliveryRepository deliveryRepository;
    private final LocationTrackingService locationTrackingService;
    
    private static final double MAX_SEARCH_RADIUS_KM = 10.0; // 10km radius
    private static final double MIN_RATING = 3.0; // Minimum courier rating
    
    /**
     * Automatically assign the best available courier to delivery
     * 
     * @return true if courier was assigned, false if no suitable courier found
     */
    @Transactional
    public boolean assignCourierToDelivery(Delivery delivery) {
        log.info("Attempting to assign courier to delivery: {}", delivery.getId());
        
        // Find best courier
        Optional<Courier> bestCourier = findBestCourier(
            delivery.getPickupLatitude(),
            delivery.getPickupLongitude()
        );
        
        if (bestCourier.isEmpty()) {
            log.warn("No suitable courier found for delivery: {}", delivery.getId());
            return false;
        }
        
        Courier courier = bestCourier.get();
        
        // Assign courier and update statuses
        delivery.assignCourier(courier);
        courier.updateStatus(CourierStatus.BUSY);
        
        // Calculate estimated distance and time
        Double distance = locationTrackingService.calculateDistance(
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            delivery.getPickupLatitude(),
            delivery.getPickupLongitude()
        );
        
        delivery.setEstimatedDistanceMeters(distance.intValue());
        delivery.setEstimatedTimeMinutes(calculateEstimatedTime(distance));
        
        deliveryRepository.save(delivery);
        courierRepository.save(courier);
        
        log.info("Courier {} assigned to delivery {}", courier.getId(), delivery.getId());
        return true;
    }
    
    /**
     * Find best courier using intelligent algorithm
     */
    private Optional<Courier> findBestCourier(Double pickupLat, Double pickupLon) {
        // Step 1: Get nearby couriers from Redis (fastest, real-time)
        List<Long> nearbyCourierIds = locationTrackingService.findNearestCouriers(
            pickupLat, 
            pickupLon, 
            MAX_SEARCH_RADIUS_KM
        );
        
        if (nearbyCourierIds.isEmpty()) {
            log.debug("No couriers found in Redis within radius");
            // Fallback: get all available couriers from DB
            return findBestCourierFromDb();
        }
        
        // Step 2: Filter available couriers from DB
        List<Courier> availableCouriers = nearbyCourierIds.stream()
                .map(courierRepository::findActiveById)
                .flatMap(Optional::stream)
                .filter(this::isCourierAvailable)
                .toList();
        
        if (availableCouriers.isEmpty()) {
            log.debug("No available couriers found in nearby list");
            return Optional.empty();
        }
        
        // Step 3: Score and select best courier
        return availableCouriers.stream()
                .max((c1, c2) -> Double.compare(
                    calculateCourierScore(c1, pickupLat, pickupLon),
                    calculateCourierScore(c2, pickupLat, pickupLon)
                ));
    }
    
    /**
     * Fallback: find best courier from DB when Redis has no data
     */
    private Optional<Courier> findBestCourierFromDb() {
        List<Courier> allAvailableCouriers = courierRepository.findByStatus(CourierStatus.AVAILABLE);
        
        return allAvailableCouriers.stream()
                .filter(this::isCourierAvailable)
                .max(Comparator.comparingDouble(Courier::getAverageRating));
    }
    
    /**
     * Check if courier is available for assignment
     */
    private boolean isCourierAvailable(Courier courier) {
        // Check status
        if (courier.getStatus() != CourierStatus.AVAILABLE) {
            return false;
        }
        
        // Check if courier has current location
        if (courier.getCurrentLatitude() == null || courier.getCurrentLongitude() == null) {
            return false;
        }
        
        // Check rating threshold
        if (courier.getAverageRating() < MIN_RATING && courier.getTotalDeliveries() > 10) {
            return false;
        }
        
        // Check if courier already has active delivery
        Optional<Delivery> activeDelivery = deliveryRepository.findActiveDeliveryByCourier(courier.getId());
        return activeDelivery.isEmpty();
    }
    
    /**
     * Calculate courier score for assignment priority
     * Higher score = better candidate
     * Factors:
     * - Distance (closer is better)
     * - Rating (higher is better)
     * - Experience (more deliveries is better)
     */
    private double calculateCourierScore(Courier courier, Double pickupLat, Double pickupLon) {
        // Distance score (inverse - closer is better)
        double distance = locationTrackingService.calculateDistance(
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            pickupLat,
            pickupLon
        );
        double distanceScore = 1.0 / (1.0 + (distance / METERS_IN_KM));
        double ratingScore = courier.getAverageRating() / MAX_RATING;
        double experienceScore = Math.min(courier.getTotalDeliveries(), MAX_EXPERIENCE_DELIVERIES) / (double) MAX_EXPERIENCE_DELIVERIES;
        
        return (distanceScore * WEIGHT_DISTANCE) +
               (ratingScore * WEIGHT_RATING) +
               (experienceScore * WEIGHT_EXPERIENCE);
    }
    
    private Integer calculateEstimatedTime(Double distanceMeters) {
        double distanceKm = distanceMeters / METERS_IN_KM;
        double timeHours = distanceKm / AVERAGE_SPEED_KMH;
        int timeMinutes = (int) Math.ceil(timeHours * MINUTES_IN_HOUR);
        
        return timeMinutes + BUFFER_TIME_MINUTES;
    }
}

