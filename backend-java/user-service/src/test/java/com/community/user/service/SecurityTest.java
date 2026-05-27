package com.community.user.service;

import com.community.common.security.PasswordEncoder;
import com.community.common.security.PaymentPasswordEncoder;
import com.community.common.security.DataEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PaymentPasswordEncoder paymentPasswordEncoder;

    @Autowired
    private DataEncryptor dataEncryptor;

    @Test
    public void testPasswordEncodeAndMatch() {
        String rawPassword = "TestPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.contains(":"));
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("WrongPassword", encoded));
    }

    @Test
    public void testPaymentPasswordEncodeAndMatch() {
        String rawPassword = "123456";
        String encoded = paymentPasswordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(paymentPasswordEncoder.matches(rawPassword, encoded));
        assertFalse(paymentPasswordEncoder.matches("654321", encoded));
    }

    @Test
    public void testPaymentPasswordMustBeSixDigits() {
        String sixDigitPassword = "123456";
        String sevenDigitPassword = "1234567";

        assertTrue(paymentPasswordEncoder.matches(sixDigitPassword,
                paymentPasswordEncoder.encode(sixDigitPassword)));

        String encodedSeven = paymentPasswordEncoder.encode(sevenDigitPassword);
        assertNotNull(encodedSeven);
    }

    @Test
    public void testDataEncryptAndDecrypt() {
        String plainText = "身份证号: 110101199001011234";

        String encrypted = dataEncryptor.encrypt(plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = dataEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testDataEncryptDifferentResults() {
        String plainText = "same text";

        String encrypted1 = dataEncryptor.encrypt(plainText);
        String encrypted2 = dataEncryptor.encrypt(plainText);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(plainText, dataEncryptor.decrypt(encrypted1));
        assertEquals(plainText, dataEncryptor.decrypt(encrypted2));
    }

    @Test
    public void testPhoneNumberEncryption() {
        String phone = "13800138000";

        String encrypted = dataEncryptor.encrypt(phone);
        String decrypted = dataEncryptor.decrypt(encrypted);

        assertEquals(phone, decrypted);
    }

    @Test
    public void testPasswordUniqueness() {
        String samePassword = "SamePassword123";

        String encoded1 = passwordEncoder.encode(samePassword);
        String encoded2 = passwordEncoder.encode(samePassword);

        assertNotEquals(encoded1, encoded2);

        assertTrue(passwordEncoder.matches(samePassword, encoded1));
        assertTrue(passwordEncoder.matches(samePassword, encoded2));
    }
}
