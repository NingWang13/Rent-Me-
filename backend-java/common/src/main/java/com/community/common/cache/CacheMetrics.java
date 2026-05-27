package com.community.common.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CacheMetrics {

    private final LocalCacheService localCache;
    private final MeterRegistry meterRegistry;

    public CacheMetrics(LocalCacheService localCache, MeterRegistry meterRegistry) {
        this.localCache = localCache;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("cache.local.size", localCache, LocalCacheService::size)
                .description("Local cache size")
                .register(meterRegistry);

        Gauge.builder("cache.local.hitRate", localCache, cache -> {
            CacheStats stats = cache.getStats();
            return stats.hitRate();
        }).description("Local cache hit rate")
                .register(meterRegistry);

        Gauge.builder("cache.local.missRate", localCache, cache -> {
            CacheStats stats = cache.getStats();
            return stats.missRate();
        }).description("Local cache miss rate")
                .register(meterRegistry);

        Gauge.builder("cache.local.evictionCount", localCache, cache -> {
            CacheStats stats = cache.getStats();
            return stats.evictionCount();
        }).description("Local cache eviction count")
                .register(meterRegistry);
    }
}
