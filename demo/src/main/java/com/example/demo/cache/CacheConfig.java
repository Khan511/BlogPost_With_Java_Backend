package com.example.demo.cache;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheStore<String, Integer> userCache() {
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }
}
