package com.community.payment.entity;

import com.community.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long userId;

    private BigDecimal amount;

    private Integer paymentMethod;

    private Integer status;

    private String transactionNo;

    private String channelTransactionNo;

    private String errorMessage;

    private LocalDateTime paymentTime;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getChannelTransactionNo() {
        return channelTransactionNo;
    }

    public void setChannelTransactionNo(String channelTransactionNo) {
        this.channelTransactionNo = channelTransactionNo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
        private Integer paymentMethod;
        private Integer status;
        private String transactionNo;
        private String channelTransactionNo;
        private String errorMessage;

        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder paymentMethod(Integer paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder transactionNo(String transactionNo) { this.transactionNo = transactionNo; return this; }
        public Builder channelTransactionNo(String channelTransactionNo) { this.channelTransactionNo = channelTransactionNo; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public PaymentTransaction build() {
            PaymentTransaction t = new PaymentTransaction();
            t.orderId = this.orderId;
            t.userId = this.userId;
            t.amount = this.amount;
            t.paymentMethod = this.paymentMethod;
            t.status = this.status;
            t.transactionNo = this.transactionNo;
            t.channelTransactionNo = this.channelTransactionNo;
            t.errorMessage = this.errorMessage;
            return t;
        }
    }
}
