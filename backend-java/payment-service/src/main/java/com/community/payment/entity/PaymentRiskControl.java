package com.community.payment.entity;

import com.community.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class PaymentRiskControl extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Integer paymentPasswordFailCount;

    private LocalDateTime paymentPasswordLockTime;

    private Integer dailyPaymentAmount;

    private LocalDateTime lastPaymentTime;

    private Boolean faceVerified;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPaymentPasswordFailCount() {
        return paymentPasswordFailCount;
    }

    public void setPaymentPasswordFailCount(Integer paymentPasswordFailCount) {
        this.paymentPasswordFailCount = paymentPasswordFailCount;
    }

    public LocalDateTime getPaymentPasswordLockTime() {
        return paymentPasswordLockTime;
    }

    public void setPaymentPasswordLockTime(LocalDateTime paymentPasswordLockTime) {
        this.paymentPasswordLockTime = paymentPasswordLockTime;
    }

    public Integer getDailyPaymentAmount() {
        return dailyPaymentAmount;
    }

    public void setDailyPaymentAmount(Integer dailyPaymentAmount) {
        this.dailyPaymentAmount = dailyPaymentAmount;
    }

    public LocalDateTime getLastPaymentTime() {
        return lastPaymentTime;
    }

    public void setLastPaymentTime(LocalDateTime lastPaymentTime) {
        this.lastPaymentTime = lastPaymentTime;
    }

    public Boolean getFaceVerified() {
        return faceVerified;
    }

    public void setFaceVerified(Boolean faceVerified) {
        this.faceVerified = faceVerified;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Integer paymentPasswordFailCount;
        private LocalDateTime paymentPasswordLockTime;
        private Integer dailyPaymentAmount;
        private LocalDateTime lastPaymentTime;
        private Boolean faceVerified;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder paymentPasswordFailCount(Integer paymentPasswordFailCount) { this.paymentPasswordFailCount = paymentPasswordFailCount; return this; }
        public Builder paymentPasswordLockTime(LocalDateTime paymentPasswordLockTime) { this.paymentPasswordLockTime = paymentPasswordLockTime; return this; }
        public Builder dailyPaymentAmount(Integer dailyPaymentAmount) { this.dailyPaymentAmount = dailyPaymentAmount; return this; }
        public Builder lastPaymentTime(LocalDateTime lastPaymentTime) { this.lastPaymentTime = lastPaymentTime; return this; }
        public Builder faceVerified(Boolean faceVerified) { this.faceVerified = faceVerified; return this; }

        public PaymentRiskControl build() {
            PaymentRiskControl control = new PaymentRiskControl();
            control.userId = this.userId;
            control.paymentPasswordFailCount = this.paymentPasswordFailCount;
            control.paymentPasswordLockTime = this.paymentPasswordLockTime;
            control.dailyPaymentAmount = this.dailyPaymentAmount;
            control.lastPaymentTime = this.lastPaymentTime;
            control.faceVerified = this.faceVerified;
            return control;
        }
    }
}
