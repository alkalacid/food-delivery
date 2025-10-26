package com.fooddelivery.user.controller;

import com.fooddelivery.user.dto.AddressRequestDTO;
import com.fooddelivery.user.dto.AddressResponseDTO;
import com.fooddelivery.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AddressController {
    
    private final AddressService addressService;
    
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses() {
        List<AddressResponseDTO> addresses = addressService.getUserAddresses();
        return ResponseEntity.ok(addresses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable Long id) {
        AddressResponseDTO address = addressService.getAddressById(id);
        return ResponseEntity.ok(address);
    }
    
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(address);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}

