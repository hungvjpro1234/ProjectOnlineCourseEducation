// app/src/main/java/com/example/projectonlinecourseeducation/core/utils/Utils.java
package com.example.projectonlinecourseeducation.core.utils;

import android.util.Patterns;

public class Utils {
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    // username: 3-20 ký tự, chữ/số/._, bắt đầu bằng chữ
    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches("^[A-Za-z][A-Za-z0-9._]{2,19}$");
    }
}
