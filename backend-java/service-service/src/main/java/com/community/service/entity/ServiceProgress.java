package com.community.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("service_progress")
public class ServiceProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long helperId;

    private Integer step;

    private String stepName;

    private String description;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;

    private String location;

    private String photoUrl;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getHelperId() {
        return helperId;
    }

    public void setHelperId(Long helperId) {
        this.helperId = helperId;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long orderId;
        private Long helperId;
        private Integer step;
        private String stepName;
        private String description;
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime completeTime;
        private String location;
        private String photoUrl;

        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder helperId(Long helperId) { this.helperId = helperId; return this; }
        public Builder step(Integer step) { this.step = step; return this; }
        public Builder stepName(String stepName) { this.stepName = stepName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder completeTime(LocalDateTime completeTime) { this.completeTime = completeTime; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder photoUrl(String photoUrl) { this.photoUrl = photoUrl; return this; }

        public ServiceProgress build() {
            ServiceProgress p = new ServiceProgress();
            p.orderId = this.orderId;
            p.helperId = this.helperId;
            p.step = this.step;
            p.stepName = this.stepName;
            p.description = this.description;
            p.status = this.status;
            p.startTime = this.startTime;
            p.completeTime = this.completeTime;
            p.location = this.location;
            p.photoUrl = this.photoUrl;
            return p;
        }
    }
}
