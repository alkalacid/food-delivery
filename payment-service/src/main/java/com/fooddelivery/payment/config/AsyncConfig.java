package com.fooddelivery.payment.config;

import com.fooddelivery.common.config.BaseAsyncConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * Configuration for async operations (audit logging)
 * Audit logging has low volume, so we use smaller thread pool:
 * - Core: 2 threads (minimal resources)
 * - Max: 5 threads (low concurrency expected)
 * - Queue: 100 (small buffer for burst)
 */
@Configuration
public class AsyncConfig extends BaseAsyncConfig {
    
    @Bean(name = "auditExecutor")
    @Override
    public Executor getAsyncExecutor() {
        return createAsyncExecutor("audit-async-", 2, 5, 100);
    }
}

