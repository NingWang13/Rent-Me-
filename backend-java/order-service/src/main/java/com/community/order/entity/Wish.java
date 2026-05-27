package com.community.order.entity;

import com.community.common.entity.BaseEntity;

import java.time.LocalDateTime;

public class Wish extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String title;

    private String content;

    private Integer category;

    private Integer rewardCredit;

    private Integer status;

    private Long claimedBy;

    private LocalDateTime claimTime;

    private String latitude;

    private String longitude;

    private String location;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getRewardCredit() {
        return rewardCredit;
    }

    public void setRewardCredit(Integer rewardCredit) {
        this.rewardCredit = rewardCredit;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(Long claimedBy) {
        this.claimedBy = claimedBy;
    }

    public LocalDateTime getClaimTime() {
        return claimTime;
    }

    public void setClaimTime(LocalDateTime claimTime) {
        this.claimTime = claimTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
