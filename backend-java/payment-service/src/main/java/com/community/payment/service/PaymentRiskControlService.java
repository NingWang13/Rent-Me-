package com.community.payment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.payment.entity.PaymentRiskControl;
import com.community.payment.mapper.PaymentRiskControlMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentRiskControlService extends ServiceImpl<PaymentRiskControlMapper, PaymentRiskControl> {

    private static final int MAX_PAYMENT_PASSWORD_FAILURES = 5;
    private static final int PASSWORD_LOCK_DURATION_MINUTES = 30;
    private static final int SINGLE_PAYMENT_LIMIT = 5000;
    private static final int DAILY_PAYMENT_LIMIT = 20000;

    public void verifyPayment(Long userId, String paymentPassword, boolean requireFaceVerify) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);

        if (riskControl.getPaymentPasswordFailCount() >= MAX_PAYMENT_PASSWORD_FAILURES) {
            LocalDateTime lockTime = riskControl.getPaymentPasswordLockTime();
            if (lockTime == null) {
                resetPaymentPasswordFailCount(userId);
                return;
            }
            LocalDateTime lockEndTime = lockTime.plusMinutes(PASSWORD_LOCK_DURATION_MINUTES);
            if (LocalDateTime.now().isBefore(lockEndTime)) {
                throw new BusinessException(400, "支付密码已锁定，请30分钟后重试");
            } else {
                resetPaymentPasswordFailCount(userId);
            }
        }

        if (requireFaceVerify) {
            if (!Boolean.TRUE.equals(riskControl.getFaceVerified())) {
                throw new BusinessException(400, "需要完成人脸验证");
            }
        }
    }

    public void recordPaymentPasswordFailure(Long userId) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        riskControl.setPaymentPasswordFailCount(riskControl.getPaymentPasswordFailCount() + 1);
        if (riskControl.getPaymentPasswordFailCount() >= MAX_PAYMENT_PASSWORD_FAILURES) {
            riskControl.setPaymentPasswordLockTime(LocalDateTime.now());
        }
        updateById(riskControl);
    }

    public void resetPaymentPasswordFailCount(Long userId) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        riskControl.setPaymentPasswordFailCount(0);
        riskControl.setPaymentPasswordLockTime(null);
        updateById(riskControl);
    }

    public void recordPaymentSuccess(Long userId, String orderNo) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        riskControl.setPaymentPasswordFailCount(0);
        riskControl.setPaymentPasswordLockTime(null);
        riskControl.setLastPaymentTime(LocalDateTime.now());
        updateById(riskControl);
    }

    public void verifyPaymentAmount(Long userId, String orderNo, int amount) {
        if (amount > SINGLE_PAYMENT_LIMIT) {
            throw new BusinessException(400, "单笔支付金额不能超过" + SINGLE_PAYMENT_LIMIT + "元");
        }

        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        if (riskControl.getDailyPaymentAmount() + amount > DAILY_PAYMENT_LIMIT) {
            throw new BusinessException(400, "当日累计支付金额已达上限" + DAILY_PAYMENT_LIMIT + "元");
        }
    }

    public void updateDailyPaymentAmount(Long userId, int amount) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        riskControl.setDailyPaymentAmount(riskControl.getDailyPaymentAmount() + amount);
        updateById(riskControl);
    }

    public void resetDailyPaymentAmount(Long userId) {
        PaymentRiskControl riskControl = getOrCreateRiskControl(userId);
        riskControl.setDailyPaymentAmount(0);
        updateById(riskControl);
    }

    public boolean isNewUser(Long userId) {
        PaymentRiskControl riskControl = getById(userId);
        return riskControl == null;
    }

    private PaymentRiskControl getOrCreateRiskControl(Long userId) {
        PaymentRiskControl riskControl = getById(userId);
        if (riskControl == null) {
            riskControl = PaymentRiskControl.builder()
                    .userId(userId)
                    .paymentPasswordFailCount(0)
                    .dailyPaymentAmount(0)
                    .faceVerified(false)
                    .build();
            save(riskControl);
        }
        return riskControl;
    }
}
