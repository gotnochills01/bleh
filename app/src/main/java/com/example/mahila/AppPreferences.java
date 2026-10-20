package com.example.mahila;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mahila.api.TokenManager;

public class AppPreferences {
    public static final int CYCLE_LENGTH_DAYS = 28;
    private static final String PREFS_NAME = "mahila_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_CURRENT_USER_EMAIL = "current_user_email";
    private static final String PREFIX_CLINICIAN_REMINDER = "clinician_reminder_";
    private static final String PREFIX_LAST_HEALTH_TIP = "last_health_tip_";
    private static final String PREFIX_PROFILE_IMAGE = "profile_image_path_";

    private final SharedPreferences prefs;
    private final Context context;

    public AppPreferences(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String resolveCurrentUserEmail(String storedUserEmail, String sessionUserEmail) {
        if (storedUserEmail != null && !storedUserEmail.trim().isEmpty()) {
            return storedUserEmail.trim();
        }
        if (sessionUserEmail != null && !sessionUserEmail.trim().isEmpty()) {
            return sessionUserEmail.trim();
        }
        return "";
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setCurrentUserEmail(String email) {
        prefs.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply();
    }

    public String getCurrentUserEmail() {
        String storedEmail = prefs.getString(KEY_CURRENT_USER_EMAIL, "");
        String sessionEmail = context != null ? new TokenManager(context).getUserEmail() : null;
        return resolveCurrentUserEmail(storedEmail, sessionEmail);
    }

    public void clearCurrentUserEmail() {
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply();
    }

    public void logout() {
        setLoggedIn(false);
        clearCurrentUserEmail();
    }

    public void saveClinicianReminder(String userEmail, long millis) {
        if (userEmail == null || userEmail.isEmpty()) return;
        prefs.edit().putLong(PREFIX_CLINICIAN_REMINDER + userEmail, millis).apply();
    }

    public long getClinicianReminderMillis(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return -1L;
        return prefs.getLong(PREFIX_CLINICIAN_REMINDER + userEmail, -1L);
    }

    public void saveLastHealthTip(String userEmail, String title) {
        if (userEmail == null || userEmail.isEmpty()) return;
        prefs.edit().putString(PREFIX_LAST_HEALTH_TIP + userEmail, title).apply();
    }

    public String getLastHealthTip(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return "";
        return prefs.getString(PREFIX_LAST_HEALTH_TIP + userEmail, "");
    }

    public void saveProfileImagePath(String userEmail, String path) {
        if (userEmail == null || userEmail.isEmpty()) return;
        prefs.edit().putString(PREFIX_PROFILE_IMAGE + userEmail, path).apply();
    }

    public String getProfileImagePath(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return "";
        return prefs.getString(PREFIX_PROFILE_IMAGE + userEmail, "");
    }

    public void clearProfileImagePath(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return;
        prefs.edit().remove(PREFIX_PROFILE_IMAGE + userEmail).apply();
    }
}
