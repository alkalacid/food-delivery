package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.restaurant.dto.MenuItemRequestDTO;
import com.fooddelivery.restaurant.dto.MenuItemResponseDTO;
import com.fooddelivery.restaurant.enums.MenuCategory;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.MenuItemNotFoundException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.exception.UnauthorizedAccessException;
import com.fooddelivery.restaurant.mapper.MenuItemMapper;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
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
public class MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper menuItemMapper;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId")
    public List<MenuItemResponseDTO> getMenuByRestaurant(Long restaurantId) {
        log.info("Fetching menu for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId + '_available'")
    public List<MenuItemResponseDTO> getAvailableMenuByRestaurant(Long restaurantId) {
        log.info("Fetching available menu for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId + '_category_' + #category")
    public List<MenuItemResponseDTO> getMenuByCategory(Long restaurantId, MenuCategory category) {
        log.info("Fetching menu for restaurant: {} and category: {}", restaurantId, category);
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId + '_categories'")
    public List<MenuCategory> getCategoriesByRestaurant(Long restaurantId) {
        log.info("Fetching categories for restaurant: {}", restaurantId);
        return menuItemRepository.findCategoriesByRestaurantId(restaurantId);
    }
    
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public MenuItemResponseDTO createMenuItem(Long restaurantId, MenuItemRequestDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Creating new menu item for restaurant: {}", restaurantId);
        
        Restaurant restaurant = findRestaurantAndCheckOwnership(restaurantId, currentUserId);
        
        MenuItem menuItem = menuItemMapper.toEntity(request);
        menuItem.setRestaurant(restaurant);
        
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item created with id: {}", saved.getId());
        
        return menuItemMapper.toResponse(saved);
    }
    
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public MenuItemResponseDTO updateMenuItem(Long restaurantId, Long itemId, MenuItemRequestDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Updating menu item: {} for restaurant: {}", itemId, restaurantId);
        
        findRestaurantAndCheckOwnership(restaurantId, currentUserId);
        
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        menuItemMapper.updateEntity(request, menuItem);
        MenuItem updated = menuItemRepository.save(menuItem);
        
        log.info("Menu item updated: {}", itemId);
        return menuItemMapper.toResponse(updated);
    }
    
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public void deleteMenuItem(Long restaurantId, Long itemId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Deleting menu item: {} from restaurant: {}", itemId, restaurantId);
        
        findRestaurantAndCheckOwnership(restaurantId, currentUserId);
        
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        menuItemRepository.delete(menuItem);
        log.info("Menu item deleted: {}", itemId);
    }
    
    @Transactional(readOnly = true)
    private Restaurant findRestaurantAndCheckOwnership(Long restaurantId, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findActiveById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));
        
        if (!restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to manage this restaurant's menu");
        }
        
        return restaurant;
    }
}

