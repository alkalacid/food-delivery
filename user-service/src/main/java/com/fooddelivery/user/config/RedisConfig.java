package com.fooddelivery.user.config;

import com.fooddelivery.common.config.BaseRedisConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisConfig extends BaseRedisConfig {
    
    @Override
    protected void configureCaches(
            Map<String, RedisCacheConfiguration> cacheConfigs,
            RedisCacheConfiguration defaultConfig) {
        
        // User profiles: 2 hours (frequently accessed)
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Addresses: 1 hour
        cacheConfigs.put("addresses", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // JWT blacklist for logout (future): 24 hours
        cacheConfigs.put("jwtBlacklist", defaultConfig.entryTtl(Duration.ofHours(24)));
    }
}

