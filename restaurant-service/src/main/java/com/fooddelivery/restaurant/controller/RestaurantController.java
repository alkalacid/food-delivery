package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.RestaurantRequestDTO;
import com.fooddelivery.restaurant.dto.RestaurantResponseDTO;
import com.fooddelivery.restaurant.dto.RestaurantSearchDTO;
import com.fooddelivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Validated
public class RestaurantController {
    
    private final RestaurantService restaurantService;
    
    @GetMapping
    public List<RestaurantResponseDTO> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }
    
    @GetMapping("/{id}")
    public RestaurantResponseDTO getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }
    
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    public List<RestaurantResponseDTO> getRestaurantsByOwner(@PathVariable Long ownerId) {
        return restaurantService.getRestaurantsByOwner(ownerId);
    }
    
    @PostMapping("/search")
    public List<RestaurantResponseDTO> searchNearbyRestaurants(@Valid @RequestBody RestaurantSearchDTO searchDTO) {
        return restaurantService.searchNearbyRestaurants(searchDTO);
    }
    
    @GetMapping("/search/name")
    public List<RestaurantResponseDTO> searchByName(
            @RequestParam @NotBlank(message = "Search query is required") String query) {
        return restaurantService.searchByName(query);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponseDTO createRestaurant(@Valid @RequestBody RestaurantRequestDTO request) {
        return restaurantService.createRestaurant(request);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public RestaurantResponseDTO updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDTO request) {
        return restaurantService.updateRestaurant(id, request);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
    }
}

