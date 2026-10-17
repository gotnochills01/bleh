package com.example.mahila;

import android.content.Intent;
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

        btnLogin.setOnClickListener(v -> loginUser());

        btnGuest.setOnClickListener(v -> {
            appPreferences.setLoggedIn(false);
            appPreferences.setCurrentUserEmail("Guest");
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
                                    "Login response did not contain a token",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        new TokenManager(LoginActivity.this).saveSession(
                                token, userId, email, name, lang);
                        appPreferences.setLoggedIn(true);
                        appPreferences.setCurrentUserEmail(email);

                        startActivity(new Intent(LoginActivity.this,
                                MainActivity.class));
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this,
                                "Could not read login response: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String message) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(LoginActivity.this,
                            "Login failed: " + message,
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
