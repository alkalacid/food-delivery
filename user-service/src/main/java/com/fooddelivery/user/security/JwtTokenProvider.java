package com.fooddelivery.user.security;

import com.fooddelivery.common.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Service-specific wrapper for JwtUtil to support Authentication-based token generation.
 * Delegates to common JwtUtil for actual JWT operations.
 */
@Component
public class JwtTokenProvider {
    
    final JwtUtil jwtUtil;
    
    public JwtTokenProvider(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return jwtUtil.generateToken(
            userPrincipal.getId(),
            userPrincipal.getEmail(),
            userPrincipal.getRole().name()
        );
    }
    
    public String generateToken(Long userId, String email, String role) {
        return jwtUtil.generateToken(userId, email, role);
    }
    
    public Long getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }
    
    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    public long getExpirationMs() {
        return jwtUtil.getExpirationMs();
    }
}

