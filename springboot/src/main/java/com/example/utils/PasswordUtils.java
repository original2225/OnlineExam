package com.example.utils;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (ObjectUtil.isEmpty(rawPassword) || ObjectUtil.isEmpty(storedPassword)) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    public static boolean needsUpgrade(String storedPassword) {
        return ObjectUtil.isNotEmpty(storedPassword) && !isBcrypt(storedPassword);
    }

    private static boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
