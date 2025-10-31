package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    @Query("SELECT r FROM Restaurant r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Restaurant> findActiveById(@Param("id") Long id);
    
    @Query("SELECT r FROM Restaurant r WHERE r.deletedAt IS NULL AND r.isActive = true")
    List<Restaurant> findAllActive();
    
    @Query("SELECT r FROM Restaurant r WHERE r.ownerId = :ownerId AND r.deletedAt IS NULL")
    List<Restaurant> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT r FROM Restaurant r WHERE r.cuisineType = :cuisineType " +
           "AND r.deletedAt IS NULL AND r.isActive = true")
    List<Restaurant> findByCuisineType(@Param("cuisineType") String cuisineType);
    
    /**
     * Optimized geolocation search using Haversine formula with bounding box pre-filter.
     * 1. Bounding box filter (uses index on lat/lon) - fast rectangular approximation
     * 2. Haversine formula (computed once via CTE) - precise distance
     * 3. Optional cuisine type filter
     * 4. LIMIT for performance
     * 
     * Finds restaurants within specified radius (in kilometers) from given coordinates.
     * If cuisineType is provided, filters results by cuisine type.
     */
    @Query(value = 
           "WITH nearby AS (" +
           "    SELECT *, " +
           "           (6371 * ACOS(" +
           "               COS(RADIANS(:latitude)) * COS(RADIANS(latitude)) * " +
           "               COS(RADIANS(longitude) - RADIANS(:longitude)) + " +
           "               SIN(RADIANS(:latitude)) * SIN(RADIANS(latitude))" +
           "           )) AS distance_km " +
           "    FROM restaurants " +
           "    WHERE deleted_at IS NULL " +
           "      AND is_active = 1 " +
           // Bounding box pre-filter (approximately 1 degree ≈ 111 km)
           "      AND latitude BETWEEN :latitude - (:radiusKm / 111.0) " +
           "                       AND :latitude + (:radiusKm / 111.0) " +
           "      AND longitude BETWEEN :longitude - (:radiusKm / (111.0 * COS(RADIANS(:latitude)))) " +
           "                        AND :longitude + (:radiusKm / (111.0 * COS(RADIANS(:latitude)))) " +
           "      AND (:cuisineType IS NULL OR LOWER(cuisine_type) = LOWER(:cuisineType)) " +
           ") " +
           "SELECT * FROM nearby " +
           "WHERE distance_km <= :radiusKm " +
           "ORDER BY distance_km " +
           "OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY",
           nativeQuery = true)
    List<Restaurant> findNearbyRestaurants(
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("radiusKm") double radiusKm,
        @Param("cuisineType") String cuisineType
    );
    
    @Query("SELECT r FROM Restaurant r WHERE r.deletedAt IS NULL AND r.isActive = true " +
           "AND LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Restaurant> searchByName(@Param("searchTerm") String searchTerm);
}

