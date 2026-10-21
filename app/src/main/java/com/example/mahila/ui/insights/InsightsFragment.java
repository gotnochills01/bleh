package com.example.mahila.ui.insights;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.R;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.AnalyticsEngine;
import com.google.android.material.chip.Chip;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InsightsFragment extends Fragment {

    private Chip chipCompleteness;
    private TextView tvCompletenessExplanation;
    private TextView tvAvgCycleLength;
    private TextView tvAvgPeriodDuration;
    private TextView tvCycleRegularityStatus;
    private TextView tvSymptomSummaryList;
    private TextView tvFoodAssociationList;
    private TextView tvPersonalizedRecommendations;

    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_insights, container, false);

        chipCompleteness = root.findViewById(R.id.chip_completeness_badge);
        tvCompletenessExplanation = root.findViewById(R.id.tv_completeness_explanation);
        tvAvgCycleLength = root.findViewById(R.id.tv_avg_cycle_length);
        tvAvgPeriodDuration = root.findViewById(R.id.tv_avg_period_duration);
        tvCycleRegularityStatus = root.findViewById(R.id.tv_cycle_regularity_status);
        tvSymptomSummaryList = root.findViewById(R.id.tv_symptom_summary_list);
        tvFoodAssociationList = root.findViewById(R.id.tv_food_association_list);
        tvPersonalizedRecommendations = root.findViewById(R.id.tv_personalized_recommendations);

        dbHelper = new DatabaseHelper(requireContext());
        tokenManager = new TokenManager(requireContext());

        loadLocalAnalytics();
        loadRemoteBackendAnalytics();

        return root;
    }

    private void loadLocalAnalytics() {
        String email = tokenManager.getUserEmail();
        if (email == null || email.trim().isEmpty()) email = "guest@mahila.local";

        String healthPreferences = "";
        String diagnosedConditions = "";
        try (Cursor userCursor = dbHelper.getUser(email)) {
            if (userCursor != null && userCursor.moveToFirst()) {
                int prefIdx = userCursor.getColumnIndex(DatabaseHelper.KEY_HEALTH_PREFERENCES);
                int condIdx = userCursor.getColumnIndex(DatabaseHelper.KEY_DIAGNOSED_CONDITIONS);
                if (prefIdx >= 0 && !userCursor.isNull(prefIdx)) {
                    healthPreferences = userCursor.getString(prefIdx);
                }
                if (condIdx >= 0 && !userCursor.isNull(condIdx)) {
                    diagnosedConditions = userCursor.getString(condIdx);
                }
            }
        } catch (Exception ignored) {}

        List<Long> periods = new ArrayList<>();
        List<AnalyticsEngine.SymptomRecord> symptoms = new ArrayList<>();
        List<AnalyticsEngine.MoodRecord> moods = new ArrayList<>();
        List<Integer> painScales = new ArrayList<>();

        // Load Periods
        try (Cursor pCursor = dbHelper.getPeriod(email)) {
            if (pCursor != null && pCursor.moveToFirst()) {
                int colDate = pCursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                int colPain = pCursor.getColumnIndex(DatabaseHelper.KEY_PAIN_SCALE);
                do {
                    if (colDate >= 0 && !pCursor.isNull(colDate)) {
                        long ts = pCursor.getLong(colDate);
                        if (ts > 0) periods.add(ts);
                    }
                    if (colPain >= 0 && !pCursor.isNull(colPain)) {
                        painScales.add(pCursor.getInt(colPain));
                    }
                } while (pCursor.moveToNext());
            }
        } catch (Exception ignored) {}

        // Load Symptoms
        try (Cursor sCursor = dbHelper.getSymptoms(email)) {
            if (sCursor != null && sCursor.moveToFirst()) {
                int colSym = sCursor.getColumnIndex(DatabaseHelper.KEY_SYMPTOM);
                int colSev = sCursor.getColumnIndex(DatabaseHelper.KEY_SEVERITY);
                int colDate = sCursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);
                do {
                    String sym = colSym >= 0 && !sCursor.isNull(colSym) ? sCursor.getString(colSym) : null;
                    String sev = colSev >= 0 && !sCursor.isNull(colSev) ? sCursor.getString(colSev) : "MODERATE";
                    String date = colDate >= 0 && !sCursor.isNull(colDate) ? sCursor.getString(colDate) : "";
                    if (sym != null) {
                        symptoms.add(new AnalyticsEngine.SymptomRecord(sym, sev, date));
                    }
                } while (sCursor.moveToNext());
            }
        } catch (Exception ignored) {}

        // Load Moods
        try (Cursor mCursor = dbHelper.getMoods(email)) {
            if (mCursor != null && mCursor.moveToFirst()) {
                int colType = mCursor.getColumnIndex(DatabaseHelper.KEY_MOOD_TYPE);
                int colInt = mCursor.getColumnIndex(DatabaseHelper.KEY_INTENSITY);
                int colDate = mCursor.getColumnIndex(DatabaseHelper.KEY_LOGGED_DATE);
                do {
                    String type = colType >= 0 && !mCursor.isNull(colType) ? mCursor.getString(colType) : null;
                    int intensity = colInt >= 0 && !mCursor.isNull(colInt) ? mCursor.getInt(colInt) : 3;
                    String date = colDate >= 0 && !mCursor.isNull(colDate) ? mCursor.getString(colDate) : "";
                    if (type != null) {
                        moods.add(new AnalyticsEngine.MoodRecord(type, intensity, date));
                    }
                } while (mCursor.moveToNext());
            }
        } catch (Exception ignored) {}

        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(periods, symptoms, moods, painScales,
                healthPreferences, diagnosedConditions);
        displayAnalyticsResult(result);
    }

    private void displayAnalyticsResult(AnalyticsEngine.AnalyticsResult result) {
        if (chipCompleteness != null) {
            chipCompleteness.setText(result.completenessStatus + " DATA");
        }
        if (tvCompletenessExplanation != null) {
            tvCompletenessExplanation.setText(result.completenessExplanation);
        }

        if (tvAvgCycleLength != null) {
            tvAvgCycleLength.setText(String.format("%.1f Days", result.averageCycleLength));
        }
        if (tvAvgPeriodDuration != null) {
            tvAvgPeriodDuration.setText(String.format("%.1f Days", result.averagePeriodDuration));
        }

        if (tvCycleRegularityStatus != null) {
            if (result.isIrregular) {
                tvCycleRegularityStatus.setText("Cycle Pattern: Irregular (High Variance >4 Days). Predictions are approximate.");
            } else {
                tvCycleRegularityStatus.setText("Cycle Pattern: Regular (Within expected ±2 day range).");
            }
        }

        if (tvSymptomSummaryList != null) {
            if (result.neutralSymptomSummaries.isEmpty()) {
                tvSymptomSummaryList.setText("No symptoms logged yet. Use the Symptom Logger to track patterns across your cycle.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (String line : result.neutralSymptomSummaries) {
                    sb.append("• ").append(line).append("\n\n");
                }
                tvSymptomSummaryList.setText(sb.toString().trim());
            }
        }

        if (tvFoodAssociationList != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : result.foodAssociationObservations) {
                sb.append("• ").append(line).append("\n\n");
            }
            tvFoodAssociationList.setText(sb.toString().trim());
        }

        if (tvPersonalizedRecommendations != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : result.personalizedRecommendations) {
                sb.append("• ").append(line).append("\n\n");
            }
            if (sb.length() == 0) {
                tvPersonalizedRecommendations.setText("No personalized suggestions available yet. Track your cycle and symptoms to unlock them.");
            } else {
                tvPersonalizedRecommendations.setText(sb.toString().trim());
            }
        }
    }

    private void loadRemoteBackendAnalytics() {
        String token = tokenManager.getToken();
        if (token == null || token.trim().isEmpty()) return;

        ApiClient.getRequest(requireContext(), ApiService.ANALYTICS, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    double avgCycle = obj.optDouble("averageCycleLength", 28.0);
                    double avgDuration = obj.optDouble("averagePeriodDuration", 5.0);
                    String regularity = obj.optString("cycleRegularity", "Regular");
                    String completeness = obj.optString("dataCompletenessStatus", "MODERATE");

                    if (chipCompleteness != null) {
                        chipCompleteness.setText(completeness + " DATA");
                    }
                    if (tvAvgCycleLength != null) {
                        tvAvgCycleLength.setText(String.format("%.1f Days", avgCycle));
                    }
                    if (tvAvgPeriodDuration != null) {
                        tvAvgPeriodDuration.setText(String.format("%.1f Days", avgDuration));
                    }
                    if (tvCycleRegularityStatus != null) {
                        tvCycleRegularityStatus.setText("Cycle Pattern (Synced): " + regularity);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onError(String message) {
                // Offline fallback maintained silently
            }
        });
    }
}
