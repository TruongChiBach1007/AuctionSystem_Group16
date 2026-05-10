package com.auction.utils;

public class InputValidator {

    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && username.length() >= 4;
    }

    public static boolean isValidPassword(String password) {

        return password != null && password.length() >= 6;
    }
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
}