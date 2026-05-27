package com.community.common.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitService {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int permitsPerSecond) {
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> new RateLimiter(permitsPerSecond));
        return limiter.tryAcquire();
    }

    private static class RateLimiter {
        private final int permitsPerSecond;
        private final AtomicInteger tokens;
        private volatile long lastRefillTime;

        RateLimiter(int permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
            this.tokens = new AtomicInteger(permitsPerSecond);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            int newTokens = (int) (elapsed * permitsPerSecond / 1000);
            if (newTokens > 0) {
                tokens.set(Math.min(permitsPerSecond, tokens.get() + newTokens));
                lastRefillTime = now;
            }
        }
    }
}
