package com.community.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 微信登录请求DTO
 */
public class WechatLoginRequest implements Serializable {

    @NotBlank(message = "code不能为空")
    private String code;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    private String avatar;

    public WechatLoginRequest() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
