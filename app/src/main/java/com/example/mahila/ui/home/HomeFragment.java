package com.example.mahila.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.AiChatActivity;
import com.example.mahila.DatabaseHelper;
import com.example.mahila.NutritionActivity;
import com.example.mahila.R;
import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.CycleCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private TextView tvUserName;
    private TextView tvCycleDay;
    private TextView tvCyclePhase;
    private TextView tvNextPeriod;
    private TextView tvFertileWindow;
    private TextView tvAiInsightText;
    private Chip chipPhaseBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting      = root.findViewById(R.id.tv_greeting);
        tvUserName      = root.findViewById(R.id.tv_user_name);
        tvCycleDay      = root.findViewById(R.id.tv_cycle_day);
        tvCyclePhase    = root.findViewById(R.id.tv_cycle_phase);
        tvNextPeriod    = root.findViewById(R.id.tv_next_period);
        tvFertileWindow = root.findViewById(R.id.tv_fertile_window);
        tvAiInsightText = root.findViewById(R.id.tv_ai_insight_text);
        chipPhaseBadge  = root.findViewById(R.id.chip_phase_badge);

        // Time-of-day greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning," : hour < 17 ? "Good Afternoon," : "Good Evening,";
        if (tvGreeting != null) tvGreeting.setText(greeting);

        // Retrieve user session & profile details
        TokenManager tokenManager = new TokenManager(requireContext());
        String email = tokenManager.getUserEmail();
        String name = tokenManager.getUserName();

        List<Long> periodHistory = new ArrayList<>();
        String diagnosedConditions = null;

        if (email != null && !email.isEmpty()) {
            try (DatabaseHelper db = new DatabaseHelper(requireContext())) {
                Cursor uCursor = db.getUser(email);
                if (uCursor != null && uCursor.moveToFirst()) {
                    if (name == null || name.isEmpty() || name.equalsIgnoreCase("Friend") || name.equalsIgnoreCase("User")) {
                        int nameCol = uCursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                        if (nameCol >= 0 && !uCursor.isNull(nameCol)) {
                            name = uCursor.getString(nameCol);
                        }
                    }
                    int condCol = uCursor.getColumnIndex(DatabaseHelper.KEY_DIAGNOSED_CONDITIONS);
                    if (condCol >= 0 && !uCursor.isNull(condCol)) {
                        diagnosedConditions = uCursor.getString(condCol);
                    }
                    uCursor.close();
                }

                Cursor cursor = db.getPeriod(email);
                if (cursor != null && cursor.moveToFirst()) {
                    int col = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                    do {
                        if (col >= 0 && !cursor.isNull(col)) {
                            long ts = cursor.getLong(col);
                            if (ts > 0) periodHistory.add(ts);
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } catch (Exception ignored) {}
        }

        if (name == null || name.trim().isEmpty()) name = "Friend";
        tvUserName.setText(name);

        // Cycle calculations
        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(periodHistory, 28);

        if (periodHistory.isEmpty()) {
            tvCycleDay.setText("Day --");
            tvCyclePhase.setText("No Period History Logged");
            tvNextPeriod.setText("Not Available");
            tvFertileWindow.setText("Not Available");
            if (chipPhaseBadge != null) {
                chipPhaseBadge.setText("Log First Period");
            }
            if (tvAiInsightText != null) {
                tvAiInsightText.setText("No period logged yet. Click 'Log Period' below to record your cycle and enable intelligent predictions.");
            }
        } else {
            tvCycleDay.setText("Day " + info.cycleDay);
            tvCyclePhase.setText(info.currentPhase);
            tvNextPeriod.setText(info.nextPeriodDate);
            tvFertileWindow.setText(info.ovulationWindow);

            if (chipPhaseBadge != null) {
                chipPhaseBadge.setText(info.isIrregular ? info.currentPhase + " (Irregular)" : info.currentPhase);
            }

            if (tvAiInsightText != null) {
                StringBuilder insight = new StringBuilder(getCycleInsightText(info.currentPhase));
                if (diagnosedConditions != null && !diagnosedConditions.trim().isEmpty()) {
                    insight.append("\n\n• Profile Health Condition: ").append(diagnosedConditions).append(" noted. Consult your doctor for tailored care.");
                }
                tvAiInsightText.setText(insight.toString());
            }
        }

        // Quick action buttons
        MaterialButton btnLogPeriod = root.findViewById(R.id.btn_quick_log_period);
        if (btnLogPeriod != null) {
            btnLogPeriod.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), com.example.mahila.PeriodTrackerActivity.class)));
        }

        MaterialButton btnAiChat = root.findViewById(R.id.btn_quick_ai_chat);
        if (btnAiChat != null) {
            btnAiChat.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AiChatActivity.class)));
        }

        // Nutrition Card click listener
        View cardNutrition = root.findViewById(R.id.card_nutrition_banner);
        if (cardNutrition != null) {
            cardNutrition.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), NutritionActivity.class)));
        }

        MaterialButton btnOpenNutrition = root.findViewById(R.id.btn_open_nutrition);
        if (btnOpenNutrition != null) {
            btnOpenNutrition.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), NutritionActivity.class)));
        }

        return root;
    }

    private String getCycleInsightText(String phase) {
        if (phase == null) return "Track your symptoms daily to receive personalized wellness guidance.";
        if (phase.toLowerCase().contains("menstrual")) {
            return "During your menstrual phase, prioritize gentle rest, warm hydration, and iron-rich foods like leafy greens.";
        } else if (phase.toLowerCase().contains("follicular")) {
            return "Energy levels rise in the follicular phase! Ideal time for moderate exercise and fresh complex carbohydrates.";
        } else if (phase.toLowerCase().contains("ovulat")) {
            return "Peak fertility phase. Stay well hydrated, incorporate healthy fats, and keep track of basal temperature changes.";
        } else if (phase.toLowerCase().contains("luteal")) {
            return "In the luteal phase, focus on stress management, magnesium-rich foods, and consistent sleep hygiene.";
        }
        return "Track your cycle consistently to get tailored metabolic and wellness insights.";
    }
}
