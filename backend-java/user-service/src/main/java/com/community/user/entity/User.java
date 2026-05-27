package com.community.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.common.entity.BaseEntity;

@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private String openid;

    private Integer userType;

    private String idCard;

    private Boolean idCardVerified;

    private Boolean faceVerified;

    private String paymentPassword;

    private Integer creditScore;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private String passwordHistory = "[]";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Boolean getIdCardVerified() {
        return idCardVerified;
    }

    public void setIdCardVerified(Boolean idCardVerified) {
        this.idCardVerified = idCardVerified;
    }

    public Boolean getFaceVerified() {
        return faceVerified;
    }

    public void setFaceVerified(Boolean faceVerified) {
        this.faceVerified = faceVerified;
    }

    public String getPaymentPassword() {
        return paymentPassword;
    }

    public void setPaymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPasswordHistory() {
        return passwordHistory;
    }

    public void setPasswordHistory(String passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String phone;
        private String nickname;
        private String avatarUrl;
        private Integer userType;
        private String idCard;
        private Boolean idCardVerified;
        private Boolean faceVerified;
        private String paymentPassword;
        private Integer creditScore;
        private Integer status;
        private String passwordHistory = "[]";

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder userType(Integer userType) {
            this.userType = userType;
            return this;
        }

        public Builder idCard(String idCard) {
            this.idCard = idCard;
            return this;
        }

        public Builder idCardVerified(Boolean idCardVerified) {
            this.idCardVerified = idCardVerified;
            return this;
        }

        public Builder faceVerified(Boolean faceVerified) {
            this.faceVerified = faceVerified;
            return this;
        }

        public Builder paymentPassword(String paymentPassword) {
            this.paymentPassword = paymentPassword;
            return this;
        }

        public Builder creditScore(Integer creditScore) {
            this.creditScore = creditScore;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder passwordHistory(String passwordHistory) {
            this.passwordHistory = passwordHistory;
            return this;
        }

        public User build() {
            User user = new User();
            user.username = this.username;
            user.password = this.password;
            user.phone = this.phone;
            user.nickname = this.nickname;
            user.avatarUrl = this.avatarUrl;
            user.userType = this.userType;
            user.idCard = this.idCard;
            user.idCardVerified = this.idCardVerified;
            user.faceVerified = this.faceVerified;
            user.paymentPassword = this.paymentPassword;
            user.creditScore = this.creditScore;
            user.status = this.status;
            user.passwordHistory = this.passwordHistory;
            return user;
        }
    }
}
