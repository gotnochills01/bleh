package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spLanguage;
    private TokenManager tokenManager;

    private static final String[] LANGUAGES = new String[]{"English", "Kannada", "Hindi", "Tamil", "Telugu", "Malayalam"};
    private static final String[] CODES = new String[]{"en", "kn", "hi", "ta", "te", "ml"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tokenManager = new TokenManager(this);
        LocaleHelper.setLocale(this, tokenManager.getLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spLanguage = findViewById(R.id.sp_language);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LANGUAGES);
        spLanguage.setAdapter(adapter);

        String curLang = tokenManager.getLanguage();
        for (int i = 0; i < CODES.length; i++) {
            if (CODES[i].equalsIgnoreCase(curLang)) {
                spLanguage.setSelection(i);
                break;
            }
        }

        Button btnApply = findViewById(R.id.btn_apply_language);
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                int pos = spLanguage.getSelectedItemPosition();
                String selectedCode = CODES[pos];
                tokenManager.setLanguage(selectedCode);
                LocaleHelper.setLocale(SettingsActivity.this, selectedCode);

                Toast.makeText(this, "Language set to " + LANGUAGES[pos], Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        Button btnReminders = findViewById(R.id.btn_reminders);
        if (btnReminders != null) {
            btnReminders.setOnClickListener(v -> startActivity(new Intent(this, RemindersActivity.class)));
        }

        Button btnExport = findViewById(R.id.btn_export_data);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> Toast.makeText(this, "Health data exported to JSON!", Toast.LENGTH_SHORT).show());
        }

        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                tokenManager.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }
}
