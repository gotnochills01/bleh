package com.example.mahila.ui.profile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.R;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private EditText etName, etAge, etDob, etWeight, etHeight, etBlood, etCity, etConditions, etNotes;
    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        tokenManager = new TokenManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        etName       = root.findViewById(R.id.et_profile_name);
        etAge        = root.findViewById(R.id.et_profile_age);
        etDob        = root.findViewById(R.id.et_profile_dob);
        etWeight     = root.findViewById(R.id.et_profile_weight);
        etHeight     = root.findViewById(R.id.et_profile_height);
        etBlood      = root.findViewById(R.id.et_profile_blood);
        etCity       = root.findViewById(R.id.et_profile_city);
        etConditions = root.findViewById(R.id.et_profile_conditions);
        etNotes      = root.findViewById(R.id.et_profile_notes);

        if (etName != null) etName.setText(tokenManager.getUserName());

        Button btnSave = root.findViewById(R.id.btn_save_profile);
        if (btnSave != null) btnSave.setOnClickListener(v -> saveProfile());

        loadProfileLocal();
        loadProfileRemote();

        return root;
    }

    private void loadProfileLocal() {
        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) return;

        try (Cursor cursor = dbHelper.getUser(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
                int ageIdx = cursor.getColumnIndex(DatabaseHelper.KEY_AGE);
                int dobIdx = cursor.getColumnIndex(DatabaseHelper.KEY_DATE_OF_BIRTH);
                int bloodIdx = cursor.getColumnIndex(DatabaseHelper.KEY_BLOOD_GROUP);
                int cityIdx = cursor.getColumnIndex(DatabaseHelper.KEY_CITY);
                int condIdx = cursor.getColumnIndex(DatabaseHelper.KEY_DIAGNOSED_CONDITIONS);
                int notesIdx = cursor.getColumnIndex(DatabaseHelper.KEY_EMERGENCY_NOTES);
                int weightIdx = cursor.getColumnIndex(DatabaseHelper.KEY_WEIGHT);
                int heightIdx = cursor.getColumnIndex(DatabaseHelper.KEY_HEIGHT);

                if (nameIdx >= 0 && !cursor.isNull(nameIdx) && etName != null) etName.setText(cursor.getString(nameIdx));
                if (ageIdx >= 0 && !cursor.isNull(ageIdx) && etAge != null) {
                    int ageVal = cursor.getInt(ageIdx);
                    if (ageVal > 0) etAge.setText(String.valueOf(ageVal));
                }
                if (dobIdx >= 0 && !cursor.isNull(dobIdx) && etDob != null) etDob.setText(cursor.getString(dobIdx));
                if (bloodIdx >= 0 && !cursor.isNull(bloodIdx) && etBlood != null) etBlood.setText(cursor.getString(bloodIdx));
                if (cityIdx >= 0 && !cursor.isNull(cityIdx) && etCity != null) etCity.setText(cursor.getString(cityIdx));
                if (condIdx >= 0 && !cursor.isNull(condIdx) && etConditions != null) etConditions.setText(cursor.getString(condIdx));
                if (notesIdx >= 0 && !cursor.isNull(notesIdx) && etNotes != null) etNotes.setText(cursor.getString(notesIdx));
                if (weightIdx >= 0 && !cursor.isNull(weightIdx) && etWeight != null) {
                    double weightVal = cursor.getDouble(weightIdx);
                    if (weightVal > 0) etWeight.setText(String.valueOf(weightVal));
                }
                if (heightIdx >= 0 && !cursor.isNull(heightIdx) && etHeight != null) {
                    double heightVal = cursor.getDouble(heightIdx);
                    if (heightVal > 0) etHeight.setText(String.valueOf(heightVal));
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadProfileRemote() {
        ApiClient.getRequest(requireContext(), ApiService.USER_PROFILE, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject dto = new JSONObject(result);
                    String name = dto.optString("name", etName != null ? etName.getText().toString() : "");
                    int age = dto.optInt("age", -1);
                    String dob = dto.optString("dateOfBirth", etDob != null ? etDob.getText().toString() : "");
                    String blood = dto.optString("bloodGroup", etBlood != null ? etBlood.getText().toString() : "");
                    String city = dto.optString("city", etCity != null ? etCity.getText().toString() : "");
                    String cond = dto.optString("diagnosedConditions", etConditions != null ? etConditions.getText().toString() : "");
                    String notes = dto.optString("emergencyMedicalNotes", etNotes != null ? etNotes.getText().toString() : "");
                    double weight = dto.optDouble("weight", -1);
                    double height = dto.optDouble("height", -1);

                    if (dto.has("name") && etName != null) etName.setText(name);
                    if (age > 0 && etAge != null) etAge.setText(String.valueOf(age));
                    if (dto.has("dateOfBirth") && etDob != null) etDob.setText(dob);
                    if (dto.has("bloodGroup") && etBlood != null) etBlood.setText(blood);
                    if (dto.has("city") && etCity != null) etCity.setText(city);
                    if (dto.has("diagnosedConditions") && etConditions != null) etConditions.setText(cond);
                    if (dto.has("emergencyMedicalNotes") && etNotes != null) etNotes.setText(notes);

                    String email = tokenManager.getUserEmail();
                    if (email != null && !email.isEmpty()) {
                        dbHelper.updateUserProfile(email, name, age, dob, blood, city, notes, weight, height, cond, null);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void saveProfile() {
        String name = etName != null ? etName.getText().toString().trim() : "";
        String dob = etDob != null ? etDob.getText().toString().trim() : "";
        String blood = etBlood != null ? etBlood.getText().toString().trim() : "";
        String city = etCity != null ? etCity.getText().toString().trim() : "";
        String cond = etConditions != null ? etConditions.getText().toString().trim() : "";
        String notes = etNotes != null ? etNotes.getText().toString().trim() : "";
        int age = -1;
        try { if (etAge != null) age = Integer.parseInt(etAge.getText().toString().trim()); } catch (Exception ignored) {}
        double weight = -1;
        try { if (etWeight != null) weight = Double.parseDouble(etWeight.getText().toString().trim()); } catch (Exception ignored) {}
        double height = -1;
        try { if (etHeight != null) height = Double.parseDouble(etHeight.getText().toString().trim()); } catch (Exception ignored) {}

        String email = tokenManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            dbHelper.updateUserProfile(email, name, age, dob, blood, city, notes, weight, height, cond, null);
        }

        try {
            JSONObject dto = new JSONObject();
            dto.put("name", name);
            if (age > 0) dto.put("age", age);
            dto.put("dateOfBirth", dob);
            dto.put("bloodGroup", blood);
            dto.put("city", city);
            dto.put("diagnosedConditions", cond);
            dto.put("emergencyMedicalNotes", notes);
            if (weight > 0) dto.put("weight", weight);
            if (height > 0) dto.put("height", height);

            ApiClient.postRequest(requireContext(), ApiService.USER_PROFILE, dto, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(requireContext(), "Health profile updated and synced!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Offline Mode: Health profile saved locally", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Offline Mode: Health profile saved locally", Toast.LENGTH_SHORT).show();
        }
    }
}
