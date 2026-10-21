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

public class SignupActivity extends BaseActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView txtLoginPrompt;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

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
        tokenManager = new TokenManager(this);

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

        btnSignup.setOnClickListener(v -> signupUser());
    }

    private void setLoadingState(boolean isLoading) {
        btnSignup.setEnabled(!isLoading);
        if (txtLoginPrompt != null) txtLoginPrompt.setEnabled(!isLoading);
        btnSignup.setText(isLoading ? getString(R.string.loading_signup) : getString(R.string.sign_up));
    }

    private void signupUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("email", email);
            json.put("password", password);

            ApiClient.postRequest(this, ApiService.AUTH_SIGNUP, json, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    setLoadingState(false);
                    try {
                        JSONObject obj = new JSONObject(result);
                        String token = obj.optString("token", "");
                        Long userId = obj.optLong("userId", 1L);

                        dbHelper.insertUser(name, email, "");
                        tokenManager.saveSession(token, userId, email, name, "en");
                        appPreferences.setLoggedIn(true);
                        appPreferences.setCurrentUserEmail(email);

                        Toast.makeText(SignupActivity.this, getString(R.string.account_created_successfully), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, WelcomeActivity.class));
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(SignupActivity.this, getString(R.string.could_not_parse_registration), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String message) {
                    setLoadingState(false);
                    if (message != null && (message.contains("already registered") || message.contains("already exists"))) {
                        Toast.makeText(SignupActivity.this, getString(R.string.account_exists, email), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this, getString(R.string.registration_failed) + ": " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            setLoadingState(false);
            Toast.makeText(this, getString(R.string.error_building_request) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
