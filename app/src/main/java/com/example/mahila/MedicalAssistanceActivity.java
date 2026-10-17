package com.example.mahila;

import android.content.Intent;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicalAssistanceActivity extends AppCompatActivity {

    private static final String INTRO_TEXT =
            "Find care resources. This is not a substitute for professional medical advice.";
    private static final String CLINICIAN_DETAIL =
            "Book a consultation or visit a nearby clinic for personalized care.";

    private AppPreferences preferences;
    private DatabaseHelper dbHelper;
    private TextView introText;
    private TextView clinicianDetailText;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_assistance);

        View root = findViewById(R.id.medical_assistance_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.medical_assistance_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Medical Assistance");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        preferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);
        introText = findViewById(R.id.text_medical_intro);
        clinicianDetailText = findViewById(R.id.text_clinician_detail);

        MaterialCardView clinicianCard = findViewById(R.id.card_talk_to_clinician);
        if (clinicianCard != null) {
            clinicianCard.setOnClickListener(v -> {
                saveClinicianReminder();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_TAB", "ASSISTANCE");
                startActivity(intent);
            });
        }

        MaterialCardView urgentCard = findViewById(R.id.card_urgent_care);
        if (urgentCard != null) {
            urgentCard.setOnClickListener(v -> {
                startActivity(new Intent(this, EmergencyContactsActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSavedInfo();
    }

    private void saveClinicianReminder() {
        String userEmail = preferences.getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Opening Medical Map Search...", Toast.LENGTH_SHORT).show();
            return;
        }
        preferences.saveClinicianReminder(userEmail, System.currentTimeMillis());
        showSavedInfo();
        Toast.makeText(this, "Opening Nearby Healthcare Providers...", Toast.LENGTH_SHORT).show();
    }

    private void showSavedInfo() {
        String userEmail = preferences.getCurrentUserEmail();
        StringBuilder intro = new StringBuilder(INTRO_TEXT);

        if (userEmail != null && !userEmail.isEmpty()) {
            Cursor periodCursor = dbHelper.getPeriod(userEmail);
            if (periodCursor != null) {
                if (periodCursor.moveToFirst()) {
                    int dateIndex = periodCursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                    if (dateIndex != -1) {
                        long periodMillis = periodCursor.getLong(dateIndex);
                        intro.append("\n\nLast period start: ")
                                .append(dateFormat.format(new Date(periodMillis)));
                    }
                }
                periodCursor.close();
            }

            Cursor symptomCursor = dbHelper.getSymptoms(userEmail);
            if (symptomCursor != null) {
                if (symptomCursor.moveToFirst()) {
                    int symptomIndex = symptomCursor.getColumnIndex(DatabaseHelper.KEY_SYMPTOM);
                    int dateIndex = symptomCursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);
                    if (symptomIndex != -1) {
                        String symptom = symptomCursor.getString(symptomIndex);
                        String createdAt = dateIndex != -1 ? symptomCursor.getString(dateIndex) : "";
                        intro.append("\nLatest symptoms: ").append(createdAt).append(" — ").append(symptom);
                    }
                }
                symptomCursor.close();
            }
        }

        if (introText != null) {
            introText.setText(intro.toString());
        }

        long reminderMillis = preferences.getClinicianReminderMillis(userEmail != null ? userEmail : "");
        if (clinicianDetailText != null) {
            if (reminderMillis > 0L) {
                clinicianDetailText.setText(CLINICIAN_DETAIL + " Reminder saved on "
                        + dateFormat.format(new Date(reminderMillis)) + ".");
            } else {
                clinicianDetailText.setText(CLINICIAN_DETAIL);
            }
        }
    }
}
