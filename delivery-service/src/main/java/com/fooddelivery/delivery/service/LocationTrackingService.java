package com.fooddelivery.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for real-time courier location tracking using Redis Geo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String COURIER_LOCATION_KEY = "courier:locations";
    private static final long LOCATION_TTL_HOURS = 24;
    private static final int MAX_NEARBY_COURIERS = 10;
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    
    /**
     * Update courier location in Redis (async for performance)
     */
    @Async("locationExecutor")
    public void updateCourierLocation(Long courierId, Double latitude, Double longitude) {
        try {
            Point location = new Point(longitude, latitude);
            
            redisTemplate.opsForGeo().add(
                COURIER_LOCATION_KEY,
                location,
                courierId.toString()
            );
            
            // Set TTL on the key
            redisTemplate.expire(COURIER_LOCATION_KEY, LOCATION_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Updated location for courier {}: lat={}, lon={}", 
                     courierId, latitude, longitude);
                     
        } catch (Exception e) {
            // Location update failures should NOT break courier app
            log.error("Failed to update location for courier {}: {}", courierId, e.getMessage());
        }
    }
    
    /**
     * Find nearest available couriers within radius
     * 
     * @param latitude center point latitude
     * @param longitude center point longitude
     * @param radiusKm search radius in kilometers
     * @return list of courier IDs sorted by distance
     */
    public List<Long> findNearestCouriers(Double latitude, Double longitude, Double radiusKm) {
        try {
            Point center = new Point(longitude, latitude);
            Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
            Circle searchArea = new Circle(center, radius);
            
            GeoResults<RedisGeoCommands.GeoLocation<String>> results = 
                redisTemplate.opsForGeo().radius(
                    COURIER_LOCATION_KEY,
                    searchArea,
                    RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .sortAscending()
                        .limit(MAX_NEARBY_COURIERS)
                );
            
            if (results == null) {
                return List.of();
            }
            
            return results.getContent().stream()
                    .map(result -> Long.parseLong(result.getContent().getName()))
                    .toList();
                    
        } catch (Exception e) {
            log.error("Failed to find nearest couriers: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get distance between two points (Haversine formula)
     * 
     * @return distance in meters
     */
    public Double calculateDistance(
            Double lat1, Double lon1, 
            Double lat2, Double lon2) {
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_METERS * c;
    }
    
    /**
     * Remove courier location from tracking (when going offline)
     */
    @Async("locationExecutor")
    public void removeCourierLocation(Long courierId) {
        try {
            redisTemplate.opsForGeo().remove(COURIER_LOCATION_KEY, courierId.toString());
            log.debug("Removed location for courier {}", courierId);
        } catch (Exception e) {
            log.error("Failed to remove location for courier {}: {}", courierId, e.getMessage());
        }
    }
}

