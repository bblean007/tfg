package com.example.minitfg.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class ValidationUtilsTest {

    @Test
    public void testUsernameValidation() {
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertFalse(ValidationUtils.isValidUsername("us")); // Too short
        assertFalse(ValidationUtils.isValidUsername("user 123")); // Space
        assertFalse(ValidationUtils.isValidUsername("user@123")); // Special char
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(ValidationUtils.isValidPassword("Password123"));
        assertFalse(ValidationUtils.isValidPassword("pass123")); // No upper
        assertFalse(ValidationUtils.isValidPassword("PASSWORD123")); // No lower
        assertFalse(ValidationUtils.isValidPassword("Password")); // No digit
        assertFalse(ValidationUtils.isValidPassword("Pas1")); // Too short
    }
}
