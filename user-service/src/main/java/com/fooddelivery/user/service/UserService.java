package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.UpdateProfileRequestDTO;
import com.fooddelivery.user.dto.UserResponseDTO;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.exception.UserAlreadyExistsException;
import com.fooddelivery.user.exception.UserNotFoundException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        Long userId = getCurrentUserId();
        return getUserById(userId);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        
        return userMapper.toResponse(user);
    }
    
    @Transactional
    @CacheEvict(value = "users", key = "#root.target.getCurrentUserId()")
    public UserResponseDTO updateProfile(UpdateProfileRequestDTO request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        log.info("Updating profile for user: {}", userId);
        
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            user.setEmail(request.email());
        }
        
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            user.setPhone(request.phone());
        }
        
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        
        try {
            User updatedUser = userRepository.save(user);
            log.info("Profile updated successfully for user: {}", userId);
            return userMapper.toResponse(updatedUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Profile update failed for user: {} - duplicate data", userId);
            if (request.email() != null && userRepository.existsByEmail(request.email())) {
                throw new UserAlreadyExistsException("Email already in use");
            }
            if (request.phone() != null && userRepository.existsByPhone(request.phone())) {
                throw new UserAlreadyExistsException("Phone number already in use");
            }
            throw new UserAlreadyExistsException("Profile update failed - duplicate data");
        }
    }
    
    @Transactional
    @CacheEvict(value = "users", key = "#root.target.getCurrentUserId()")
    public void deleteAccount() {
        Long userId = getCurrentUserId();
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        log.info("Soft deleting user account: {}", userId);
        user.softDelete();
        userRepository.save(user);
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("No authenticated user found");
    }
}

