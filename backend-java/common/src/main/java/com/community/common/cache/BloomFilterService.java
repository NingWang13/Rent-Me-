package com.community.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.time.Duration;

@Component
public class BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterService.class);

    private static final long EXPECTED_INSERTIONS = 1_000_000;
    private static final double FALSE_POSITIVE_RATE = 0.01;

    private final StringRedisTemplate redisTemplate;
    private final BitSet bloomFilter;
    private final int numBits;
    private final int numHashFunctions;

    public BloomFilterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.numBits = optimalNumOfBits(EXPECTED_INSERTIONS, FALSE_POSITIVE_RATE);
        this.numHashFunctions = optimalNumOfHashFunctions(EXPECTED_INSERTIONS, numBits);
        this.bloomFilter = new BitSet(numBits);
    }

    public void add(@NonNull String key) {
        for (int i = 0; i < numHashFunctions; i++) {
            long hash = hash(key, i);
            bloomFilter.set((int) (hash % numBits), true);
        }
    }

    public boolean mightContain(@NonNull String key) {
        for (int i = 0; i < numHashFunctions; i++) {
            long hash = hash(key, i);
            if (!bloomFilter.get((int) (hash % numBits))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("null")
    public void addWithRedis(@NonNull String key, @NonNull String filterKey) {
        try {
            for (int i = 0; i < numHashFunctions; i++) {
                long hash = hash(key, i);
                int bitIndex = (int) (hash % numBits);
                redisTemplate.opsForValue().setBit(filterKey, bitIndex, true);
            }
            Duration duration = Duration.ofHours(24);
            redisTemplate.expire(filterKey, duration);
        } catch (Exception e) {
            log.error("Bloom filter add to Redis failed: key={}", key, e);
        }
    }

    public boolean mightContainWithRedis(@NonNull String key, @NonNull String filterKey) {
        try {
            for (int i = 0; i < numHashFunctions; i++) {
                long hash = hash(key, i);
                int bitIndex = (int) (hash % numBits);
                Boolean bitValue = redisTemplate.opsForValue().getBit(filterKey, bitIndex);
                if (Boolean.FALSE.equals(bitValue)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Bloom filter check Redis failed: key={}", key, e);
            return true;
        }
    }

    private long hash(String key, int seed) {
        long hash = seed;
        for (char c : key.toCharArray()) {
            hash = hash * 31 + c;
        }
        return Math.abs(hash);
    }

    private static int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    private static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }
}
