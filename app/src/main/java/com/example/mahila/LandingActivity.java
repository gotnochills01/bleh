package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppPreferences appPreferences = new AppPreferences(this);
        if (appPreferences.isLoggedIn()) {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);

        View root = findViewById(R.id.landing_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);
        TextView tvLoginPrompt = findViewById(R.id.tv_login_prompt);

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                startActivity(new Intent(LandingActivity.this, LoginActivity.class));
            });
        }

        if (tvLoginPrompt != null) {
            tvLoginPrompt.setOnClickListener(v -> {
                startActivity(new Intent(LandingActivity.this, LoginActivity.class));
            });
        }
    }
}
