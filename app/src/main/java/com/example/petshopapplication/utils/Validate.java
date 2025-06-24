package com.example.petshopapplication.utils;

import android.content.Context;

import com.example.petshopapplication.R;

import java.text.NumberFormat;
import java.util.Locale;

public class Validate {

    public static boolean isPhoneNumberValid(String phoneNumber) {
        // Regex pattern for phone number validation
        return phoneNumber.matches("^(\\+84-|0)([35789][0-9]{8})$");
    }

    public static boolean isPasswordValid(String password) {
        // Regex pattern for password validation
        return password.matches("^.{8,}$");
    }

    public static boolean isFullNameValid(String fullName) {
        // Regex pattern for fullName validation
        return fullName.matches("^[a-zA-Z\\s]+$");
    }

    public static String isRegisterValid(Context context, String phoneNumber, String password, String fullName) {
        if(!isFullNameValid(fullName)) {
            return context.getString(R.string.valid_fullName_fail);
        }
        if (!isPhoneNumberValid(phoneNumber)) {
            return context.getString(R.string.valid_phone_fail);
        }
        if (!isPasswordValid(password)) {
            return context.getString(R.string.valid_password_fail);
        }
        return null;
    }

    // Format Money VND
    public static String formatVND(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    public static String isValidFeedback(Context context, String comment) {
        // not comment = valid
        if (comment == null || comment.isEmpty()) {
            return null;
        }

        // if has content -> validate
        if (comment.trim().isEmpty() || comment.length() > 200) {
            return context.getString(R.string.valid_feedback_fail);
        }
        return null;
    }
}
