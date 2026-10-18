package com.example.mahila;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.mahila.api.TokenManager;
import com.example.mahila.ui.assistance.AssistanceFragment;
import com.example.mahila.ui.awareness.AwarenessFragment;
import com.example.mahila.ui.home.HomeFragment;
import com.example.mahila.ui.profile.ProfileFragment;
import com.example.mahila.ui.tracker.TrackerFragment;
import com.example.mahila.utils.LocaleHelper;
import com.example.mahila.utils.SQLiteToMySQLMigrator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private TokenManager tokenManager;
    private AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);
        appPreferences = new AppPreferences(this);
        LocaleHelper.setLocale(this, tokenManager.getLanguage());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.main_toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Edge-to-edge window insets
        View mainCoordinator = findViewById(R.id.main_coordinator);
        if (mainCoordinator != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainCoordinator, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                toolbar.setPadding(0, systemBars.top, 0, 0);
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }

        // Toolbar Navigation Drawer toggle
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Setup Drawer Header Info
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvName = headerView.findViewById(R.id.tv_drawer_user_name);
            TextView tvEmail = headerView.findViewById(R.id.tv_drawer_user_email);
            if (tvName != null) {
                String userName = tokenManager.getUserName();
                tvName.setText(userName != null && !userName.isEmpty() ? userName : "Mahila User");
            }
            if (tvEmail != null) {
                String userEmail = tokenManager.getUserEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    userEmail = appPreferences.getCurrentUserEmail();
                }
                tvEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "Guest Mode");
            }
        }

        navigationView.setNavigationItemSelectedListener(this);

        // Bottom Navigation Tab Switching
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                toolbar.setTitle(R.string.app_name);
            } else if (itemId == R.id.nav_tracker) {
                selectedFragment = new TrackerFragment();
                toolbar.setTitle("Period Tracker");
            } else if (itemId == R.id.nav_assistance) {
                selectedFragment = new AssistanceFragment();
                toolbar.setTitle("Medical Assistance");
            } else if (itemId == R.id.nav_awareness) {
                selectedFragment = new AwarenessFragment();
                toolbar.setTitle("Awareness Hub");
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                toolbar.setTitle("Profile & Health");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Check if intent launched with specific tab request (e.g. from MedicalAssistanceActivity)
        String openTab = getIntent() != null ? getIntent().getStringExtra("OPEN_TAB") : null;
        if (savedInstanceState == null) {
            if ("ASSISTANCE".equalsIgnoreCase(openTab)) {
                bottomNav.setSelectedItemId(R.id.nav_assistance);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                toolbar.setTitle(R.string.app_name);
            }
        }

        // Safe background SQLite to MySQL migration check
        SQLiteToMySQLMigrator.checkAndMigrate(this, new SQLiteToMySQLMigrator.MigrationCallback() {
            @Override
            public void onSuccess() {
                // Migrated cleanly or already up-to-date
            }

            @Override
            public void onFailure(String error) {
                // Offline mode preserved with local SQLite
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drawer_nutrition) {
            startActivity(new Intent(this, NutritionActivity.class));
        } else if (id == R.id.drawer_settings || id == R.id.drawer_language) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.drawer_notifications) {
            Toast.makeText(this, "Reminders and Notifications are active", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.drawer_privacy) {
            new AlertDialog.Builder(this)
                    .setTitle("Privacy & Security")
                    .setMessage("Mahila stores your cycle, health, and contacts securely. Data is encrypted and isolated per user.")
                    .setPositiveButton("Got it", null)
                    .show();
        } else if (id == R.id.drawer_about) {
            new AlertDialog.Builder(this)
                    .setTitle("About Mahila")
                    .setMessage("Mahila is an advanced personal health, cycle tracking, and medical companion for women.\nVersion 2.0 (Material Design 3)")
                    .setPositiveButton("Close", null)
                    .show();
        } else if (id == R.id.drawer_logout) {
            tokenManager.clear();
            appPreferences.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
