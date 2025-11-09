package com.fooddelivery.order.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .setReadTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
    }
}

