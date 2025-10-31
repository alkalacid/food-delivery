package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.MenuItemRequestDTO;
import com.fooddelivery.restaurant.dto.MenuItemResponseDTO;
import com.fooddelivery.restaurant.enums.MenuCategory;
import com.fooddelivery.restaurant.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Validated
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    
    @GetMapping
    public List<MenuItemResponseDTO> getRestaurantMenu(@PathVariable Long restaurantId) {
        return menuItemService.getAvailableMenuByRestaurant(restaurantId);
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public List<MenuItemResponseDTO> getAllMenuItems(@PathVariable Long restaurantId) {
        return menuItemService.getMenuByRestaurant(restaurantId);
    }
    
    @GetMapping("/category/{category}")
    public List<MenuItemResponseDTO> getMenuByCategory(
            @PathVariable Long restaurantId,
            @PathVariable MenuCategory category) {
        return menuItemService.getMenuByCategory(restaurantId, category);
    }
    
    @GetMapping("/categories")
    public List<MenuCategory> getCategories(@PathVariable Long restaurantId) {
        return menuItemService.getCategoriesByRestaurant(restaurantId);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponseDTO createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequestDTO request) {
        return menuItemService.createMenuItem(restaurantId, request);
    }
    
    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public MenuItemResponseDTO updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequestDTO request) {
        return menuItemService.updateMenuItem(restaurantId, itemId, request);
    }
    
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        menuItemService.deleteMenuItem(restaurantId, itemId);
    }
}

