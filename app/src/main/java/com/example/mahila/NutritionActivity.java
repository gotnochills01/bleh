package com.example.mahila;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import com.example.mahila.dto.NutritionDTO;
import com.example.mahila.utils.CycleCalculator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NutritionActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;
    private RecyclerView rvNutrition;
    private TextView tvEmptyNutrition;
    private ChipGroup chipGroupPhase, chipGroupDiet, chipGroupBudget;

    private String selectedPhase = "ALL";
    private String selectedDiet = "ALL";
    private String selectedBudget = "ALL";

    private final List<NutritionDTO> list = new ArrayList<>();
    private NutritionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        dbHelper = new DatabaseHelper(this);
        tokenManager = new TokenManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNutrition = findViewById(R.id.rv_nutrition);
        tvEmptyNutrition = findViewById(R.id.tv_empty_nutrition);
        chipGroupPhase = findViewById(R.id.chip_group_phase);
        chipGroupDiet = findViewById(R.id.chip_group_diet);
        chipGroupBudget = findViewById(R.id.chip_group_budget);

        rvNutrition.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NutritionAdapter(this, list);
        rvNutrition.setAdapter(adapter);

        detectActiveCyclePhase();
        setupFilterListeners();
        loadNutritionData();
    }

    private void detectActiveCyclePhase() {
        String email = tokenManager.getUserEmail();
        if (email == null) email = "guest@mahila.local";

        Cursor cursor = dbHelper.getPeriod(email);
        long lastPeriodMs = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idxDate = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                if (idxDate >= 0) {
                    lastPeriodMs = cursor.getLong(idxDate);
                }
            }
            cursor.close();
        }

        if (lastPeriodMs > 0) {
            CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfo(lastPeriodMs, 28);
            if (info != null && info.currentPhase != null) {
                String phaseStr = info.currentPhase.toLowerCase();
                if (phaseStr.contains("menstrual")) {
                    selectedPhase = "MENSTRUAL";
                    chipGroupPhase.check(R.id.chip_menstrual);
                } else if (phaseStr.contains("follicular")) {
                    selectedPhase = "FOLLICULAR";
                    chipGroupPhase.check(R.id.chip_follicular);
                } else if (phaseStr.contains("ovulat")) {
                    selectedPhase = "OVULATORY";
                    chipGroupPhase.check(R.id.chip_ovulatory);
                } else if (phaseStr.contains("luteal")) {
                    selectedPhase = "LUTEAL";
                    chipGroupPhase.check(R.id.chip_luteal);
                } else {
                    selectedPhase = "ALL";
                    chipGroupPhase.check(R.id.chip_all_phases);
                }
            }
        }
    }

    private void setupFilterListeners() {
        chipGroupPhase.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_menstrual)) selectedPhase = "MENSTRUAL";
            else if (checkedIds.contains(R.id.chip_follicular)) selectedPhase = "FOLLICULAR";
            else if (checkedIds.contains(R.id.chip_ovulatory)) selectedPhase = "OVULATORY";
            else if (checkedIds.contains(R.id.chip_luteal)) selectedPhase = "LUTEAL";
            else selectedPhase = "ALL";
            loadNutritionData();
        });

        chipGroupDiet.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_veg)) selectedDiet = "VEGETARIAN";
            else if (checkedIds.contains(R.id.chip_vegan)) selectedDiet = "VEGAN";
            else if (checkedIds.contains(R.id.chip_nonveg)) selectedDiet = "NON_VEG";
            else selectedDiet = "ALL";
            loadNutritionData();
        });

        chipGroupBudget.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_budget_low)) selectedBudget = "BUDGET";
            else if (checkedIds.contains(R.id.chip_budget_med)) selectedBudget = "MODERATE";
            else selectedBudget = "ALL";
            loadNutritionData();
        });
    }

    private void loadNutritionData() {
        // Load offline cached items first
        loadFromLocalCache();

        // Query remote Spring Boot backend
        StringBuilder endpoint = new StringBuilder(ApiService.NUTRITION);
        boolean hasParam = false;

        if (!"ALL".equalsIgnoreCase(selectedPhase)) {
            endpoint.append("?phase=").append(selectedPhase);
            hasParam = true;
        }

        if (!"ALL".equalsIgnoreCase(selectedDiet)) {
            endpoint.append(hasParam ? "&" : "?").append("dietaryType=").append(selectedDiet);
            hasParam = true;
        }

        if (!"ALL".equalsIgnoreCase(selectedBudget)) {
            endpoint.append(hasParam ? "&" : "?").append("budgetLevel=").append(selectedBudget);
        }

        ApiClient.getRequest(this, endpoint.toString(), new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    List<NutritionDTO> remoteList = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        long id = obj.optLong("id");
                        String p = obj.optString("cyclePhase");
                        String cat = obj.optString("mealCategory");
                        String food = obj.optString("foodItemName");
                        String nut = obj.optString("keyNutrients");
                        String ben = obj.optString("healthBenefits");
                        String dt = obj.optString("dietaryType");
                        String bgt = obj.optString("budgetLevel");
                        String ev = obj.optString("evidenceLevel");
                        String warn = obj.optString("safetyWarnings");
                        String url = obj.optString("evidenceUrl");

                        dbHelper.saveNutritionItemToCache(id, p, cat, food, nut, ben, dt, bgt, ev, warn, url);
                        remoteList.add(new NutritionDTO(id, p, cat, food, nut, ben, dt, bgt, ev, warn, url));
                    }

                    if (!remoteList.isEmpty()) {
                        list.clear();
                        list.addAll(remoteList);
                        updateUiState();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                // Backend unreachable, fallback maintained silently via SQLite
            }
        });
    }

    private void loadFromLocalCache() {
        list.clear();
        Cursor cursor = dbHelper.getCachedNutritionRecommendations(selectedPhase, selectedDiet, selectedBudget);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
                String phase = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CYCLE_PHASE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CATEGORY));
                String foodName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FOOD_ITEM_NAME));
                String nutrients = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_KEY_NUTRIENTS));
                String benefits = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_HEALTH_BENEFITS));
                String diet = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DIETARY_TYPE));
                String budget = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BUDGET_LEVEL));
                String evidence = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EVIDENCE_LEVEL));
                String warnings = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SAFETY_WARNINGS));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EVIDENCE_URL));

                list.add(new NutritionDTO(id, phase, category, foodName, nutrients, benefits, diet, budget, evidence, warnings, url));
            }
            cursor.close();
        }

        updateUiState();
    }

    private void updateUiState() {
        adapter.notifyDataSetChanged();
        if (list.isEmpty()) {
            tvEmptyNutrition.setVisibility(View.VISIBLE);
            rvNutrition.setVisibility(View.GONE);
        } else {
            tvEmptyNutrition.setVisibility(View.GONE);
            rvNutrition.setVisibility(View.VISIBLE);
        }
    }
}
