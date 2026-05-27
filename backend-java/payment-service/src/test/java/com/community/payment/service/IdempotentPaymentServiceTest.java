package com.community.payment.service;

import com.community.common.redis.RedisLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import static org.junit.jupiter.api.Assertions.*;

class IdempotentPaymentServiceTest {

    private TestRedisLockService testRedisLockService;
    private IdempotentPaymentService idempotentPaymentService;

    @BeforeEach
    void setUp() {
        testRedisLockService = new TestRedisLockService();
        idempotentPaymentService = new IdempotentPaymentService(testRedisLockService);
    }

    @Test
    void handleCallback_Success() {
        testRedisLockService.setTryLockResult(true);
        testRedisLockService.setExecuteWithLockResult(true);

        IdempotentPaymentService.PaymentCallbackResult result =
                idempotentPaymentService.handleCallback("TXN123", "ORDER001", "SUCCESS");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    void handleCallback_DuplicateRequest() {
        testRedisLockService.setTryLockResult(false);

        IdempotentPaymentService.PaymentCallbackResult result =
                idempotentPaymentService.handleCallback("TXN123", "ORDER001", "SUCCESS");

        assertNotNull(result);
        assertEquals(201, result.getCode());
    }

    @Test
    void handleCallback_Failed() {
        testRedisLockService.setTryLockResult(true);
        testRedisLockService.setExecuteWithLockResult(true);

        IdempotentPaymentService.PaymentCallbackResult result =
                idempotentPaymentService.handleCallback("TXN123", "ORDER001", "FAILED");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    void handleCallback_Pending() {
        testRedisLockService.setTryLockResult(true);
        testRedisLockService.setExecuteWithLockResult(true);

        IdempotentPaymentService.PaymentCallbackResult result =
                idempotentPaymentService.handleCallback("TXN123", "ORDER001", "PENDING");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    static class TestRedisLockService extends RedisLockService {
        private boolean tryLockResult = true;
        private boolean executeWithLockResult = true;

        public TestRedisLockService() {
            super(null);
        }

        @Override
        public boolean tryLock(@NonNull String key, @NonNull String value, long timeoutSeconds) {
            return tryLockResult;
        }

        @Override
        public void unlock(@NonNull String key, @NonNull String value) {
        }

        @Override
        public boolean executeWithLock(@NonNull String key, @NonNull String value, long timeoutSeconds, Runnable action) {
            if (executeWithLockResult) {
                action.run();
            }
            return executeWithLockResult;
        }

        void setTryLockResult(boolean result) {
            this.tryLockResult = result;
        }

        void setExecuteWithLockResult(boolean result) {
            this.executeWithLockResult = result;
        }
    }
}
