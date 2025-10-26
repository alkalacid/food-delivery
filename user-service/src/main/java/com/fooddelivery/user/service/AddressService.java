package com.fooddelivery.user.service;

import com.fooddelivery.user.config.AddressProperties;
import com.fooddelivery.user.dto.AddressRequestDTO;
import com.fooddelivery.user.dto.AddressResponseDTO;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.entity.UserAddress;
import com.fooddelivery.user.exception.AddressLimitExceededException;
import com.fooddelivery.user.exception.AddressNotFoundException;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.repository.UserAddressRepository;
import com.fooddelivery.user.repository.UserRepository;
import com.fooddelivery.user.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {
    
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final AddressProperties addressProperties;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "addresses", key = "#root.target.getCurrentUserId()")
    public List<AddressResponseDTO> getUserAddresses() {
        Long userId = getCurrentUserId();
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddressById(Long addressId) {
        Long userId = getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        
        return addressMapper.toResponse(address);
    }
    
    @Transactional
    @CacheEvict(value = "addresses", key = "#root.target.getCurrentUserId()")
    public AddressResponseDTO createAddress(AddressRequestDTO request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        log.info("Creating new address for user: {}", userId);
        
        long currentCount = addressRepository.countByUserId(userId);
        int maxAllowed = addressProperties.getMaxPerUser();
        if (currentCount >= maxAllowed) {
            throw new AddressLimitExceededException(
                "Maximum number of addresses (" + maxAllowed + ") reached");
        }
        
        // If this is marked as default or there are no other addresses, set as default
        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault()) || currentCount == 0;
        
        if (shouldBeDefault) {
            // Reset other default addresses
            addressRepository.resetDefaultForUser(userId);
        }
        
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        address.setIsDefault(shouldBeDefault);
        address.setLabel(request.label());
        
        UserAddress savedAddress = addressRepository.save(address);
        log.info("Address created successfully with id: {}", savedAddress.getId());
        
        return addressMapper.toResponse(savedAddress);
    }
    
    @Transactional
    @CacheEvict(value = "addresses", key = "#root.target.getCurrentUserId()")
    public AddressResponseDTO updateAddress(Long addressId, AddressRequestDTO request) {
        Long userId = getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        
        log.info("Updating address: {} for user: {}", addressId, userId);
        
        if (Boolean.TRUE.equals(request.isDefault()) && !address.getIsDefault()) {
            addressRepository.resetDefaultForUser(userId);
        }
        
        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        if (request.isDefault() != null) {
            address.setIsDefault(request.isDefault());
        }
        if (request.label() != null) {
            address.setLabel(request.label());
        }
        
        UserAddress updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully: {}", addressId);
        
        return addressMapper.toResponse(updatedAddress);
    }
    
    @Transactional
    @CacheEvict(value = "addresses", key = "#root.target.getCurrentUserId()")
    public void deleteAddress(Long addressId) {
        Long userId = getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        
        log.info("Deleting address: {} for user: {}", addressId, userId);
        addressRepository.delete(address);
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("No authenticated user found");
    }
}

