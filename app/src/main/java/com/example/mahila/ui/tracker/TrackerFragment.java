package com.example.mahila.ui.tracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.R;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import com.example.mahila.notifications.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
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
    private TextView tvEmptyPeriodHistory;
    private LinearLayout containerPeriodHistory;

    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracker, container, false);

        tokenManager = new TokenManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        etPeriodDate           = root.findViewById(R.id.et_period_date);
        spFlowLevel            = root.findViewById(R.id.sp_flow_level);
        sliderPainScale        = root.findViewById(R.id.sb_pain_scale);
        tvPainLabel            = root.findViewById(R.id.tv_pain_label);
        spMoodType             = root.findViewById(R.id.sp_mood_type);
        tvEmptyPeriodHistory   = root.findViewById(R.id.tv_empty_period_history);
        containerPeriodHistory = root.findViewById(R.id.container_period_history);

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

        loadPeriodHistory();

        return root;
    }

    private void loadPeriodHistory() {
        if (containerPeriodHistory == null) return;
        containerPeriodHistory.removeAllViews();

        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            if (tvEmptyPeriodHistory != null) tvEmptyPeriodHistory.setVisibility(View.VISIBLE);
            return;
        }

        try (Cursor cursor = dbHelper.getPeriod(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                if (tvEmptyPeriodHistory != null) tvEmptyPeriodHistory.setVisibility(View.GONE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                int idCol = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
                int dateCol = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                int flowCol = cursor.getColumnIndex(DatabaseHelper.KEY_FLOW_LEVEL);
                int painCol = cursor.getColumnIndex(DatabaseHelper.KEY_PAIN_SCALE);

                do {
                    final long recordId = idCol >= 0 ? cursor.getLong(idCol) : -1;
                    long ts = dateCol >= 0 ? cursor.getLong(dateCol) : 0;
                    String flow = flowCol >= 0 && !cursor.isNull(flowCol) ? cursor.getString(flowCol) : "MEDIUM";
                    int pain = painCol >= 0 ? cursor.getInt(painCol) : 0;

                    CardView card = new CardView(requireContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 16);
                    card.setLayoutParams(params);
                    card.setRadius(16f);
                    card.setCardElevation(2f);
                    card.setContentPadding(20, 20, 20, 20);

                    RelativeLayout itemLayout = new RelativeLayout(requireContext());

                    TextView tvInfo = new TextView(requireContext());
                    tvInfo.setText("🗓️  " + sdf.format(new Date(ts)) + "\nFlow: " + flow + " | Pain: " + pain + "/10");
                    tvInfo.setTextSize(14f);
                    tvInfo.setTextColor(0xFF3B1F2B);

                    RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    infoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                    tvInfo.setLayoutParams(infoParams);

                    MaterialButton btnDelete = new MaterialButton(requireContext());
                    btnDelete.setText("Delete");
                    btnDelete.setTextSize(12f);
                    btnDelete.setCornerRadius(10);

                    RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                    btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    btnDelete.setLayoutParams(btnParams);

                    btnDelete.setOnClickListener(v -> {
                        if (recordId > 0) {
                            dbHelper.deletePeriod(recordId, email);
                            Toast.makeText(requireContext(), "Period entry deleted", Toast.LENGTH_SHORT).show();
                            ReminderScheduler.schedulePeriodReminder(requireContext(), email, 2);
                            ReminderScheduler.scheduleOvulationReminder(requireContext(), email);
                            loadPeriodHistory();
                        }
                    });

                    itemLayout.addView(tvInfo);
                    itemLayout.addView(btnDelete);
                    card.addView(itemLayout);

                    containerPeriodHistory.addView(card);
                } while (cursor.moveToNext());
            } else {
                if (tvEmptyPeriodHistory != null) tvEmptyPeriodHistory.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            if (tvEmptyPeriodHistory != null) tvEmptyPeriodHistory.setVisibility(View.VISIBLE);
        }
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
            ReminderScheduler.schedulePeriodReminder(requireContext(), email, 2);
            ReminderScheduler.scheduleOvulationReminder(requireContext(), email);
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
                    loadPeriodHistory();
                }
                @Override public void onError(String message) {
                    Toast.makeText(requireContext(), "Offline Mode: Entry logged locally", Toast.LENGTH_SHORT).show();
                    loadPeriodHistory();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Offline Mode: Entry logged locally", Toast.LENGTH_SHORT).show();
            loadPeriodHistory();
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
