package com.fooddelivery.restaurant.config;

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
        
        cacheConfigs.put("restaurants", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("menuItems", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("restaurantSearch", defaultConfig.entryTtl(Duration.ofMinutes(5)));
    }
}

