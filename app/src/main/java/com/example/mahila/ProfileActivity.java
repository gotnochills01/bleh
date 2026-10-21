package com.example.mahila;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends BaseActivity {

    private TextView txtName, txtEmail, txtAge, txtPeriodDate, txtPrediction;
    private Button btnLastPeriod, btnEdit, btnLogout;
    private AppPreferences appPreferences;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtAge = findViewById(R.id.txtAge);
        txtPeriodDate = findViewById(R.id.txtPeriodDate);
        txtPrediction = findViewById(R.id.txtPrediction);
        btnLastPeriod = findViewById(R.id.btnLastPeriod);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        appPreferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditProfileDialog());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }

        if (txtAge != null) {
            txtAge.setOnClickListener(v -> showEditProfileDialog());
        }

        btnLastPeriod.setOnClickListener(v -> {
            String currentEmail = appPreferences.getCurrentUserEmail();
            if (currentEmail.isEmpty()) {
                Toast.makeText(this, getString(R.string.guest_mode_required_period), Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(
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

                        dbHelper.savePeriod(currentEmail, selected.getTimeInMillis());
                        loadProfileData();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void performLogout() {
        appPreferences.setLoggedIn(false);
        appPreferences.clearCurrentUserEmail();
        Toast.makeText(this, getString(R.string.logged_out_successfully), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showEditProfileDialog() {
        String currentEmail = appPreferences.getCurrentUserEmail();
        if (currentEmail.isEmpty()) {
            Toast.makeText(this, "Guest Mode: Please log in to edit your profile.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentName = "";
        int currentAge = 0;

        Cursor cursor = dbHelper.getUser(currentEmail);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                int ageIndex = cursor.getColumnIndex(DatabaseHelper.KEY_AGE);
                if (nameIndex != -1) currentName = cursor.getString(nameIndex);
                if (ageIndex != -1) currentAge = cursor.getInt(ageIndex);
            }
            cursor.close();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        inputName.setText(currentName);
        layout.addView(inputName);

        final EditText inputAge = new EditText(this);
        inputAge.setHint("Age");
        inputAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (currentAge > 0) {
            inputAge.setText(String.valueOf(currentAge));
        }
        layout.addView(inputAge);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = inputName.getText().toString().trim();
            String newAgeStr = inputAge.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int newAge = 0;
            if (!newAgeStr.isEmpty()) {
                try {
                    newAge = Integer.parseInt(newAgeStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid age number", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean updated = dbHelper.updateUser(currentEmail, newName, newAge);
            if (updated) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                loadProfileData();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadProfileData() {
        String currentEmail = appPreferences.getCurrentUserEmail();
        if (currentEmail.isEmpty()) {
            txtName.setText("Guest User");
            txtEmail.setText("Guest");
            if (txtAge != null) {
                txtAge.setText("Age: N/A (Guest Mode)");
            }
            txtPeriodDate.setText("Last Period: Not logged in");
            txtPrediction.setText("Next Period: Not Available");
            return;
        }

        // Load name, email, and age from DatabaseHelper
        Cursor userCursor = dbHelper.getUser(currentEmail);
        if (userCursor != null) {
            if (userCursor.moveToFirst()) {
                int nameIndex = userCursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                int emailIndex = userCursor.getColumnIndex(DatabaseHelper.KEY_EMAIL);
                int ageIndex = userCursor.getColumnIndex(DatabaseHelper.KEY_AGE);

                String name = (nameIndex != -1) ? userCursor.getString(nameIndex) : "";
                String email = (emailIndex != -1) ? userCursor.getString(emailIndex) : currentEmail;
                int age = (ageIndex != -1) ? userCursor.getInt(ageIndex) : 0;

                txtName.setText(name.isEmpty() ? "User" : name);
                txtEmail.setText(email);
                if (txtAge != null) {
                    txtAge.setText(age > 0 ? "Age: " + age : "Age: Not set");
                }
            } else {
                txtName.setText("User");
                txtEmail.setText(currentEmail);
                if (txtAge != null) {
                    txtAge.setText("Age: Not set");
                }
            }
            userCursor.close();
        } else {
            txtName.setText("User");
            txtEmail.setText(currentEmail);
            if (txtAge != null) {
                txtAge.setText("Age: Not set");
            }
        }

        // Load latest period record for current user from DatabaseHelper
        Cursor periodCursor = dbHelper.getPeriod(currentEmail);
        long lastPeriodMillis = -1L;
        if (periodCursor != null) {
            if (periodCursor.moveToFirst()) {
                int dateIndex = periodCursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                if (dateIndex != -1) {
                    lastPeriodMillis = periodCursor.getLong(dateIndex);
                }
            }
            periodCursor.close();
        }

        if (lastPeriodMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date lastPeriodDate = new Date(lastPeriodMillis);

            Calendar lastPeriodCal = startOfDay(lastPeriodMillis);
            Calendar todayCal = startOfToday();

            long diffMillis = todayCal.getTimeInMillis() - lastPeriodCal.getTimeInMillis();
            long daysBetween = TimeUnit.MILLISECONDS.toDays(diffMillis);
            int cycleDay = (int) daysBetween + 1;

            Calendar nextPeriodCal = (Calendar) lastPeriodCal.clone();
            nextPeriodCal.add(Calendar.DAY_OF_MONTH, 28);

            txtPeriodDate.setText("Last Period: " + sdf.format(lastPeriodDate) + " (Cycle Day " + cycleDay + ")");
            txtPrediction.setText("Next Period: " + sdf.format(nextPeriodCal.getTime()));
        } else {
            txtPeriodDate.setText("Last Period: No date selected");
            txtPrediction.setText("Next Period: Not Available");
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
}