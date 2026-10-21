package com.example.mahila;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicalAssistanceActivity extends BaseActivity {

    private static final String INTRO_TEXT =
            "Find care resources. This is not a substitute for professional medical advice.";
    private static final String CLINICIAN_DETAIL =
            "Book a consultation or visit a nearby clinic for personalized care.";

    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final long MAX_STALE_LOCATION_MS = 120000L;

    private AppPreferences preferences;
    private DatabaseHelper dbHelper;
    private TextView introText;
    private TextView clinicianDetailText;
    private TextView locationStatusText;
    private MaterialButton useLocationButton;
    private FusedLocationProviderClient fusedLocationClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_assistance);

        View root = findViewById(R.id.medical_assistance_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.medical_assistance_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.medical_assistance_title);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        preferences = new AppPreferences(this);
        dbHelper = new DatabaseHelper(this);
        introText = findViewById(R.id.text_medical_intro);
        clinicianDetailText = findViewById(R.id.text_clinician_detail);
        locationStatusText = findViewById(R.id.text_location_status);
        useLocationButton = findViewById(R.id.button_use_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (useLocationButton != null) {
            useLocationButton.setOnClickListener(v -> requestCurrentLocation());
        }

        MaterialCardView clinicianCard = findViewById(R.id.card_talk_to_clinician);
        if (clinicianCard != null) {
            clinicianCard.setOnClickListener(v -> {
                saveClinicianReminder();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_TAB", "ASSISTANCE");
                startActivity(intent);
            });
        }

        MaterialCardView urgentCard = findViewById(R.id.card_urgent_care);
        if (urgentCard != null) {
            urgentCard.setOnClickListener(v -> {
                startActivity(new Intent(this, EmergencyContactsActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSavedInfo();
        if (locationStatusText != null) {
            CharSequence currentText = locationStatusText.getText();
            if (currentText == null || currentText.toString().trim().isEmpty()) {
                locationStatusText.setText(R.string.location_waiting_for_permission);
            }
        }
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
            if (locationStatusText != null) {
                locationStatusText.setText(R.string.location_permission_required);
            }
            return;
        }

        if (!isLocationProviderEnabled()) {
            if (locationStatusText != null) {
                locationStatusText.setText(R.string.location_gps_disabled);
            }
            Toast.makeText(this, getString(R.string.location_gps_disabled_toast), Toast.LENGTH_LONG).show();
            return;
        }

        if (locationStatusText != null) {
            locationStatusText.setText(R.string.location_loading);
        }
        if (useLocationButton != null) {
            useLocationButton.setEnabled(false);
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (useLocationButton != null) {
                        useLocationButton.setEnabled(true);
                    }
                    if (location == null) {
                        if (locationStatusText != null) {
                            locationStatusText.setText(R.string.location_unavailable);
                        }
                        Toast.makeText(this, getString(R.string.location_unavailable_toast), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLocationStatus(location);
                })
                .addOnFailureListener(this, e -> {
                    if (useLocationButton != null) {
                        useLocationButton.setEnabled(true);
                    }
                    if (locationStatusText != null) {
                        locationStatusText.setText(getString(R.string.location_request_failed, e.getMessage() == null ? "" : e.getMessage()));
                    }
                    Toast.makeText(this, getString(R.string.location_request_failed_toast), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isLocationProviderEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationStatus(Location location) {
        if (locationStatusText == null) {
            return;
        }

        boolean fresh = (System.currentTimeMillis() - location.getTime()) <= MAX_STALE_LOCATION_MS;
        String freshness = fresh
                ? getString(R.string.location_fresh)
                : getString(R.string.location_stale);
        String message = getString(R.string.location_status_format,
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()),
                String.valueOf(location.hasAccuracy() ? location.getAccuracy() : 0f),
                freshness);
        locationStatusText.setText(message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCurrentLocation();
            } else {
                if (locationStatusText != null) {
                    locationStatusText.setText(R.string.location_permission_denied);
                }
                Toast.makeText(this, getString(R.string.location_permission_denied_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveClinicianReminder() {
        String userEmail = preferences.getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, getString(R.string.opening_medical_map_search), Toast.LENGTH_SHORT).show();
            return;
        }
        preferences.saveClinicianReminder(userEmail, System.currentTimeMillis());
        showSavedInfo();
        Toast.makeText(this, getString(R.string.opening_nearby_healthcare), Toast.LENGTH_SHORT).show();
    }

    private void showSavedInfo() {
        String userEmail = preferences.getCurrentUserEmail();
        StringBuilder intro = new StringBuilder(INTRO_TEXT);

        if (userEmail != null && !userEmail.isEmpty()) {
            Cursor periodCursor = dbHelper.getPeriod(userEmail);
            if (periodCursor != null) {
                if (periodCursor.moveToFirst()) {
                    int dateIndex = periodCursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                    if (dateIndex != -1) {
                        long periodMillis = periodCursor.getLong(dateIndex);
                        intro.append("\n\nLast period start: ")
                                .append(dateFormat.format(new Date(periodMillis)));
                    }
                }
                periodCursor.close();
            }

            Cursor symptomCursor = dbHelper.getSymptoms(userEmail);
            if (symptomCursor != null) {
                if (symptomCursor.moveToFirst()) {
                    int symptomIndex = symptomCursor.getColumnIndex(DatabaseHelper.KEY_SYMPTOM);
                    int dateIndex = symptomCursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);
                    if (symptomIndex != -1) {
                        String symptom = symptomCursor.getString(symptomIndex);
                        String createdAt = dateIndex != -1 ? symptomCursor.getString(dateIndex) : "";
                        intro.append("\nLatest symptoms: ").append(createdAt).append(" — ").append(symptom);
                    }
                }
                symptomCursor.close();
            }
        }

        if (introText != null) {
            introText.setText(intro.toString());
        }

        long reminderMillis = preferences.getClinicianReminderMillis(userEmail != null ? userEmail : "");
        if (clinicianDetailText != null) {
            if (reminderMillis > 0L) {
                clinicianDetailText.setText(CLINICIAN_DETAIL + " Reminder saved on "
                        + dateFormat.format(new Date(reminderMillis)) + ".");
            } else {
                clinicianDetailText.setText(CLINICIAN_DETAIL);
            }
        }
    }
}
