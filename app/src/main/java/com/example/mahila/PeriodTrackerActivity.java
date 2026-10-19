package com.example.mahila;

import android.app.DatePickerDialog;
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

import com.example.mahila.notifications.ReminderScheduler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PeriodTrackerActivity extends AppCompatActivity {

    private AppPreferences preferences;
    private DatabaseHelper dbHelper;
    private TextView cycleDayText;
    private TextView nextPeriodText;
    private TextView lastPeriodText;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_period_tracker);

        View root = findViewById(R.id.period_tracker_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.period_tracker_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Period Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        preferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);
        cycleDayText = findViewById(R.id.text_cycle_day);
        nextPeriodText = findViewById(R.id.text_next_period);
        lastPeriodText = findViewById(R.id.text_last_period);

        MaterialButton logButton = findViewById(R.id.button_log_period);
        logButton.setOnClickListener(v -> showDatePicker());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCycleInfo();
    }

    private long getLastPeriodMillisForUser() {
        String email = preferences.getCurrentUserEmail();
        if (email.isEmpty()) {
            return -1L;
        }
        Cursor cursor = dbHelper.getPeriod(email);
        long lastPeriodMillis = -1L;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                if (columnIndex != -1) {
                    lastPeriodMillis = cursor.getLong(columnIndex);
                }
            }
            cursor.close();
        }
        return lastPeriodMillis;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        long lastPeriodMillis = getLastPeriodMillisForUser();
        if (lastPeriodMillis > 0L) {
            calendar.setTimeInMillis(lastPeriodMillis);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    Calendar today = startOfToday();
                    if (selected.after(today)) {
                        Toast.makeText(this, "Please choose today or an earlier date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Calendar oldestAllowed = (Calendar) today.clone();
                    oldestAllowed.add(Calendar.YEAR, -2);
                    if (selected.before(oldestAllowed)) {
                        Toast.makeText(this, "Please choose a date within the last 2 years.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userEmail = preferences.getCurrentUserEmail();
                    if (userEmail.isEmpty()) {
                        Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean saved = dbHelper.savePeriod(userEmail, selected.getTimeInMillis());
                    if (saved) {
                        ReminderScheduler.schedulePeriodReminder(this, userEmail, 2);
                        ReminderScheduler.scheduleOvulationReminder(this, userEmail);
                        showCycleInfo();
                        Toast.makeText(this, "Last period date saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save period date", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showCycleInfo() {
        long lastPeriodMillis = getLastPeriodMillisForUser();
        if (lastPeriodMillis <= 0L) {
            cycleDayText.setText("No period logged yet");
            nextPeriodText.setText("Log your last period start to see your next expected date.");
            lastPeriodText.setText("Not saved yet");
            return;
        }

        Calendar lastPeriod = startOfDay(lastPeriodMillis);
        Calendar today = startOfToday();
        Calendar nextPeriod = startOfDay(lastPeriodMillis);
        nextPeriod.add(Calendar.DAY_OF_YEAR, AppPreferences.CYCLE_LENGTH_DAYS);

        long daysSinceStart = daysBetween(lastPeriod, today);
        int cycleDay = (int) daysSinceStart + 1;
        long daysUntilNext = daysBetween(today, nextPeriod);

        cycleDayText.setText("Day " + cycleDay + " of " + AppPreferences.CYCLE_LENGTH_DAYS);
        lastPeriodText.setText("Started on " + dateFormat.format(new Date(lastPeriodMillis)));

        if (daysUntilNext > 1) {
            nextPeriodText.setText("Next period expected on " + dateFormat.format(nextPeriod.getTime())
                    + " (" + daysUntilNext + " days).");
        } else if (daysUntilNext == 1) {
            nextPeriodText.setText("Next period expected tomorrow ("
                    + dateFormat.format(nextPeriod.getTime()) + ").");
        } else if (daysUntilNext == 0) {
            nextPeriodText.setText("Next period expected today.");
        } else {
            long daysLate = Math.abs(daysUntilNext);
            nextPeriodText.setText("Next period was expected on " + dateFormat.format(nextPeriod.getTime())
                    + " (" + daysLate + " day(s) ago). Log a new start date if it has begun.");
        }
    }

    private Calendar startOfToday() {
        return startOfDay(System.currentTimeMillis());
    }

    private Calendar startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private long daysBetween(Calendar start, Calendar end) {
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
}
