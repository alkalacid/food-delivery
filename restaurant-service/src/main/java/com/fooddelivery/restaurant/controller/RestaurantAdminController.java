package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.MenuItemRequestDTO;
import com.fooddelivery.restaurant.dto.MenuItemResponseDTO;
import com.fooddelivery.restaurant.dto.RestaurantRequestDTO;
import com.fooddelivery.restaurant.dto.RestaurantResponseDTO;
import com.fooddelivery.restaurant.service.MenuItemService;
import com.fooddelivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for restaurant management
 * Only accessible by RESTAURANT_OWNER users for their own restaurants
 */
@RestController
@RequestMapping("/api/restaurant/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantAdminController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    /**
     * Update restaurant information
     */
    @PutMapping("/restaurants/{restaurantId}")
    public RestaurantResponseDTO updateRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantRequestDTO request) {
        log.info("Restaurant owner updating restaurant: {}", restaurantId);
        return restaurantService.updateRestaurant(restaurantId, request);
    }

    /**
     * Get restaurant menu items (for management)
     */
    @GetMapping("/restaurants/{restaurantId}/menu")
    public List<MenuItemResponseDTO> getRestaurantMenu(@PathVariable Long restaurantId) {
        log.info("Restaurant owner fetching menu for restaurant: {}", restaurantId);
        return menuItemService.getMenuByRestaurant(restaurantId);
    }

    /**
     * Add new menu item
     */
    @PostMapping("/restaurants/{restaurantId}/menu")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponseDTO createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequestDTO request) {
        log.info("Restaurant owner creating menu item for restaurant: {}", restaurantId);
        return menuItemService.createMenuItem(restaurantId, request);
    }

    /**
     * Update menu item
     */
    @PutMapping("/menu-items/{menuItemId}")
    public MenuItemResponseDTO updateMenuItem(
            @PathVariable Long menuItemId,
            @Valid @RequestBody MenuItemRequestDTO request) {
        log.info("Restaurant owner updating menu item: {}", menuItemId);
        return menuItemService.updateMenuItem(menuItemId, request);
    }

    /**
     * Delete menu item
     */
    @DeleteMapping("/menu-items/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long menuItemId) {
        log.info("Restaurant owner deleting menu item: {}", menuItemId);
        menuItemService.deleteMenuItem(menuItemId);
    }

    /**
     * Toggle menu item availability
     */
    @PatchMapping("/menu-items/{menuItemId}/availability")
    public MenuItemResponseDTO toggleMenuItemAvailability(@PathVariable Long menuItemId) {
        log.info("Restaurant owner toggling availability for menu item: {}", menuItemId);
        return menuItemService.toggleAvailability(menuItemId);
    }
}

