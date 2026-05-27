package com.community.common.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(@NonNull String key, @NonNull String value, long timeoutSeconds) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeoutSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(@NonNull String key, @NonNull String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        }
    }

    public boolean executeWithLock(@NonNull String key, @NonNull String value, long timeoutSeconds, Runnable action) {
        if (tryLock(key, value, timeoutSeconds)) {
            try {
                action.run();
                return true;
            } finally {
                unlock(key, value);
            }
        }
        return false;
    }
}
