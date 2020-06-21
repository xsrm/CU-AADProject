package com.michael.aadproject;

import android.content.SharedPreferences;
import android.util.Patterns;
import android.widget.EditText;

public final class EntryHelper {
    public static boolean checkName(String name) {
        if (name.length() < 2 || name.contains("[0-9]+")) {
            return false;
        }
        return true;
    }

    public static boolean checkEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean checkPassword(String password) {
        if (password.isEmpty() || password.contains(" ")) {
            return false;
        }
        return true;
    }

    public static void saveEmail(SharedPreferences sharedPref, String EMAIL_KEY,
                                 EditText editEmail) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(EMAIL_KEY, editEmail.getText().toString());
        editor.commit();
    }
}
