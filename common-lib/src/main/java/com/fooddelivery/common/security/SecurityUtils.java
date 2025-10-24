package com.fooddelivery.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related operations
 */
public class SecurityUtils {
    
    /**
     * Get current authenticated user ID from SecurityContext
     * 
     * @return user ID from JWT token
     * @throws IllegalStateException if no authentication found or principal is invalid
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        Object principal = authentication.getPrincipal();
        
        // Handle UserPrincipal (from user-service pattern)
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        
        // Handle JWT claims (standard pattern)
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Long userId = jwt.getClaim("userId");
            if (userId != null) {
                return userId;
            }
        }
        
        // Try to get from name (if userId stored as name)
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot extract user ID from authentication: " + principal.getClass());
        }
    }
    
    /**
     * Get current authenticated username/email from SecurityContext
     * 
     * @return username or email from JWT token
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        return authentication.getName();
    }
    
    /**
     * Check if current user has specific role
     * 
     * @param role role to check
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * UserPrincipal interface - implement this in your service
     */
    public interface UserPrincipal {
        Long getId();
        String getEmail();
    }
}

