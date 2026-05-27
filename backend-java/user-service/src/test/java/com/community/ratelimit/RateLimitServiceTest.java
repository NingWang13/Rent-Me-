package com.community.ratelimit;

import com.community.common.ratelimit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void testTryAcquire_WithinLimit() {
        boolean result = rateLimitService.tryAcquire("test:key", 10);
        assertTrue(result);
    }

    @Test
    void testTryAcquire_ExceedsLimit() {
        String key = "test:limit:key";
        int limit = 5;

        for (int i = 0; i < limit; i++) {
            assertTrue(rateLimitService.tryAcquire(key, limit));
        }

        boolean result = rateLimitService.tryAcquire(key, limit);
        assertFalse(result);
    }

    @Test
    void testTryAcquire_DifferentKeysIndependent() {
        assertTrue(rateLimitService.tryAcquire("key1", 1));
        assertFalse(rateLimitService.tryAcquire("key1", 1));

        assertTrue(rateLimitService.tryAcquire("key2", 1));
    }

    @Test
    void testTryAcquire_ZeroPermits() {
        boolean result = rateLimitService.tryAcquire("test:zero", 0);
        assertFalse(result);
    }
}
