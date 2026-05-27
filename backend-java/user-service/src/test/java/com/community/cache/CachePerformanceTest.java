package com.community.cache;

import com.community.common.cache.LocalCacheService;
import com.community.common.cache.MultiLevelCacheService;
import com.community.common.cache.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CachePerformanceTest {

    private LocalCacheService localCacheService;
    private MultiLevelCacheService multiLevelCacheService;

    @BeforeEach
    void setUp() {
        localCacheService = new LocalCacheService();
        multiLevelCacheService = new MultiLevelCacheService(localCacheService, new RedisCacheService(null) {
            @Override
            public void put(@NonNull String key, @NonNull Object value, long ttlSeconds) {
            }

            @Override
            public void putWithRandomTtl(@NonNull String key, @NonNull Object value, long baseTtlSeconds, long randomOffsetSeconds) {
            }

            @Override
            public String get(@NonNull String key) {
                return null;
            }

            @Override
            public <T> T get(@NonNull String key, @NonNull Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T get(@NonNull String key, @NonNull com.alibaba.fastjson2.TypeReference<T> typeReference) {
                return null;
            }

            @Override
            public void evict(@NonNull String key) {
            }

            @Override
            public boolean hasKey(@NonNull String key) {
                return false;
            }
        });
    }

    @Test
    void testLocalCachePerformance() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            localCacheService.put("key:" + i, "value:" + i, 60);
        }

        for (int i = 0; i < 10000; i++) {
            Object value = localCacheService.get("key:" + i);
            assertNotNull(value);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Local cache 10000 operations: " + duration + "ms");
        assertTrue(duration < 1000, "Local cache should be fast");
    }

    @Test
    void testLocalCachePutAndGet() {
        String key = "test:putget:" + System.currentTimeMillis();
        String value = "test value";

        localCacheService.put(key, value, 60);
        Object result = localCacheService.get(key);

        assertEquals(value, result);
    }

    @Test
    void testLocalCacheExpiration() throws InterruptedException {
        String key = "test:expire:" + System.currentTimeMillis();

        localCacheService.put(key, "will expire", 1);

        Object immediate = localCacheService.get(key);
        assertEquals("will expire", immediate);

        Thread.sleep(1100);

        Object afterExpire = localCacheService.get(key);
        assertNull(afterExpire, "Cache entry should be expired and return null");
    }

    @Test
    void testLocalCacheEviction() {
        String key = "test:evict:" + System.currentTimeMillis();

        localCacheService.put(key, "test data", 60);
        assertNotNull(localCacheService.get(key));

        localCacheService.evict(key);
        assertNull(localCacheService.get(key));
    }

    @Test
    void testLocalCacheClear() {
        localCacheService.put("key1", "value1", 60);
        localCacheService.put("key2", "value2", 60);
        localCacheService.put("key3", "value3", 60);

        localCacheService.clear();

        assertNull(localCacheService.get("key1"));
        assertNull(localCacheService.get("key2"));
        assertNull(localCacheService.get("key3"));
    }

    @Test
    void testMultiLevelCachePutAndGet() {
        String key = "test:multi:" + System.currentTimeMillis();

        multiLevelCacheService.put(key, "multi data", 60);
        Object result = multiLevelCacheService.get(key);

        assertEquals("multi data", result);
    }

    @Test
    void testMultiLevelCacheEviction() {
        String key = "test:multi:evict:" + System.currentTimeMillis();

        multiLevelCacheService.put(key, "test data", 60);
        assertNotNull(multiLevelCacheService.get(key));

        multiLevelCacheService.evict(key);
        assertNull(multiLevelCacheService.get(key));
    }

    @Test
    void testConcurrentCacheAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "concurrent:" + threadId + ":" + i;
                        localCacheService.put(key, "value:" + i, 60);
                        Object value = localCacheService.get(key);
                        if (value != null) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent cache operations success: " + successCount.get() + "/" + (threadCount * operationsPerThread));
        assertEquals(threadCount * operationsPerThread, successCount.get(), "All concurrent operations should succeed");
    }

    @Test
    void testCacheWithComplexObject() {
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("userId", 12345L);
        complexData.put("userName", "testUser");
        complexData.put("roles", new String[]{"admin", "user"});
        complexData.put("metadata", Map.of("key1", "value1", "key2", "value2"));

        String key = "complex:" + System.currentTimeMillis();
        multiLevelCacheService.put(key, complexData, 60);

        Object result = multiLevelCacheService.get(key);
        assertNotNull(result);
        assertEquals(complexData.toString(), result.toString());
    }

    @Test
    void testCachePenetrationProtection() {
        String nonExistentKey = "nonexistent:" + System.currentTimeMillis();
        Object result = multiLevelCacheService.get(nonExistentKey);
        assertNull(result, "Non-existent key should return null");

        multiLevelCacheService.put(nonExistentKey, new Object(), 60);
        Object cachedResult = multiLevelCacheService.get(nonExistentKey);
        assertNotNull(cachedResult, "Should be cached after put");
    }

    @Test
    void testCacheKeyPattern() {
        String[] validPatterns = {
            "order:detail:123",
            "order:no:ORD20250101120000000",
            "order:counts:456",
            "user:profile:789",
            "activity:list:page1"
        };

        for (String pattern : validPatterns) {
            assertNotNull(pattern, "Cache key pattern should not be null");
            assertTrue(pattern.contains(":"), "Cache key should contain colon separator");
        }
    }
}
