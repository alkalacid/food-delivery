package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.restaurant.dto.RestaurantRequestDTO;
import com.fooddelivery.restaurant.dto.RestaurantResponseDTO;
import com.fooddelivery.restaurant.dto.RestaurantSearchDTO;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.exception.UnauthorizedAccessException;
import com.fooddelivery.restaurant.mapper.RestaurantMapper;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {
    
    private static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;
    
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponseDTO getRestaurantById(Long id) {
        log.info("Fetching restaurant with id: {}", id);
        Restaurant restaurant = findActiveRestaurantById(id);
        return restaurantMapper.toResponse(restaurant);
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> getAllRestaurants() {
        log.info("Fetching all active restaurants");
        return restaurantRepository.findAllActive().stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> getRestaurantsByOwner(Long ownerId) {
        log.info("Fetching restaurants for owner: {}", ownerId);
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "restaurantSearch", key = "#searchDTO.latitude + '_' + #searchDTO.longitude + '_' + #searchDTO.radiusKm + '_' + #searchDTO.cuisineType")
    public List<RestaurantResponseDTO> searchNearbyRestaurants(RestaurantSearchDTO searchDTO) {
        log.info("Searching restaurants near lat: {}, lon: {}, radius: {} km, cuisine: {}",
                searchDTO.latitude(), searchDTO.longitude(), searchDTO.radiusKm(), searchDTO.cuisineType());
        
        double radiusKm = searchDTO.radiusKm() != null ? searchDTO.radiusKm() : DEFAULT_SEARCH_RADIUS_KM;
        String cuisineType = (searchDTO.cuisineType() != null && !searchDTO.cuisineType().isEmpty()) 
                              ? searchDTO.cuisineType() 
                              : null;
        
        List<Restaurant> restaurants = restaurantRepository.findNearbyRestaurants(
                searchDTO.latitude(),
                searchDTO.longitude(),
                radiusKm,
                cuisineType
        );
        
        return restaurants.stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> searchByName(String searchTerm) {
        log.info("Searching restaurants by name: {}", searchTerm);
        return restaurantRepository.searchByName(searchTerm).stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }
    
    @Transactional
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("Creating new restaurant: {} for owner: {}", request.name(), ownerId);
        
        Restaurant restaurant = restaurantMapper.toEntity(request);
        restaurant.setOwnerId(ownerId);
        
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created with id: {}", saved.getId());
        
        return restaurantMapper.toResponse(saved);
    }
    
    @Transactional
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Updating restaurant: {}", id);
        
        Restaurant restaurant = findRestaurantAndCheckOwnership(id, currentUserId);
        
        restaurantMapper.updateEntity(request, restaurant);
        Restaurant updated = restaurantRepository.save(restaurant);
        
        log.info("Restaurant updated: {}", id);
        return restaurantMapper.toResponse(updated);
    }
    
    @Transactional
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public void deleteRestaurant(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Deleting restaurant: {}", id);
        
        Restaurant restaurant = findRestaurantAndCheckOwnership(id, currentUserId);
        
        restaurant.softDelete();
        restaurantRepository.save(restaurant);
        log.info("Restaurant soft deleted: {}", id);
    }
    
    @Transactional(readOnly = true)
    private Restaurant findActiveRestaurantById(Long id) {
        return restaurantRepository.findActiveById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    private Restaurant findRestaurantAndCheckOwnership(Long restaurantId, Long currentUserId) {
        Restaurant restaurant = findActiveRestaurantById(restaurantId);
        
        if (!restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to manage this restaurant");
        }
        
        return restaurant;
    }
}

