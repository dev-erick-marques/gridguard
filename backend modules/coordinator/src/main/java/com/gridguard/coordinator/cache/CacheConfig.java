package com.gridguard.coordinator.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Deque;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Deque<DeviceStatusPayloadDTO>> deviceStatusCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .build();
    }
}
