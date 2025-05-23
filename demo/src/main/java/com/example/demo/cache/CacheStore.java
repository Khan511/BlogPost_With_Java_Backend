package com.example.demo.cache;

import java.util.concurrent.TimeUnit;
import jakarta.validation.constraints.NotNull;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheStore<K, V> {
    private final Cache<K, V> cache;

    public CacheStore(int expiryDuration, TimeUnit timeUnit) {

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public V get(@NotNull K key) {
        log.info("Retrieving from cache with key {}", key.toString());
        return cache.getIfPresent(key);
    }

    public void put(@NotNull K key, @NotNull V value) {
        log.info("Storing record in Cache for key {}", key.toString());
        cache.put(key, value);
    }

    public void evict(@NotNull K key) {
        log.info("Removing from Cache with key {}", key.toString());
        cache.invalidate(key);
    }

}
