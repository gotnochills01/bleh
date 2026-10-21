package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.LocaleHelper;

public class SettingsActivity extends BaseActivity {

    private Spinner spLanguage;
    private TokenManager tokenManager;

    private static final String[] CODES = new String[]{"en", "kn"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);
        setContentView(R.layout.activity_settings);

        spLanguage = findViewById(R.id.sp_language);
        String[] languages = new String[]{
                getString(R.string.language_english),
                getString(R.string.language_kannada)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

                Toast.makeText(this, getString(R.string.language_updated_to, languages[pos]), Toast.LENGTH_SHORT).show();
                recreate();
            });
        }

        Button btnReminders = findViewById(R.id.btn_reminders);
        if (btnReminders != null) {
            btnReminders.setOnClickListener(v -> startActivity(new Intent(this, RemindersActivity.class)));
        }

        Button btnExport = findViewById(R.id.btn_export_data);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> Toast.makeText(this, getString(R.string.health_data_exported), Toast.LENGTH_SHORT).show());
        }

        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                tokenManager.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
