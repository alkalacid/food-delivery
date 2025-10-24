package com.fooddelivery.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Base security configuration providing common beans for all microservices.
 * Each service should extend this and add service-specific security configuration.
 */
public abstract class BaseSecurityConfig {
    
    /**
     * Password encoder bean - BCrypt with default strength (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Override this method in subclasses to define service-specific public endpoints
     * 
     * @return Array of public endpoint patterns (e.g., "/api/auth/**", "/actuator/**")
     */
    protected abstract String[] getPublicEndpoints();
    
    /**
     * Common public endpoints across all services
     */
    protected String[] getCommonPublicEndpoints() {
        return new String[]{
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**"
        };
    }
}

