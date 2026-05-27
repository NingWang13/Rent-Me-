package com.community.common.security;

import cn.hutool.crypto.digest.DigestUtil;

public class PaymentPasswordEncoder {

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.length() != 6) {
            throw new IllegalArgumentException("支付密码必须为6位数字");
        }
        return DigestUtil.sha256Hex("payment_salt_" + rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.length() != 6) {
            return false;
        }
        String expectedHash = DigestUtil.sha256Hex("payment_salt_" + rawPassword);
        return expectedHash.equals(encodedPassword);
    }
}
