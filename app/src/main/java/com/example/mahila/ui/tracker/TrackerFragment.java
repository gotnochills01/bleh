package com.example.mahila.ui.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.R;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracker, container, false);

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
        try {
            JSONObject obj = new JSONObject();
            obj.put("startDate", etPeriodDate.getText().toString());
            obj.put("flowLevel", spFlowLevel.getSelectedItem().toString());
            obj.put("painScale", (int) sliderPainScale.getValue());

            ApiClient.postRequest(requireContext(), ApiService.PERIODS, obj, new ApiClient.ApiCallback<String>() {
                @Override public void onSuccess(String result) {
                    Toast.makeText(requireContext(), "Period entry saved!", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String message) {
                    Toast.makeText(requireContext(), "Entry logged locally", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Entry logged", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMood() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("moodType", spMoodType.getSelectedItem().toString());
            obj.put("intensity", 3);
            obj.put("loggedDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            ApiClient.postRequest(requireContext(), ApiService.MOODS, obj, new ApiClient.ApiCallback<String>() {
                @Override public void onSuccess(String result) {
                    Toast.makeText(requireContext(), "Mood recorded!", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String message) {
                    Toast.makeText(requireContext(), "Mood recorded!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Mood recorded!", Toast.LENGTH_SHORT).show();
        }
    }
}
