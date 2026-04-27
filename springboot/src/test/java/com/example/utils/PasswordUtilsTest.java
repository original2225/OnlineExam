package com.example.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void matchesEncodedPassword() {
        String encoded = PasswordUtils.encode("secret123");

        assertTrue(PasswordUtils.matches("secret123", encoded));
        assertFalse(PasswordUtils.matches("bad", encoded));
        assertFalse(PasswordUtils.needsUpgrade(encoded));
    }

    @Test
    void supportsPlaintextUpgrade() {
        assertTrue(PasswordUtils.matches("legacy", "legacy"));
        assertTrue(PasswordUtils.needsUpgrade("legacy"));
    }
}
