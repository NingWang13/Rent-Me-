package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("friend_relation")
public class FriendRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long friendId;

    private String remark;

    private Integer status;

    private String applyMessage;

    private LocalDateTime applyTime;

    private LocalDateTime agreeTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getAgreeTime() {
        return agreeTime;
    }

    public void setAgreeTime(LocalDateTime agreeTime) {
        this.agreeTime = agreeTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Long friendId;
        private String remark;
        private Integer status;
        private String applyMessage;
        private LocalDateTime applyTime;
        private LocalDateTime agreeTime;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder friendId(Long friendId) { this.friendId = friendId; return this; }
        public Builder remark(String remark) { this.remark = remark; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder applyMessage(String applyMessage) { this.applyMessage = applyMessage; return this; }
        public Builder applyTime(LocalDateTime applyTime) { this.applyTime = applyTime; return this; }
        public Builder agreeTime(LocalDateTime agreeTime) { this.agreeTime = agreeTime; return this; }

        public FriendRelation build() {
            FriendRelation relation = new FriendRelation();
            relation.userId = this.userId;
            relation.friendId = this.friendId;
            relation.remark = this.remark;
            relation.status = this.status;
            relation.applyMessage = this.applyMessage;
            relation.applyTime = this.applyTime;
            relation.agreeTime = this.agreeTime;
            return relation;
        }
    }
}
