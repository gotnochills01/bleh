package com.example.mahila;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomLoggerActivity extends AppCompatActivity {

    private ChipGroup symptomChipGroup;
    private TextView historyText;
    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_logger);

        View root = findViewById(R.id.symptom_logger_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.symptom_logger_toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        dbHelper = new DatabaseHelper(this);
        tokenManager = new TokenManager(this);

        symptomChipGroup = findViewById(R.id.symptom_chip_group);
        historyText = findViewById(R.id.text_symptom_history);
        MaterialButton saveButton = findViewById(R.id.button_save_symptoms);

        populateDefaultChips();

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveSymptoms());
        }

        loadHistory();
    }

    private void populateDefaultChips() {
        if (symptomChipGroup == null) return;
        symptomChipGroup.removeAllViews();

        String[] symptoms = {"Cramps", "Headache", "Bloating", "Fatigue", "Acne", "Mood Swings", "Backache"};
        for (String s : symptoms) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCheckable(true);
            symptomChipGroup.addView(chip);
        }
    }

    private void saveSymptoms() {
        if (symptomChipGroup == null) return;

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < symptomChipGroup.getChildCount(); i++) {
            View v = symptomChipGroup.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.isChecked()) {
                    selected.add(chip.getText().toString());
                }
            }
        }

        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one symptom", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = tokenManager.getUserEmail();
        String createdAt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (String symptom : selected) {
            dbHelper.saveSymptom(userEmail, symptom, "MODERATE", createdAt, false);

            try {
                JSONObject json = new JSONObject();
                json.put("symptomName", symptom);
                json.put("severity", "MODERATE");
                json.put("loggedDate", createdAt);

                ApiClient.postRequest(this, ApiService.SYMPTOMS, json, new ApiClient.ApiCallback<String>() {
                    @Override public void onSuccess(String result) {
                        dbHelper.markSymptomsSynced(userEmail);
                    }
                    @Override public void onError(String message) {}
                });
            } catch (Exception ignored) {}
        }

        Toast.makeText(this, "Symptoms logged successfully!", Toast.LENGTH_SHORT).show();
        loadHistory();
    }

    private void loadHistory() {
        if (historyText == null) return;

        String userEmail = tokenManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            historyText.setText("No symptom history found.");
            return;
        }

        StringBuilder sb = new StringBuilder("Recent Symptom Log:\n");
        try (Cursor cursor = dbHelper.getSymptoms(userEmail)) {
            if (cursor != null && cursor.moveToFirst()) {
                int count = 0;
                int symptomIdx = cursor.getColumnIndex(DatabaseHelper.KEY_SYMPTOM);
                int dateIdx = cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);

                do {
                    String symptom = symptomIdx >= 0 ? cursor.getString(symptomIdx) : "Symptom";
                    String date = dateIdx >= 0 ? cursor.getString(dateIdx) : "";
                    sb.append("• ").append(symptom).append(" (").append(date).append(")\n");
                    count++;
                } while (cursor.moveToNext() && count < 5);

                historyText.setText(sb.toString());
            } else {
                historyText.setText("No symptoms logged yet.");
            }
        } catch (Exception e) {
            historyText.setText("No symptoms logged yet.");
        }
    }
}
