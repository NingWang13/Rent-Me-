package com.community.common.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrderTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutJob.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public OrderTimeoutJob(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        log.info("Start checking timeout orders");

        Set<Object> timeoutOrders = redisTemplate.opsForZSet()
                .rangeByScore("order:timeout", 0, System.currentTimeMillis());

        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            log.info("No timeout orders found");
            return;
        }

        for (Object orderId : timeoutOrders) {
            try {
                redisTemplate.opsForZSet().remove("order:timeout", orderId);
                log.info("Order {} timeout cancelled", orderId);
            } catch (Exception e) {
                log.error("Failed to cancel timeout order: {}", orderId, e);
            }
        }

        log.info("Timeout order check completed");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        log.info("Start cleaning expired data");
        try {
            Set<String> keys = redisTemplate.keys("order:timeout:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("Expired data cleaned successfully");
        } catch (Exception e) {
            log.error("Failed to clean expired data", e);
        }
    }
}
