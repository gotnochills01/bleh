package com.example.mahila;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mahila.api.TokenManager;

public class WelcomeActivity extends AppCompatActivity {

    private TextView txtWelcome;
    private Button btnContinue;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        View root = findViewById(R.id.welcome_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        appPreferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);
        TokenManager tokenManager = new TokenManager(this);

        txtWelcome = findViewById(R.id.txtWelcome);
        btnContinue = findViewById(R.id.btnContinue);

        String currentEmail = appPreferences.getCurrentUserEmail();
        String name = tokenManager.getUserName();

        if (name == null || name.isEmpty() || name.equals("User")) {
            if (!currentEmail.isEmpty()) {
                try (Cursor cursor = dbHelper.getUser(currentEmail)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        if (name != null && !name.isEmpty() && !name.equals("User")) {
            txtWelcome.setText("Welcome, " + name + " 👋");
        } else {
            txtWelcome.setText("Welcome! 👋");
        }

        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        });
    }
}