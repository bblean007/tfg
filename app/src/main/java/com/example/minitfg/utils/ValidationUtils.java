package com.example.minitfg.utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < 3) return false;
        // Alfanumérico sin espacios
        return username.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        // Al menos una mayúscula, una minúscula y un número
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
