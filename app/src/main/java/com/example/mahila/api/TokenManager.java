package com.example.mahila.api;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "mahila_auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LANGUAGE = "preferred_language";
    private static final String KEY_MIGRATED = "is_migrated_to_mysql";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, Long userId, String email, String name, String language) {
        SharedPreferences.Editor editor = prefs.edit();
        if (token != null) {
            editor.putString(KEY_TOKEN, token);
        } else {
            editor.remove(KEY_TOKEN);
        }

        if (userId != null) {
            editor.putLong(KEY_USER_ID, userId);
        } else {
            editor.remove(KEY_USER_ID);
        }

        if (email != null) {
            editor.putString(KEY_USER_EMAIL, email);
        }
        if (name != null) {
            editor.putString(KEY_USER_NAME, name);
        }
        if (language != null) {
            editor.putString(KEY_LANGUAGE, language);
        }
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public boolean isMigrated() {
        return prefs.getBoolean(KEY_MIGRATED, false);
    }

    public void setMigrated(boolean value) {
        prefs.edit().putBoolean(KEY_MIGRATED, value).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
