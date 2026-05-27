package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

@TableName("group_chat")
public class GroupChat extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String groupNo;

    private String name;

    private String avatarUrl;

    private String description;

    private Long ownerId;

    private Integer memberCount;

    private Integer maxMembers;

    private Integer joinType;

    private Integer chatType;

    private Integer status;

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Integer getChatType() {
        return chatType;
    }

    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupNo;
        private String name;
        private String avatarUrl;
        private String description;
        private Long ownerId;
        private Integer memberCount;
        private Integer maxMembers;
        private Integer joinType;
        private Integer chatType;
        private Integer status;

        public Builder groupNo(String groupNo) { this.groupNo = groupNo; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public Builder memberCount(Integer memberCount) { this.memberCount = memberCount; return this; }
        public Builder maxMembers(Integer maxMembers) { this.maxMembers = maxMembers; return this; }
        public Builder joinType(Integer joinType) { this.joinType = joinType; return this; }
        public Builder chatType(Integer chatType) { this.chatType = chatType; return this; }
        public Builder status(Integer status) { this.status = status; return this; }

        public GroupChat build() {
            GroupChat group = new GroupChat();
            group.groupNo = this.groupNo;
            group.name = this.name;
            group.avatarUrl = this.avatarUrl;
            group.description = this.description;
            group.ownerId = this.ownerId;
            group.memberCount = this.memberCount;
            group.maxMembers = this.maxMembers;
            group.joinType = this.joinType;
            group.chatType = this.chatType;
            group.status = this.status;
            return group;
        }
    }
}
