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

    private final Context context;
    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
        this.prefs = context != null ? context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) : null;
    }

    public void setLoggedIn(boolean loggedIn) {
        if (prefs != null) prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs != null && prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setCurrentUserEmail(String email) {
        if (prefs != null) prefs.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply();
    }

    public String getCurrentUserEmail() {
        String email = prefs != null ? prefs.getString(KEY_CURRENT_USER_EMAIL, "") : "";
        if ((email == null || email.isEmpty()) && context != null) {
            TokenManager tm = new TokenManager(context);
            String tokenEmail = tm.getUserEmail();
            if (tokenEmail != null && !tokenEmail.isEmpty()) {
                setCurrentUserEmail(tokenEmail);
                return tokenEmail;
            }
        }
        return email != null ? email : "";
    }

    public void clearCurrentUserEmail() {
        if (prefs != null) prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply();
    }

    public void logout() {
        setLoggedIn(false);
        clearCurrentUserEmail();
    }

    public void saveClinicianReminder(String userEmail, long millis) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return;
        prefs.edit().putLong(PREFIX_CLINICIAN_REMINDER + userEmail, millis).apply();
    }

    public long getClinicianReminderMillis(String userEmail) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return -1L;
        return prefs.getLong(PREFIX_CLINICIAN_REMINDER + userEmail, -1L);
    }

    public void saveLastHealthTip(String userEmail, String title) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return;
        prefs.edit().putString(PREFIX_LAST_HEALTH_TIP + userEmail, title).apply();
    }

    public String getLastHealthTip(String userEmail) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return "";
        return prefs.getString(PREFIX_LAST_HEALTH_TIP + userEmail, "");
    }

    public void saveProfileImagePath(String userEmail, String path) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return;
        prefs.edit().putString(PREFIX_PROFILE_IMAGE + userEmail, path).apply();
    }

    public String getProfileImagePath(String userEmail) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return "";
        return prefs.getString(PREFIX_PROFILE_IMAGE + userEmail, "");
    }

    public void clearProfileImagePath(String userEmail) {
        if (userEmail == null || userEmail.isEmpty() || prefs == null) return;
        prefs.edit().remove(PREFIX_PROFILE_IMAGE + userEmail).apply();
    }
}
