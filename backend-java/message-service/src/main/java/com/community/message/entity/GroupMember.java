package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("group_member")
public class GroupMember extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long groupId;

    private Long userId;

    private Integer role;

    private String nickname;

    private Integer joinType;

    private Long inviterId;

    private Integer status;

    private LocalDateTime joinTime;

    private LocalDateTime quitTime;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getQuitTime() {
        return quitTime;
    }

    public void setQuitTime(LocalDateTime quitTime) {
        this.quitTime = quitTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long groupId;
        private Long userId;
        private Integer role;
        private String nickname;
        private Integer joinType;
        private Long inviterId;
        private Integer status;
        private LocalDateTime joinTime;
        private LocalDateTime quitTime;

        public Builder groupId(Long groupId) { this.groupId = groupId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder role(Integer role) { this.role = role; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder joinType(Integer joinType) { this.joinType = joinType; return this; }
        public Builder inviterId(Long inviterId) { this.inviterId = inviterId; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder joinTime(LocalDateTime joinTime) { this.joinTime = joinTime; return this; }
        public Builder quitTime(LocalDateTime quitTime) { this.quitTime = quitTime; return this; }

        public GroupMember build() {
            GroupMember member = new GroupMember();
            member.groupId = this.groupId;
            member.userId = this.userId;
            member.role = this.role;
            member.nickname = this.nickname;
            member.joinType = this.joinType;
            member.inviterId = this.inviterId;
            member.status = this.status;
            member.joinTime = this.joinTime;
            member.quitTime = this.quitTime;
            return member;
        }
    }
}
