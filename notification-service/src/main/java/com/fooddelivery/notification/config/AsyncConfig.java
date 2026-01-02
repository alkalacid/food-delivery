package com.fooddelivery.notification.config;

import com.fooddelivery.common.config.BaseAsyncConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * Configuration for async notification processing
 * Notifications are sent asynchronously to avoid blocking main business logic.
 * This improves response times and user experience.
 * Notifications have high volume (every order, payment, delivery triggers notifications):
 * - Core: 5 threads (higher baseline for notifications)
 * - Max: 20 threads (handle many concurrent notifications)
 * - Queue: 500 (large buffer for peak times)
 */
@Configuration
public class AsyncConfig extends BaseAsyncConfig {
    
    @Bean(name = "notificationExecutor")
    @Override
    public Executor getAsyncExecutor() {
        return createAsyncExecutor("notification-async-", 5, 20, 500);
    }
}

