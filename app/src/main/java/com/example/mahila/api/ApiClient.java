package com.example.mahila.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private static String buildFullUrl(String endpoint) {
        if (endpoint == null) endpoint = "";
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }
        return BASE_URL + endpoint;
    }

    public static void postRequest(Context context, String endpoint, JSONObject body, ApiCallback<String> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            String fullUrl = buildFullUrl(endpoint);
            Log.d(TAG, "Executing POST request to: " + fullUrl);

            try {
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                TokenManager tm = new TokenManager(context);
                String token = tm.getToken();
                if (token != null && !token.trim().isEmpty()) {
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
                Log.d(TAG, "POST " + fullUrl + " returned status code: " + code);

                if (code >= 200 && code < 300) {
                    String response = readStream(conn.getInputStream());
                    mainHandler.post(() -> callback.onSuccess(response));
                } else {
                    String errorBody = readStream(conn.getErrorStream());
                    String errorMessage = formatHttpErrorMessage(code, errorBody);
                    Log.w(TAG, "POST " + fullUrl + " failed: " + errorMessage);
                    mainHandler.post(() -> callback.onError(errorMessage));
                }

            } catch (ConnectException e) {
                String msg = "Could not connect to server at " + fullUrl + ". Ensure Spring Boot is running on port 8080.";
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } catch (SocketTimeoutException e) {
                String msg = "Server connection timed out at " + fullUrl + ". Please try again.";
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } catch (Exception e) {
                String msg = "Network error (" + e.getClass().getSimpleName() + "): " + e.getMessage();
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public static void getRequest(Context context, String endpoint, ApiCallback<String> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            String fullUrl = buildFullUrl(endpoint);
            Log.d(TAG, "Executing GET request to: " + fullUrl);

            try {
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                TokenManager tm = new TokenManager(context);
                String token = tm.getToken();
                if (token != null && !token.trim().isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "GET " + fullUrl + " returned status code: " + code);

                if (code >= 200 && code < 300) {
                    String response = readStream(conn.getInputStream());
                    mainHandler.post(() -> callback.onSuccess(response));
                } else {
                    String errorBody = readStream(conn.getErrorStream());
                    String errorMessage = formatHttpErrorMessage(code, errorBody);
                    Log.w(TAG, "GET " + fullUrl + " failed: " + errorMessage);
                    mainHandler.post(() -> callback.onError(errorMessage));
                }

            } catch (ConnectException e) {
                String msg = "Could not connect to server at " + fullUrl + ". Ensure Spring Boot is running on port 8080.";
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } catch (SocketTimeoutException e) {
                String msg = "Server connection timed out at " + fullUrl + ". Please try again.";
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } catch (Exception e) {
                String msg = "Network error (" + e.getClass().getSimpleName() + "): " + e.getMessage();
                Log.e(TAG, msg, e);
                mainHandler.post(() -> callback.onError(msg));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private static String readStream(InputStream is) {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String formatHttpErrorMessage(int statusCode, String errorBody) {
        if (errorBody != null && !errorBody.trim().isEmpty()) {
            try {
                JSONObject json = new JSONObject(errorBody);
                if (json.has("message")) {
                    return "Server Error (" + statusCode + "): " + json.getString("message");
                } else if (json.has("error")) {
                    return "Server Error (" + statusCode + "): " + json.getString("error");
                }
            } catch (Exception ignored) {
                return "Server Error (" + statusCode + "): " + errorBody;
            }
        }
        switch (statusCode) {
            case 400:
                return "HTTP 400: Bad Request. Check your request details.";
            case 401:
                return "HTTP 401: Invalid credentials or unauthorized request.";
            case 403:
                return "HTTP 403: Access forbidden by server.";
            case 404:
                return "HTTP 404: Endpoint not found on server.";
            case 500:
                return "HTTP 500: Internal server error on backend.";
            default:
                return "HTTP Error " + statusCode + " from server.";
        }
    }
}
