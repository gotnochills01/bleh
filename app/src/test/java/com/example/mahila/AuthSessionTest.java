package com.example.mahila;

import com.example.mahila.api.ApiClient;
import com.example.mahila.api.TokenManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthSessionTest {

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
