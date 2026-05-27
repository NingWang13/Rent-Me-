package com.community.common.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate redisTemplate;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("null")
    public void put(@NonNull String key, @NonNull Object value, long ttlSeconds) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");
        try {
            String jsonValue = JSON.toJSONString(value);
            Duration duration = Duration.ofSeconds(ttlSeconds);
            redisTemplate.opsForValue().set(key, jsonValue, duration);
        } catch (Exception e) {
            log.error("Redis cache put failed: key={}", key, e);
        }
    }

    @SuppressWarnings("null")
    public void putWithRandomTtl(@NonNull String key, @NonNull Object value, long baseTtlSeconds, long randomOffsetSeconds) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");
        try {
            long actualTtl = baseTtlSeconds + ThreadLocalRandom.current().nextLong(randomOffsetSeconds);
            String jsonValue = JSON.toJSONString(value);
            Duration duration = Duration.ofSeconds(actualTtl);
            redisTemplate.opsForValue().set(key, jsonValue, duration);
        } catch (Exception e) {
            log.error("Redis cache put with random TTL failed: key={}", key, e);
        }
    }

    public String get(@NonNull String key) {
        Assert.notNull(key, "Key must not be null");
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis cache get failed: key={}", key, e);
            return null;
        }
    }

    public <T> T get(@NonNull String key, @NonNull Class<T> clazz) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(clazz, "Class must not be null");
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return JSON.parseObject(jsonValue, clazz);
        } catch (Exception e) {
            log.error("Redis cache get with deserialization failed: key={}", key, e);
            return null;
        }
    }

    public <T> T get(@NonNull String key, @NonNull TypeReference<T> typeReference) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(typeReference, "TypeReference must not be null");
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return JSON.parseObject(jsonValue, typeReference);
        } catch (Exception e) {
            log.error("Redis cache get with TypeReference failed: key={}", key, e);
            return null;
        }
    }

    public void evict(@NonNull String key) {
        Assert.notNull(key, "Key must not be null");
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis cache evict failed: key={}", key, e);
        }
    }

    public boolean hasKey(@NonNull String key) {
        Assert.notNull(key, "Key must not be null");
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis cache hasKey failed: key={}", key, e);
            return false;
        }
    }

    @SuppressWarnings("null")
    public void putIfAbsent(@NonNull String key, @NonNull Object value, long ttlSeconds) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");
        try {
            String jsonValue = JSON.toJSONString(value);
            Duration duration = Duration.ofSeconds(ttlSeconds);
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, jsonValue, duration);
            if (success != null && !success) {
                log.debug("Cache key already exists: {}", key);
            }
        } catch (Exception e) {
            log.error("Redis cache putIfAbsent failed: key={}", key, e);
        }
    }

    @SuppressWarnings("null")
    public boolean tryLock(@NonNull String key, @NonNull String value, long ttlSeconds) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");
        try {
            Duration duration = Duration.ofSeconds(ttlSeconds);
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, duration);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Redis cache tryLock failed: key={}", key, e);
            return false;
        }
    }

    public boolean releaseLock(@NonNull String key, @NonNull String value) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (value.equals(currentValue)) {
                redisTemplate.delete(key);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Redis cache releaseLock failed: key={}", key, e);
            return false;
        }
    }
}
