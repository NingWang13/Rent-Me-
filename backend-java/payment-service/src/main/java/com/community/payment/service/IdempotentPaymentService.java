package com.community.payment.service;

import com.community.common.redis.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class IdempotentPaymentService {

    private static final Logger log = LoggerFactory.getLogger(IdempotentPaymentService.class);

    private final RedisLockService redisLockService;

    public IdempotentPaymentService(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    private static final long LOCK_EXPIRE_SECONDS = 30;

    public PaymentCallbackResult handleCallback(@NonNull String transactionId, @NonNull String orderId, @NonNull String status) {
        String idempotentKey = buildIdempotentKey(transactionId);

        if (redisLockService.tryLock(idempotentKey, "processing", 5)) {
            try {
                return executePaymentCallback(transactionId, orderId, status);
            } finally {
                redisLockService.unlock(idempotentKey, "processing");
            }
        } else {
            log.warn("Duplicate callback detected: transactionId={}, orderId={}", transactionId, orderId);
            return PaymentCallbackResult.duplicate("ALREADY_PROCESSED");
        }
    }

    private PaymentCallbackResult executePaymentCallback(@NonNull String transactionId, @NonNull String orderId, @NonNull String status) {
        log.info("Processing payment callback: transactionId={}, orderId={}, status={}", transactionId, orderId, status);

        String lockKey = "payment:callback:" + orderId;
        String lockValue = "lock:" + transactionId;
        try {
            boolean success = redisLockService.executeWithLock(lockKey, lockValue, LOCK_EXPIRE_SECONDS, () -> {
                doProcessCallback(transactionId, orderId, status);
            });
            if (success) {
                return PaymentCallbackResult.success("Payment processed successfully");
            } else {
                return PaymentCallbackResult.failure("Failed to acquire lock");
            }
        } catch (Exception e) {
            log.error("Failed to acquire lock for payment callback: orderId={}", orderId, e);
            return PaymentCallbackResult.failure("SYSTEM_BUSY");
        }
    }

    private PaymentCallbackResult doProcessCallback(String transactionId, String orderId, String status) {
        if ("SUCCESS".equals(status)) {
            return PaymentCallbackResult.success("Payment processed successfully");
        } else if ("FAILED".equals(status)) {
            return PaymentCallbackResult.failed("Payment failed");
        } else if ("PENDING".equals(status)) {
            return PaymentCallbackResult.pending("Payment pending");
        }
        return PaymentCallbackResult.failure("Unknown status: " + status);
    }

    private @NonNull String buildIdempotentKey(@NonNull String transactionId) {
        return "payment:callback:" + transactionId;
    }

    public static class PaymentCallbackResult {
        private final int code;
        private final String message;
        private final String data;
        private final boolean success;

        private PaymentCallbackResult(int code, String message, String data) {
            this.code = code;
            this.message = message;
            this.data = data;
            this.success = code == 200;
        }

        public static PaymentCallbackResult success(String message) {
            return new PaymentCallbackResult(200, message, null);
        }

        public static PaymentCallbackResult duplicate(String cachedResult) {
            return new PaymentCallbackResult(201, "Already processed", cachedResult);
        }

        public static PaymentCallbackResult failure(String message) {
            return new PaymentCallbackResult(500, message, null);
        }

        public static PaymentCallbackResult failed(String message) {
            return new PaymentCallbackResult(400, message, null);
        }

        public static PaymentCallbackResult pending(String message) {
            return new PaymentCallbackResult(202, message, null);
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public String getData() { return data; }
        public boolean isSuccess() { return success; }
    }
}
