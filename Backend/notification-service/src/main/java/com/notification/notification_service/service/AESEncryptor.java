package com.notification.notification_service.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESEncryptor {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public static String encrypt(String plainText, String base64Secret) {
        // Input validation
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }
        if (base64Secret == null || base64Secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 secret cannot be null or empty");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Secret);

            // Validate AES key length (must be 16, 24, or 32 bytes)
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalArgumentException(String.format(
                    "Invalid AES key length: %d bytes. Must be 16 (AES-128), 24 (AES-192), or 32 (AES-256) bytes.",
                    keyBytes.length));
            }
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

    public static String decrypt(String encryptedText, String base64Secret) {
        // Input validation
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }
        if (base64Secret == null || base64Secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 secret cannot be null or empty");
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedText);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[decoded.length - IV_LENGTH_BYTE];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            byte[] keyBytes = Base64.getDecoder().decode(base64Secret);

            // Validate AES key length (must be 16, 24, or 32 bytes)
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalArgumentException(String.format(
                    "Invalid AES key length: %d bytes. Must be 16 (AES-128), 24 (AES-192), or 32 (AES-256) bytes.",
                    keyBytes.length));
            }

            SecretKey key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting QR data", e);
        }
    }
}
