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
import com.example.mahila.R;
import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.CycleCalculator;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private TextView tvUserName;
    private TextView tvCycleDay;
    private TextView tvCyclePhase;
    private TextView tvNextPeriod;
    private TextView tvFertileWindow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting    = root.findViewById(R.id.tv_greeting);
        tvUserName    = root.findViewById(R.id.tv_user_name);
        tvCycleDay    = root.findViewById(R.id.tv_cycle_day);
        tvCyclePhase  = root.findViewById(R.id.tv_cycle_phase);
        tvNextPeriod  = root.findViewById(R.id.tv_next_period);
        tvFertileWindow = root.findViewById(R.id.tv_fertile_window);

        // Greeting by time-of-day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning," : hour < 17 ? "Good Afternoon," : "Good Evening,";
        if (tvGreeting != null) tvGreeting.setText(greeting);

        // User name from session / SQLite
        TokenManager tokenManager = new TokenManager(requireContext());
        String email = tokenManager.getUserEmail();
        String name = tokenManager.getUserName();

        long lastPeriodMillis = -1;
        if (email != null && !email.isEmpty()) {
            try (DatabaseHelper db = new DatabaseHelper(requireContext())) {
                // Fetch profile name fallback if session name is generic
                if (name == null || name.isEmpty() || name.equalsIgnoreCase("Friend") || name.equalsIgnoreCase("User")) {
                    Cursor uCursor = db.getUser(email);
                    if (uCursor != null && uCursor.moveToFirst()) {
                        int nameCol = uCursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                        if (nameCol >= 0 && !uCursor.isNull(nameCol)) {
                            name = uCursor.getString(nameCol);
                        }
                        uCursor.close();
                    }
                }

                // Fetch most recent period record
                Cursor cursor = db.getPeriod(email);
                if (cursor != null && cursor.moveToFirst()) {
                    int col = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                    if (col >= 0 && !cursor.isNull(col)) {
                        long ts = cursor.getLong(col);
                        if (ts > 0) lastPeriodMillis = ts;
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                // Fall through to default
            }
        }

        if (name == null || name.trim().isEmpty()) name = "Friend";
        tvUserName.setText(name);

        // Compute cycle info safely
        if (lastPeriodMillis <= 0) {
            lastPeriodMillis = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
        }
        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfo(lastPeriodMillis, 28);

        tvCycleDay.setText("Day " + info.cycleDay + " of Cycle");
        tvCyclePhase.setText(info.currentPhase);
        tvNextPeriod.setText(info.nextPeriodDate);
        tvFertileWindow.setText(info.ovulationWindow);

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

        return root;
    }
}
