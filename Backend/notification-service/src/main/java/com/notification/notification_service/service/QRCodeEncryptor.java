package com.notification.notification_service.service;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class QRCodeEncryptor {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    @Value("${NOTIFICATION_QRCODE_SECRET}")
    private static String base64Secret;

    public static String encrypt(String plainText) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new java.security.SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting QR data", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedText);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[decoded.length - IV_LENGTH_BYTE];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "AES");
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting QR data", e);
        }
    }
}
