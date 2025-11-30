package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.CourierRequestDTO;
import com.fooddelivery.delivery.dto.CourierResponseDTO;
import com.fooddelivery.delivery.dto.LocationUpdateDTO;
import com.fooddelivery.delivery.enums.CourierStatus;
import com.fooddelivery.delivery.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
@Validated
public class CourierController {
    
    private final CourierService courierService;
    
    /**
     * Register as courier
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('COURIER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CourierResponseDTO registerCourier(@Valid @RequestBody CourierRequestDTO request) {
        return courierService.registerCourier(request);
    }
    
    /**
     * Get current courier profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('COURIER')")
    public CourierResponseDTO getCurrentCourier() {
        return courierService.getCurrentCourier();
    }
    
    /**
     * Get courier by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CourierResponseDTO getCourierById(@PathVariable Long id) {
        return courierService.getCourierById(id);
    }
    
    /**
     * Update courier profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('COURIER')")
    public CourierResponseDTO updateCourier(@Valid @RequestBody CourierRequestDTO request) {
        return courierService.updateCourier(request);
    }
    
    /**
     * Update courier status
     */
    @PatchMapping("/me/status")
    @PreAuthorize("hasRole('COURIER')")
    public CourierResponseDTO updateStatus(@RequestParam CourierStatus status) {
        return courierService.updateStatus(status);
    }
    
    /**
     * Update courier location
     */
    @PostMapping("/me/location")
    @PreAuthorize("hasRole('COURIER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocation(@Valid @RequestBody LocationUpdateDTO location) {
        courierService.updateLocation(location);
    }
    
    /**
     * Get all available couriers (admin only)
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CourierResponseDTO> getAvailableCouriers() {
        return courierService.getAvailableCouriers();
    }
    
    /**
     * Get couriers by status (admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CourierResponseDTO> getCouriersByStatus(@PathVariable CourierStatus status) {
        return courierService.getCouriersByStatus(status);
    }
    
    /**
     * Delete courier profile
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('COURIER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourier() {
        courierService.deleteCourier();
    }
}

