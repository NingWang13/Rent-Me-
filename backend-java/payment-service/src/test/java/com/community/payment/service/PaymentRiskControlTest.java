package com.community.payment.service;

import com.community.common.exception.BusinessException;
import com.community.payment.entity.PaymentRiskControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRiskControlTest {

    private TestPaymentRiskControlService riskControlService;
    private PaymentRiskControl testRiskControl;

    @BeforeEach
    void setUp() {
        testRiskControl = new PaymentRiskControl();
        testRiskControl.setId(1L);
        testRiskControl.setUserId(100L);
        testRiskControl.setPaymentPasswordFailCount(0);
        testRiskControl.setDailyPaymentAmount(0);
        testRiskControl.setFaceVerified(false);

        riskControlService = new TestPaymentRiskControlService();
    }

    @Test
    void isNewUser_True() {
        riskControlService.setGetByIdResult(null);

        boolean result = riskControlService.isNewUser(999L);

        assertTrue(result);
    }

    @Test
    void isNewUser_False() {
        riskControlService.setGetByIdResult(testRiskControl);

        boolean result = riskControlService.isNewUser(100L);

        assertFalse(result);
    }

    @Test
    void verifyPaymentPassword_FailuresExceeded() {
        testRiskControl.setPaymentPasswordFailCount(5);
        testRiskControl.setPaymentPasswordLockTime(LocalDateTime.now());
        riskControlService.setGetByIdResult(testRiskControl);

        assertThrows(BusinessException.class, () ->
                riskControlService.verifyPayment(100L, "123456", false));
    }

    @Test
    void verifyPaymentPassword_FaceVerifyRequired() {
        riskControlService.setGetByIdResult(testRiskControl);

        assertThrows(BusinessException.class, () ->
                riskControlService.verifyPayment(100L, "123456", true));
    }

    @Test
    void recordPaymentPasswordFailure_LocksAfterMaxFailures() {
        riskControlService.setGetByIdResult(testRiskControl);

        for (int i = 0; i < 5; i++) {
            riskControlService.recordPaymentPasswordFailure(100L);
        }

        assertEquals(5, riskControlService.getUpdateByIdCallCount());
    }

    @Test
    void verifyPaymentAmount_ExceedsSingleLimit() {
        riskControlService.setGetByIdResult(testRiskControl);

        assertThrows(BusinessException.class, () ->
                riskControlService.verifyPaymentAmount(100L, "order1", 6000));
    }

    @Test
    void verifyPaymentAmount_ExceedsDailyLimit() {
        testRiskControl.setDailyPaymentAmount(18000);
        riskControlService.setGetByIdResult(testRiskControl);

        assertThrows(BusinessException.class, () ->
                riskControlService.verifyPaymentAmount(100L, "order1", 3000));
    }

    @Test
    void verifyPaymentAmount_WithinLimits() {
        riskControlService.setGetByIdResult(testRiskControl);

        assertDoesNotThrow(() ->
                riskControlService.verifyPaymentAmount(100L, "order1", 1000));
    }

    @Test
    void resetPaymentPasswordFailCount_Success() {
        testRiskControl.setPaymentPasswordFailCount(3);
        testRiskControl.setPaymentPasswordLockTime(LocalDateTime.now());
        riskControlService.setGetByIdResult(testRiskControl);

        riskControlService.resetPaymentPasswordFailCount(100L);

        assertEquals(1, riskControlService.getUpdateByIdCallCount());
    }

    static class TestPaymentRiskControlService extends PaymentRiskControlService {
        private PaymentRiskControl getByIdResult;
        private int updateByIdCallCount = 0;

        public TestPaymentRiskControlService() {
            super();
        }

        @Override
        public PaymentRiskControl getById(java.io.Serializable id) {
            return getByIdResult;
        }

        @Override
        public boolean updateById(PaymentRiskControl entity) {
            updateByIdCallCount++;
            return true;
        }

        void setGetByIdResult(PaymentRiskControl result) {
            this.getByIdResult = result;
        }

        int getUpdateByIdCallCount() {
            return updateByIdCallCount;
        }
    }
}
