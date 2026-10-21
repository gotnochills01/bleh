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

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGuest;
    private TextView txtSignup;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

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
        tokenManager = new TokenManager(this);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGuest.setOnClickListener(v -> {
            tokenManager.clear();
            tokenManager.saveSession(null, null, "Guest", "Guest", "en");
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

    private void setButtonsLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnGuest.setEnabled(!isLoading);
        if (txtSignup != null) txtSignup.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? getString(R.string.loading_login) : getString(R.string.log_in));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_email_password), Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsLoading(true);

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            ApiClient.postRequest(this, ApiService.AUTH_LOGIN, json,
                    new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    setButtonsLoading(false);

                    try {
                        JSONObject obj = new JSONObject(result);
                        String token = obj.optString("token", "");
                        long userId = obj.optLong("userId", -1L);
                        String name = obj.optString("name", "User");
                        String lang = obj.optString("preferredLanguage", "en");

                        if (token.isEmpty()) {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.login_response_missing_token),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Store local record & synchronized session tokens
                        dbHelper.insertUser(name, email, "", 0);
                        tokenManager.saveSession(token, userId, email, name, lang);
                        appPreferences.setLoggedIn(true);
                        appPreferences.setCurrentUserEmail(email);

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.could_not_parse_login) + ": " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String message) {
                    setButtonsLoading(false);
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed) + ": " + message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            setButtonsLoading(false);
            Toast.makeText(this, getString(R.string.error_building_request) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
