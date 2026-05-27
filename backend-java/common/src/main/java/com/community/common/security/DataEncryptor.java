package com.community.common.security;

import cn.hutool.crypto.symmetric.AES;

public class DataEncryptor {

    private static final String KEY = "community-platform-2024";

    public String encrypt(String data) {
        AES aes = new AES(KEY.getBytes());
        return aes.encryptHex(data.getBytes());
    }

    public String decrypt(String encryptedData) {
        AES aes = new AES(KEY.getBytes());
        return new String(aes.decrypt(encryptedData));
    }
}
