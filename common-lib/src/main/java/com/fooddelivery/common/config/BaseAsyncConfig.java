package com.fooddelivery.common.config;

import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Base configuration for async operations
 * 
 * Provides common async executor configuration that can be reused across services.
 * Each service can extend this class and customize thread pool parameters for their specific needs.
 * 
 * IMPORTANT: @EnableAsync must be added to the main Application class, NOT here.
 * 
 * Usage:
 * <pre>
 * {@code
 * // In Application class:
 * @SpringBootApplication
 * @EnableAsync  // <- Add here
 * public class MyApplication { ... }
 * 
 * // In config:
 * @Configuration
 * public class MyAsyncConfig extends BaseAsyncConfig {
 *     @Bean(name = "myExecutor")
 *     @Override
 *     public Executor getAsyncExecutor() {
 *         return createAsyncExecutor("my-async-", 5, 10, 200);
 *     }
 * }
 * }
 * </pre>
 */
public abstract class BaseAsyncConfig implements AsyncConfigurer {
    
    /**
     * Default values for async executor
     */
    protected static final int DEFAULT_CORE_POOL_SIZE = 5;
    protected static final int DEFAULT_MAX_POOL_SIZE = 10;
    protected static final int DEFAULT_QUEUE_CAPACITY = 100;
    protected static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 60;
    
    /**
     * Create configured ThreadPoolTaskExecutor with custom parameters
     * 
     * @param threadNamePrefix prefix for thread names (e.g., "audit-async-")
     * @param corePoolSize minimum number of threads
     * @param maxPoolSize maximum number of threads
     * @param queueCapacity queue size for pending tasks
     * @return configured executor
     */
    protected Executor createAsyncExecutor(
            String threadNamePrefix, 
            int corePoolSize, 
            int maxPoolSize, 
            int queueCapacity) {
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(DEFAULT_AWAIT_TERMINATION_SECONDS);
        executor.initialize();
        
        return executor;
    }
    
    /**
     * Create configured ThreadPoolTaskExecutor with default parameters
     * 
     * @param threadNamePrefix prefix for thread names
     * @return configured executor with default settings
     */
    protected Executor createAsyncExecutor(String threadNamePrefix) {
        return createAsyncExecutor(
            threadNamePrefix, 
            DEFAULT_CORE_POOL_SIZE, 
            DEFAULT_MAX_POOL_SIZE, 
            DEFAULT_QUEUE_CAPACITY
        );
    }
}

