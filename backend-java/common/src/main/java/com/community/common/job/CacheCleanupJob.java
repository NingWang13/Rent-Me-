package com.community.common.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CacheCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(CacheCleanupJob.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheCleanupJob(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredCache() {
        log.info("Start cleaning expired cache");
        try {
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            log.info("Cache cleanup completed at {}", dateStr);
        } catch (Exception e) {
            log.error("Failed to clean expired cache", e);
        }
    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void checkCacheHealth() {
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                connectionFactory.getConnection().ping();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
        }
    }
}
