package com.vizja.sw.lab6.lib.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SecurityUtil {
    private SecurityUtil() {
    }


    public static byte[] base64Decoding(String secret) {
        return Base64.getDecoder().decode(secret);
    }

    public static String base64Encoding(byte[] inputs) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(inputs);
    }

    public static String decodeToString(String base64Secret) {
        return new String(base64Decoding(base64Secret), UTF_8);
    }

    public static String urlDecoder(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("SHA3-256 algorithm not available", exception);
        }
    }


    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String computedHash = hashPassword(plainPassword);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                hashedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

}
