package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View root = findViewById(R.id.home_root);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.home_toolbar);
        toolbar.setTitle(R.string.app_name);

        bindCard(R.id.card_period_tracker, PeriodTrackerActivity.class);
        bindCard(R.id.card_symptom_logger, SymptomLoggerActivity.class);
        bindCard(R.id.card_medical_assistance, MedicalAssistanceActivity.class);
        bindCard(R.id.card_emergency_contacts, EmergencyContactsActivity.class);
        bindCard(R.id.card_health_tips, HealthTipsActivity.class);

        // Profile Card
        bindCard(R.id.card_profile, ProfileActivity.class);

        // New Feature Cards
        bindCard(R.id.card_ai_chat, AiChatActivity.class);
        bindCard(R.id.card_quiz, QuizActivity.class);
        bindCard(R.id.card_settings, SettingsActivity.class);
    }

    private void bindCard(int cardId, Class<?> destination) {

        MaterialCardView card = findViewById(cardId);

        if (card == null) {
            return;
        }

        card.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, destination))
        );
    }
}