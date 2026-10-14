package com.example.mahila.ui.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.R;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import com.google.android.material.slider.Slider;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackerFragment extends Fragment {

    private EditText etPeriodDate;
    private Spinner spFlowLevel;
    private Slider sliderPainScale;
    private TextView tvPainLabel;
    private Spinner spMoodType;
    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracker, container, false);

        tokenManager = new TokenManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        etPeriodDate   = root.findViewById(R.id.et_period_date);
        spFlowLevel    = root.findViewById(R.id.sp_flow_level);
        sliderPainScale = root.findViewById(R.id.sb_pain_scale);
        tvPainLabel    = root.findViewById(R.id.tv_pain_label);
        spMoodType     = root.findViewById(R.id.sp_mood_type);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etPeriodDate.setText(today);

        String[] flows = {"LIGHT", "MEDIUM", "HEAVY", "SPOTTING"};
        spFlowLevel.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, flows));

        String[] moods = {"HAPPY", "SAD", "ANGRY", "ANXIOUS", "STRESSED", "CALM"};
        spMoodType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, moods));

        sliderPainScale.addOnChangeListener((slider, value, fromUser) ->
                tvPainLabel.setText("Pain Scale: " + (int) value + " / 10"));

        root.findViewById(R.id.btn_save_period).setOnClickListener(v -> savePeriod());
        root.findViewById(R.id.btn_save_mood).setOnClickListener(v -> saveMood());

        return root;
    }

    private void savePeriod() {
        String dateStr = etPeriodDate.getText().toString().trim();
        String flow = spFlowLevel.getSelectedItem().toString();
        int pain = (int) sliderPainScale.getValue();
        String email = tokenManager.getUserEmail();

        long periodMillis = System.currentTimeMillis();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = sdf.parse(dateStr);
            if (d != null) periodMillis = d.getTime();
        } catch (Exception ignored) {}

        if (email != null && !email.isEmpty()) {
            dbHelper.savePeriod(email, periodMillis, flow, pain, false);
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("startDate", dateStr);
            obj.put("flowLevel", flow);
            obj.put("painScale", pain);

            ApiClient.postRequest(requireContext(), ApiService.PERIODS, obj, new ApiClient.ApiCallback<String>() {
                @Override public void onSuccess(String result) {
                    if (email != null && !email.isEmpty()) {
                        dbHelper.markPeriodsSynced(email);
                    }
                    Toast.makeText(requireContext(), "Period entry saved and synced!", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String message) {
                    Toast.makeText(requireContext(), "Offline Mode: Entry logged locally", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Offline Mode: Entry logged locally", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMood() {
        String mood = spMoodType.getSelectedItem().toString();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String email = tokenManager.getUserEmail();

        if (email != null && !email.isEmpty()) {
            dbHelper.saveMood(email, mood, 3, today, false);
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("moodType", mood);
            obj.put("intensity", 3);
            obj.put("loggedDate", today);

            ApiClient.postRequest(requireContext(), ApiService.MOODS, obj, new ApiClient.ApiCallback<String>() {
                @Override public void onSuccess(String result) {
                    if (email != null && !email.isEmpty()) {
                        dbHelper.markMoodsSynced(email);
                    }
                    Toast.makeText(requireContext(), "Mood recorded and synced!", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String message) {
                    Toast.makeText(requireContext(), "Offline Mode: Mood recorded locally", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Offline Mode: Mood recorded locally", Toast.LENGTH_SHORT).show();
        }
    }
}
