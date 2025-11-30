package com.fooddelivery.delivery.config;

import com.fooddelivery.common.config.BaseAsyncConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * Configuration for async operations (location tracking)
 * Location updates come very frequently (every 5-10 seconds from each courier):
 * - Core: 3 threads (medium baseline)
 * - Max: 10 threads (handle multiple couriers)
 * - Queue: 1000 (large buffer - GPS updates are frequent and can queue)
 */
@Configuration
public class AsyncConfig extends BaseAsyncConfig {
    
    @Bean(name = "locationExecutor")
    @Override
    public Executor getAsyncExecutor() {
        return createAsyncExecutor("location-async-", 3, 10, 1000);
    }
}

