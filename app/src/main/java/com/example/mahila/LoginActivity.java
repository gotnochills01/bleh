package com.example.mahila;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGuest;
    private TextView txtSignup;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGuest = findViewById(R.id.btnGuest);
        txtSignup = findViewById(R.id.txtSignup);
        appPreferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGuest.setOnClickListener(v -> {
            appPreferences.setLoggedIn(false);
            appPreferences.setCurrentUserEmail("Guest");
            new TokenManager(LoginActivity.this).saveSession(null, null, "Guest", "Guest", "en");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("guest_mode", true);
            startActivity(intent);
            finish();
        });

        txtSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            ApiClient.postRequest(this, ApiService.AUTH_LOGIN, json,
                    new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    try {
                        JSONObject obj = new JSONObject(result);
                        String token = obj.optString("token", "");
                        long userId = obj.optLong("userId", -1L);
                        String name = obj.optString("name", "User");
                        String lang = obj.optString("preferredLanguage", "en");

                        if (token.isEmpty()) {
                            Toast.makeText(LoginActivity.this,
                                    "Login response did not contain a valid token",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Save validated user session locally in SQLite for offline access
                        dbHelper.insertUser(name, email, password);

                        // Save server JWT session
                        new TokenManager(LoginActivity.this).saveSession(
                                token, userId, email, name, lang);
                        appPreferences.setLoggedIn(true);
                        appPreferences.setCurrentUserEmail(email);

                        startActivity(new Intent(LoginActivity.this,
                                MainActivity.class));
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this,
                                "Could not process login response: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String message) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    // Only attempt offline local fallback if there is a genuine network connection failure / timeout.
                    // If the server responded with an HTTP status (e.g. HTTP 401, 400, 403), deny authentication!
                    if (message != null && (message.contains("Connection error") || message.contains("timeout") || message.contains("Failed to connect"))) {
                        fallbackLogin(email, password);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login Failed: Invalid credentials or " + message,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fallbackLogin(String email, String password) {
        if (dbHelper.checkLogin(email, password)) {
            appPreferences.setLoggedIn(true);
            appPreferences.setCurrentUserEmail(email);

            String userName = "User";
            try (Cursor cursor = dbHelper.getUser(email)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                    if (nameIdx >= 0 && !cursor.isNull(nameIdx)) {
                        userName = cursor.getString(nameIdx);
                    }
                }
            } catch (Exception ignored) {}

            // For offline local sessions, do not pass a fake JWT token to backend calls
            new TokenManager(LoginActivity.this).saveSession(null, null, email, userName, "en");
            Toast.makeText(this, "Network unavailable: Logged in via local offline data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this,
                    "Network unavailable and no matching local account found.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
