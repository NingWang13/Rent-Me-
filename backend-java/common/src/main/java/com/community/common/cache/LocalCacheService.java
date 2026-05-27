package com.community.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class LocalCacheService {

    private final Cache<String, Object> cache;
    private final ConcurrentHashMap<String, Long> expireTimes = new ConcurrentHashMap<>();

    public LocalCacheService() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    if (key != null) {
                        expireTimes.remove(key);
                    }
                })
                .build();
    }

    public void put(@NonNull String key, @NonNull Object value, long ttlSeconds) {
        expireTimes.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
        cache.put(key, value);
    }

    public Object get(@NonNull String key) {
        Long expireTime = expireTimes.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            cache.invalidate(key);
            expireTimes.remove(key);
            return null;
        }
        return cache.getIfPresent(key);
    }

    public void evict(@NonNull String key) {
        expireTimes.remove(key);
        cache.invalidate(key);
    }

    public void clear() {
        expireTimes.clear();
        cache.invalidateAll();
    }

    public CacheStats getStats() {
        return cache.stats();
    }

    public long size() {
        return cache.estimatedSize();
    }
}
