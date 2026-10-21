package com.example.mahila;

import static org.junit.Assert.assertEquals;

import android.content.SharedPreferences;

import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.LocaleHelper;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class LocaleHelperTest {

    @Test
    public void localeHelperNormalizesSupportedCodes() {
        assertEquals("en", LocaleHelper.normalizeLanguageCode("en-US"));
        assertEquals("kn", LocaleHelper.normalizeLanguageCode("kn-IN"));
        assertEquals("hi", LocaleHelper.normalizeLanguageCode("hi-IN"));
        assertEquals("ta", LocaleHelper.normalizeLanguageCode("ta-IN"));
        assertEquals("te", LocaleHelper.normalizeLanguageCode("te-IN"));
        assertEquals("ml", LocaleHelper.normalizeLanguageCode("ml-IN"));
        assertEquals("en", LocaleHelper.normalizeLanguageCode("fr"));
    }

    @Test
    public void tokenManagerPersistsLanguageSelection() {
        SharedPreferences prefs = new InMemorySharedPreferences();
        TokenManager tokenManager = new TokenManager(prefs);

        tokenManager.setLanguage("kn");
        assertEquals("kn", tokenManager.getLanguage());

        tokenManager.setLanguage("en");
        assertEquals("en", tokenManager.getLanguage());
    }

    private static class InMemorySharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Map<String, ?> getAll() { return new HashMap<>(values); }
        @Override public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defValue;
        }
        @Override public java.util.Set<String> getStringSet(String key, java.util.Set<String> defValues) {
            Object value = values.get(key);
            return value instanceof java.util.Set ? (java.util.Set<String>) value : defValues;
        }
        @Override public int getInt(String key, int defValue) { Object value = values.get(key); return value instanceof Integer ? (Integer) value : defValue; }
        @Override public long getLong(String key, long defValue) { Object value = values.get(key); return value instanceof Long ? (Long) value : defValue; }
        @Override public float getFloat(String key, float defValue) { Object value = values.get(key); return value instanceof Float ? (Float) value : defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { Object value = values.get(key); return value instanceof Boolean ? (Boolean) value : defValue; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public Editor edit() { return new InMemoryEditor(); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }

        private class InMemoryEditor implements Editor {
            @Override public Editor putString(String key, String value) { values.put(key, value); return this; }
            @Override public Editor putStringSet(String key, java.util.Set<String> values) { return this; }
            @Override public Editor putInt(String key, int value) { values.put(key, value); return this; }
            @Override public Editor putLong(String key, long value) { values.put(key, value); return this; }
            @Override public Editor putFloat(String key, float value) { values.put(key, value); return this; }
            @Override public Editor putBoolean(String key, boolean value) { values.put(key, value); return this; }
            @Override public Editor remove(String key) { values.remove(key); return this; }
            @Override public Editor clear() { values.clear(); return this; }
            @Override public boolean commit() { return true; }
            @Override public void apply() { }
        }
    }
}
