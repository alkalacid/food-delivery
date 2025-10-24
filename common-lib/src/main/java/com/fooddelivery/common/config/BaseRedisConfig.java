package com.fooddelivery.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Redis configuration with caching support.
 * Services should extend this class and override configureCaches() to add service-specific caches.
 */
@EnableCaching
public abstract class BaseRedisConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = createDefaultCacheConfig();
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        configureCaches(cacheConfigurations, defaultConfig);
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
    
    /**
     * Creates default cache configuration.
     * Can be overridden by services to customize default TTL and serialization.
     */
    protected RedisCacheConfiguration createDefaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues();
    }
    
    /**
     * Override this method to configure service-specific caches with custom TTLs.
     * 
     * Example:
     * <pre>
     * cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofHours(2)));
     * cacheConfigs.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
     * </pre>
     */
    protected abstract void configureCaches(
        Map<String, RedisCacheConfiguration> cacheConfigs,
        RedisCacheConfiguration defaultConfig
    );
}

