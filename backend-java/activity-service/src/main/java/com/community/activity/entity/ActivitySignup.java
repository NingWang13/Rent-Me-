package com.community.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("activity_signup")
public class ActivitySignup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long userId;

    private Integer status;

    private LocalDateTime checkInTime;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
