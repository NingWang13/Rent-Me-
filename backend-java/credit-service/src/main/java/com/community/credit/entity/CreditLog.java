package com.community.credit.entity;

import com.community.common.entity.BaseEntity;

public class CreditLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Integer changeAmount;

    private Integer balance;

    private Integer type;

    private String source;

    private String sourceId;

    private String description;

    private Integer balanceAfter;

    private Long relatedId;

    private String relatedType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Integer changeAmount;
        private Integer balance;
        private Integer type;
        private String source;
        private String sourceId;
        private String description;
        private Integer balanceAfter;
        private Long relatedId;
        private String relatedType;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder changeAmount(Integer changeAmount) { this.changeAmount = changeAmount; return this; }
        public Builder balance(Integer balance) { this.balance = balance; return this; }
        public Builder type(Integer type) { this.type = type; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder sourceId(String sourceId) { this.sourceId = sourceId; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder balanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; return this; }
        public Builder relatedId(Long relatedId) { this.relatedId = relatedId; return this; }
        public Builder relatedType(String relatedType) { this.relatedType = relatedType; return this; }

        public CreditLog build() {
            CreditLog log = new CreditLog();
            log.userId = this.userId;
            log.changeAmount = this.changeAmount;
            log.balance = this.balance;
            log.type = this.type;
            log.source = this.source;
            log.sourceId = this.sourceId;
            log.description = this.description;
            log.balanceAfter = this.balanceAfter;
            log.relatedId = this.relatedId;
            log.relatedType = this.relatedType;
            return log;
        }
    }
}
