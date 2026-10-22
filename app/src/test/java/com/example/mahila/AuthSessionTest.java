package com.example.mahila;

import com.example.mahila.api.ApiClient;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthSessionTest {

    @Test
    public void testCustomBaseUrlConfiguration() {
        ApiClient.setCustomBaseUrl("http://192.168.1.100:8080/");
        assertEquals("http://192.168.1.100:8080/", ApiClient.getBaseUrl());

        ApiClient.setCustomBaseUrl(null);
        assertEquals("http://10.166.42.93:8080/", ApiClient.getBaseUrl());
    }

    @Test
    public void testBaseUrlFormatting() {
        ApiClient.setCustomBaseUrl("http://10.166.42.93:8080");
        assertEquals("http://10.166.42.93:8080/", ApiClient.getBaseUrl());
        ApiClient.setCustomBaseUrl(null);
    }

    @Test
    public void testAppPreferencesNullContextSafety() {
        AppPreferences nullPrefs = new AppPreferences(null);
        assertFalse(nullPrefs.isLoggedIn());
        assertEquals("", nullPrefs.getCurrentUserEmail());

        nullPrefs.setLoggedIn(true);
        nullPrefs.setCurrentUserEmail("test@example.com");
        assertFalse(nullPrefs.isLoggedIn());
    }
}
