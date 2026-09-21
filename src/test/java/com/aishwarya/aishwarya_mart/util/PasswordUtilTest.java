package com.aishwarya.aishwarya_mart.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashAndMatchShouldWork() {
        String password = "TestPassword123";
        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(PasswordUtil.matches(password, hash));
    }

    @Test
    void wrongPasswordShouldNotMatch() {
        String hash = PasswordUtil.hash("CorrectPassword123");

        assertFalse(PasswordUtil.matches("WrongPassword123", hash));
    }

    @Test
    void nullValuesShouldReturnFalse() {
        assertFalse(PasswordUtil.matches(null, "someHash"));
        assertFalse(PasswordUtil.matches("somePassword", null));
    }
}
