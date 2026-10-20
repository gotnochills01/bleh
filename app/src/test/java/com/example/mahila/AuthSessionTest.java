package com.example.mahila;

import android.content.ContentValues;

import com.example.mahila.api.ApiClient;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthSessionTest {

    @Test
    public void testExistingUserLoginPreservesSavedPasswordAndAge() {
        DatabaseHelper.LoginUserUpdate update = DatabaseHelper.buildUserUpdateForLogin(
                "Jane",
                "jane@example.com",
                "",
                0,
                "saved-password",
                28
        );

        assertEquals("Jane", update.getName());
        assertEquals("saved-password", update.getPassword());
        assertEquals(28, update.getAge());
    }

    @Test
    public void testCurrentUserEmailFallsBackToSessionEmail() {
        assertEquals("session@example.com", AppPreferences.resolveCurrentUserEmail("", "session@example.com"));
        assertEquals("saved@example.com", AppPreferences.resolveCurrentUserEmail("saved@example.com", "session@example.com"));
    }

    @Test
    public void testCustomBaseUrlConfiguration() {
        ApiClient.setCustomBaseUrl("http://192.168.1.100:8080/");
        assertEquals("http://192.168.1.100:8080/", ApiClient.getBaseUrl());

        ApiClient.setCustomBaseUrl(null);
        assertEquals("http://10.0.2.2:8080/", ApiClient.getBaseUrl());
    }

    @Test
    public void testBaseUrlFormatting() {
        ApiClient.setCustomBaseUrl("http://192.168.1.50:8080");
        assertEquals("http://192.168.1.50:8080/", ApiClient.getBaseUrl());
        ApiClient.setCustomBaseUrl(null);
    }
}
