package com.community.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("feedback")
public class Feedback extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Integer type;

    private String content;

    private String images;

    private Integer status;

    private String replyContent;

    private LocalDateTime replyTime;

    private Integer rating;

    private String contact;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Integer type;
        private String content;
        private String images;
        private Integer status;
        private String replyContent;
        private LocalDateTime replyTime;
        private Integer rating;
        private String contact;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder type(Integer type) {
            this.type = type;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder images(String images) {
            this.images = images;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder replyContent(String replyContent) {
            this.replyContent = replyContent;
            return this;
        }

        public Builder replyTime(LocalDateTime replyTime) {
            this.replyTime = replyTime;
            return this;
        }

        public Builder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Builder contact(String contact) {
            this.contact = contact;
            return this;
        }

        public Feedback build() {
            Feedback feedback = new Feedback();
            feedback.userId = this.userId;
            feedback.type = this.type;
            feedback.content = this.content;
            feedback.images = this.images;
            feedback.status = this.status;
            feedback.replyContent = this.replyContent;
            feedback.replyTime = this.replyTime;
            feedback.rating = this.rating;
            feedback.contact = this.contact;
            return feedback;
        }
    }
}
