package com.community.common.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class MultiLevelCacheService {

    private static final Logger log = LoggerFactory.getLogger(MultiLevelCacheService.class);

    private static final String NULL_PLACEHOLDER = "__NULL__";

    private final LocalCacheService localCache;
    private final RedisCacheService redisCache;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public MultiLevelCacheService(LocalCacheService localCache, RedisCacheService redisCache) {
        this.localCache = localCache;
        this.redisCache = redisCache;
    }

    public Object get(@NonNull String key) {
        Object value = localCache.get(key);
        if (value != null) {
            if (NULL_PLACEHOLDER.equals(value)) {
                return null;
            }
            return value;
        }

        value = redisCache.get(key);
        if (value != null) {
            if (NULL_PLACEHOLDER.equals(value)) {
                return null;
            }
            localCache.put(key, value, 60);
            return value;
        }

        return null;
    }

    public <T> T get(@NonNull String key, @NonNull Class<T> clazz) {
        Object value = localCache.get(key);
        if (value != null) {
            if (NULL_PLACEHOLDER.equals(value)) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            return JSON.parseObject(value.toString(), clazz);
        }

        T redisValue = redisCache.get(key, clazz);
        if (redisValue != null) {
            localCache.put(key, redisValue, 60);
            return redisValue;
        }

        return null;
    }

    public <T> T get(@NonNull String key, @NonNull TypeReference<T> typeReference) {
        Object value = localCache.get(key);
        if (value != null) {
            if (NULL_PLACEHOLDER.equals(value)) {
                return null;
            }
            return JSON.parseObject(value.toString(), typeReference);
        }

        T redisValue = redisCache.get(key, typeReference);
        if (redisValue != null) {
            localCache.put(key, redisValue, 60);
            return redisValue;
        }

        return null;
    }

    public <T> T getOrLoad(@NonNull String key, @NonNull Supplier<T> loader, long ttlSeconds) {
        T value = get(key, new TypeReference<T>() {});
        if (value != null) {
            return value;
        }

        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                value = get(key, new TypeReference<T>() {});
                if (value != null) {
                    return value;
                }

                value = loader.get();
                if (value != null) {
                    put(key, value, ttlSeconds);
                } else {
                    putNull(key, 300);
                }
                return value;
            } finally {
                locks.remove(key);
            }
        }
    }

    public void put(@NonNull String key, @NonNull Object value, long ttlSeconds) {
        localCache.put(key, value, Math.min(ttlSeconds, 60));
        redisCache.putWithRandomTtl(key, value, ttlSeconds, 300);
    }

    public void evict(@NonNull String key) {
        localCache.evict(key);
        redisCache.evict(key);
    }

    public void evictPattern(@NonNull String pattern) {
        localCache.clear();
        log.info("Cache evicted for pattern: {}", pattern);
    }

    private void putNull(@NonNull String key, long ttlSeconds) {
        localCache.put(key, NULL_PLACEHOLDER, Math.min(ttlSeconds, 60));
        redisCache.putWithRandomTtl(key, NULL_PLACEHOLDER, ttlSeconds, 300);
    }

    public boolean hasKey(@NonNull String key) {
        return localCache.get(key) != null || redisCache.hasKey(key);
    }

    public LocalCacheService getLocalCache() {
        return localCache;
    }

    public RedisCacheService getRedisCache() {
        return redisCache;
    }
}
