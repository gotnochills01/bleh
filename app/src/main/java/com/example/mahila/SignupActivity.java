package com.example.mahila;

import android.content.Intent;
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

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView txtLoginPrompt;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        View root = findViewById(R.id.signup_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        appPreferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        txtLoginPrompt = findViewById(R.id.txtLoginPrompt);

        if (txtLoginPrompt != null) {
            txtLoginPrompt.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("email", email);
                json.put("password", password);

                ApiClient.postRequest(this, ApiService.AUTH_SIGNUP, json, new ApiClient.ApiCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            String token = obj.optString("token");
                            Long userId = obj.optLong("userId", 1L);

                            // Also save locally in SQLite for offline preservation
                            dbHelper.insertUser(name, email, password);

                            new TokenManager(SignupActivity.this).saveSession(token, userId, email, name, "en");
                            appPreferences.setLoggedIn(true);
                            appPreferences.setCurrentUserEmail(email);

                            Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, WelcomeActivity.class));
                            finish();
                        } catch (Exception e) {
                            fallbackSignup(name, email, password);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        fallbackSignup(name, email, password);
                    }
                });
            } catch (Exception e) {
                fallbackSignup(name, email, password);
            }
        });
    }

    private void fallbackSignup(String name, String email, String password) {
        boolean isInserted = dbHelper.insertUser(name, email, password);
        if (isInserted) {
            appPreferences.setLoggedIn(true);
            appPreferences.setCurrentUserEmail(email);
            new TokenManager(this).saveSession("local_token", 1L, email, name, "en");

            Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Email may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}