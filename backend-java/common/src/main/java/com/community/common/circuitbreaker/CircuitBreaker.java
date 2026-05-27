package com.community.common.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class CircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private final String name;
    private final int failureThreshold;
    private final int successThreshold;
    private final long timeoutMs;

    private volatile CircuitState state;
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private final AtomicLong lastFailureTime;
    private final AtomicLong lastStateChangeTime;

    private static final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    public enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }

    public CircuitBreaker(String name, int failureThreshold, int successThreshold, long timeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeoutMs = timeoutMs;
        this.state = CircuitState.CLOSED;
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.lastFailureTime = new AtomicLong(0);
        this.lastStateChangeTime = new AtomicLong(System.currentTimeMillis());
    }

    public static CircuitBreaker getOrCreate(String name) {
        return breakers.computeIfAbsent(name, k -> new CircuitBreaker(name, 5, 3, 30000));
    }

    public static CircuitBreaker getOrCreate(String name, int failureThreshold, int successThreshold, long timeoutMs) {
        return breakers.computeIfAbsent(name, k -> new CircuitBreaker(name, failureThreshold, successThreshold, timeoutMs));
    }

    public <T> T execute(Supplier<T> supplier, Supplier<T> fallback) {
        if (state == CircuitState.OPEN) {
            long now = System.currentTimeMillis();
            if (now - lastStateChangeTime.get() > timeoutMs) {
                transitionTo(CircuitState.HALF_OPEN);
            } else {
                logger.warn("Circuit breaker '{}' is OPEN, executing fallback", name);
                return fallback.get();
            }
        }

        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            if (state == CircuitState.HALF_OPEN) {
                transitionTo(CircuitState.OPEN);
            }
            logger.warn("Circuit breaker '{}' triggered, executing fallback. Error: {}", name, e.getMessage());
            return fallback.get();
        }
    }

    public void execute(Runnable runnable, Runnable fallback) {
        execute(() -> {
            runnable.run();
            return null;
        }, () -> {
            fallback.run();
            return null;
        });
    }

    private synchronized void onSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            successCount.incrementAndGet();
            if (successCount.get() >= successThreshold) {
                transitionTo(CircuitState.CLOSED);
            }
        } else if (state == CircuitState.CLOSED) {
            failureCount.set(0);
        }
    }

    private synchronized void onFailure() {
        failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());

        if (state == CircuitState.CLOSED && failureCount.get() >= failureThreshold) {
            transitionTo(CircuitState.OPEN);
        } else if (state == CircuitState.HALF_OPEN) {
            transitionTo(CircuitState.OPEN);
        }
    }

    private void transitionTo(CircuitState newState) {
        logger.info("Circuit breaker '{}' transitioning from {} to {}", name, state, newState);
        this.state = newState;
        this.lastStateChangeTime.set(System.currentTimeMillis());

        if (newState == CircuitState.CLOSED) {
            failureCount.set(0);
            successCount.set(0);
        } else if (newState == CircuitState.HALF_OPEN) {
            successCount.set(0);
        }
    }

    public CircuitState getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("name", name);
        metrics.put("state", state.name());
        metrics.put("failureCount", failureCount.get());
        metrics.put("successCount", successCount.get());
        metrics.put("lastFailureTime", lastFailureTime.get());
        metrics.put("lastStateChangeTime", lastStateChangeTime.get());
        return metrics;
    }
}
