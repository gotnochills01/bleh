package com.example.mahila.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mahila.dto.*;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public static void postRequest(Context context, String endpoint, JSONObject body, ApiCallback<String> callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                TokenManager tm = new TokenManager(context);
                String token = tm.getToken();
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (body != null) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    mainHandler.post(() -> callback.onSuccess(response.toString()));
                } else {
                    mainHandler.post(() -> callback.onError("HTTP Error: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Connection error: " + e.getMessage()));
            }
        });
    }

    public static void getRequest(Context context, String endpoint, ApiCallback<String> callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                TokenManager tm = new TokenManager(context);
                String token = tm.getToken();
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    mainHandler.post(() -> callback.onSuccess(response.toString()));
                } else {
                    mainHandler.post(() -> callback.onError("HTTP Error: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Connection error: " + e.getMessage()));
            }
        });
    }
}
