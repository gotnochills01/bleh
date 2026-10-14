package com.example.mahila;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mahila.api.TokenManager;
import com.example.mahila.notifications.ReminderScheduler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RemindersActivity extends AppCompatActivity {

    private static final int REQ_NOTIF_PERMISSION = 101;

    private MaterialSwitch switchPeriodReminders;
    private MaterialSwitch switchOvulationReminders;
    private TextInputEditText etMedName;
    private LinearLayout containerMedsReminders;
    private TextView tvEmptyReminders;

    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminders);

        View root = findViewById(R.id.reminders_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.reminders_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Reminders & Notifications");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tokenManager = new TokenManager(this);
        dbHelper = new DatabaseHelper(this);

        switchPeriodReminders = findViewById(R.id.switch_period_reminders);
        switchOvulationReminders = findViewById(R.id.switch_ovulation_reminders);
        etMedName = findViewById(R.id.et_med_name);
        containerMedsReminders = findViewById(R.id.container_meds_reminders);
        tvEmptyReminders = findViewById(R.id.tv_empty_reminders);

        MaterialButton btnAddMed = findViewById(R.id.btn_add_med_reminder);
        if (btnAddMed != null) {
            btnAddMed.setOnClickListener(v -> addMedicationReminder());
        }

        if (switchPeriodReminders != null) {
            switchPeriodReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkNotificationPermission();
                String email = tokenManager.getUserEmail();
                if (isChecked) {
                    boolean scheduled = ReminderScheduler.schedulePeriodReminder(RemindersActivity.this, email, 2);
                    if (scheduled) {
                        Toast.makeText(RemindersActivity.this, "Period reminder scheduled for estimated cycle start", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RemindersActivity.this, "Insufficient period history to schedule estimates. Log your period first.", Toast.LENGTH_LONG).show();
                        switchPeriodReminders.setChecked(false);
                    }
                } else {
                    int notifId = (email != null ? email.hashCode() & 0x7FFFFFFF : 100) % 10000 + 100;
                    ReminderScheduler.cancelNotification(RemindersActivity.this, notifId);
                    Toast.makeText(RemindersActivity.this, "Period reminder canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchOvulationReminders != null) {
            switchOvulationReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkNotificationPermission();
                String email = tokenManager.getUserEmail();
                if (isChecked) {
                    boolean scheduled = ReminderScheduler.scheduleOvulationReminder(RemindersActivity.this, email);
                    if (scheduled) {
                        Toast.makeText(RemindersActivity.this, "Ovulation window reminder scheduled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RemindersActivity.this, "Insufficient cycle data for ovulation estimates.", Toast.LENGTH_LONG).show();
                        switchOvulationReminders.setChecked(false);
                    }
                } else {
                    int notifId = (email != null ? email.hashCode() & 0x7FFFFFFF : 100) % 10000 + 200;
                    ReminderScheduler.cancelNotification(RemindersActivity.this, notifId);
                    Toast.makeText(RemindersActivity.this, "Ovulation reminder canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        checkNotificationPermission();
        loadRemindersHistory();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission required for status-bar reminders", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addMedicationReminder() {
        checkNotificationPermission();
        String medName = etMedName != null ? etMedName.getText().toString().trim() : "";
        if (medName.isEmpty()) {
            Toast.makeText(this, "Please enter medication or reminder title", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Please log in to set reminders", Toast.LENGTH_SHORT).show();
            return;
        }

        long reminderTime = System.currentTimeMillis() + (24L * 60 * 60 * 1000); // 24 hrs from now
        dbHelper.saveReminder(email, medName, "MEDICATION", reminderTime, true);

        int notifId = (medName + email).hashCode() & 0x7FFFFFFF;
        ReminderScheduler.scheduleNotification(
                this,
                notifId,
                "Medication Reminder: " + medName,
                "Time to take your scheduled " + medName + ". Take as directed by your healthcare provider.",
                com.example.mahila.notifications.NotificationReceiver.CHANNEL_MEDS,
                reminderTime
        );

        Toast.makeText(this, "Medication reminder scheduled for tomorrow", Toast.LENGTH_SHORT).show();
        if (etMedName != null) etMedName.setText("");
        loadRemindersHistory();
    }

    private void loadRemindersHistory() {
        if (containerMedsReminders == null) return;
        containerMedsReminders.removeAllViews();

        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            if (tvEmptyReminders != null) tvEmptyReminders.setVisibility(View.VISIBLE);
            return;
        }

        try (Cursor cursor = dbHelper.getReminders(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                if (tvEmptyReminders != null) tvEmptyReminders.setVisibility(View.GONE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

                int idCol = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
                int titleCol = cursor.getColumnIndex(DatabaseHelper.KEY_REMINDER_TITLE);
                int timeCol = cursor.getColumnIndex(DatabaseHelper.KEY_REMINDER_TIME);

                do {
                    final long remId = idCol >= 0 ? cursor.getLong(idCol) : -1;
                    String title = titleCol >= 0 && !cursor.isNull(titleCol) ? cursor.getString(titleCol) : "Reminder";
                    long time = timeCol >= 0 ? cursor.getLong(timeCol) : 0;

                    CardView card = new CardView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 16);
                    card.setLayoutParams(params);
                    card.setRadius(16f);
                    card.setCardElevation(2f);
                    card.setContentPadding(20, 20, 20, 20);

                    LinearLayout itemLayout = new LinearLayout(this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView tvTitle = new TextView(this);
                    tvTitle.setText("⏰  " + title);
                    tvTitle.setTextSize(15f);
                    tvTitle.setTextColor(0xFF3B1F2B);

                    TextView tvTime = new TextView(this);
                    tvTime.setText("Scheduled for: " + sdf.format(new Date(time)));
                    tvTime.setTextSize(13f);
                    tvTime.setTextColor(0xFF6E5A63);

                    MaterialButton btnDelete = new MaterialButton(this);
                    btnDelete.setText("Cancel Reminder");
                    btnDelete.setTextSize(12f);
                    btnDelete.setCornerRadius(10);
                    btnDelete.setOnClickListener(v -> {
                        if (remId > 0) {
                            dbHelper.deleteReminder(remId, email);
                            int notifId = (title + email).hashCode() & 0x7FFFFFFF;
                            ReminderScheduler.cancelNotification(RemindersActivity.this, notifId);
                            Toast.makeText(RemindersActivity.this, "Reminder canceled", Toast.LENGTH_SHORT).show();
                            loadRemindersHistory();
                        }
                    });

                    itemLayout.addView(tvTitle);
                    itemLayout.addView(tvTime);
                    itemLayout.addView(btnDelete);
                    card.addView(itemLayout);

                    containerMedsReminders.addView(card);
                } while (cursor.moveToNext());
            } else {
                if (tvEmptyReminders != null) tvEmptyReminders.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            if (tvEmptyReminders != null) tvEmptyReminders.setVisibility(View.VISIBLE);
        }
    }
}
