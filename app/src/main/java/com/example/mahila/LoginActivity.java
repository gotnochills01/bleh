package com.example.mahila;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View root = findViewById(R.id.login_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        appPreferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGuest = findViewById(R.id.btnGuest);
        txtSignup = findViewById(R.id.txtSignup);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                ApiClient.postRequest(this, ApiService.AUTH_LOGIN, json, new ApiClient.ApiCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            String token = obj.optString("token");
                            Long userId = obj.optLong("userId", 1L);
                            String name = obj.optString("name", "User");
                            String lang = obj.optString("preferredLanguage", "en");

                            new TokenManager(LoginActivity.this).saveSession(token, userId, email, name, lang);
                            appPreferences.setLoggedIn(true);
                            appPreferences.setCurrentUserEmail(email);

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (Exception e) {
                            fallbackLogin(email, password);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        fallbackLogin(email, password);
                    }
                });
            } catch (Exception e) {
                fallbackLogin(email, password);
            }
        });

        btnGuest.setOnClickListener(v -> {
            appPreferences.setLoggedIn(false);
            appPreferences.clearCurrentUserEmail();
            new TokenManager(LoginActivity.this).saveSession(null, null, null, "Guest", "en");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        txtSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void fallbackLogin(String email, String password) {
        if (dbHelper.checkLogin(email, password)) {
            appPreferences.setLoggedIn(true);
            appPreferences.setCurrentUserEmail(email);

            String userName = "User";
            try (Cursor cursor = dbHelper.getUser(email)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                    if (nameIdx >= 0) {
                        userName = cursor.getString(nameIdx);
                    }
                }
            } catch (Exception ignored) {}

            new TokenManager(LoginActivity.this).saveSession("local_token", 1L, email, userName, "en");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
