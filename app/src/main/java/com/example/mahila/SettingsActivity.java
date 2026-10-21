package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.LocaleHelper;

public class SettingsActivity extends BaseActivity {

    private Spinner spLanguage;
    private TokenManager tokenManager;
    private TextView tvSelectedLanguage;

    private static final String[] CODES = new String[]{"en", "kn", "hi", "ta", "te", "ml"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);
        setContentView(R.layout.activity_settings);

        spLanguage = findViewById(R.id.sp_language);
        tvSelectedLanguage = findViewById(R.id.tv_selected_language);

        String[] languages = new String[]{
                getString(R.string.language_english),
                getString(R.string.language_kannada),
                getString(R.string.language_hindi),
                getString(R.string.language_tamil),
                getString(R.string.language_telugu),
                getString(R.string.language_malayalam)
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
        updateSelectedLanguageLabel();

        spLanguage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateSelectedLanguageLabel();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateSelectedLanguageLabel();
            }
        });

        Button btnApply = findViewById(R.id.btn_apply_language);
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                int pos = spLanguage.getSelectedItemPosition();
                String selectedCode = CODES[pos];
                tokenManager.setLanguage(selectedCode);
                LocaleHelper.setLocale(SettingsActivity.this, selectedCode);
                String selectedLanguageName = getLanguageDisplayName(selectedCode);
                Toast.makeText(this, getString(R.string.language_updated_to, selectedLanguageName), Toast.LENGTH_SHORT).show();
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

    private void updateSelectedLanguageLabel() {
        if (tvSelectedLanguage == null) return;
        String selectedText = spLanguage != null && spLanguage.getSelectedItem() != null
                ? spLanguage.getSelectedItem().toString()
                : getString(R.string.language_english);
        tvSelectedLanguage.setText(getString(R.string.language_selected_label, selectedText));
    }

    private String getLanguageDisplayName(String code) {
        String normalized = LocaleHelper.normalizeLanguageCode(code);
        if (LocaleHelper.LANGUAGE_KN.equals(normalized)) {
            return getString(R.string.language_kannada);
        }
        if (LocaleHelper.LANGUAGE_HI.equals(normalized)) {
            return getString(R.string.language_hindi);
        }
        if (LocaleHelper.LANGUAGE_TA.equals(normalized)) {
            return getString(R.string.language_tamil);
        }
        if (LocaleHelper.LANGUAGE_TE.equals(normalized)) {
            return getString(R.string.language_telugu);
        }
        if (LocaleHelper.LANGUAGE_ML.equals(normalized)) {
            return getString(R.string.language_malayalam);
        }
        return getString(R.string.language_english);
    }
}
