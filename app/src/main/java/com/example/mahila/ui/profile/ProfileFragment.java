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

    private EditText etName, etAge, etDob, etWeight, etHeight, etBlood, etCity, etNotes;
    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        tokenManager = new TokenManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        etName = root.findViewById(R.id.et_profile_name);
        etAge = root.findViewById(R.id.et_profile_age);
        etDob = root.findViewById(R.id.et_profile_dob);
        etWeight = root.findViewById(R.id.et_profile_weight);
        etHeight = root.findViewById(R.id.et_profile_height);
        etBlood = root.findViewById(R.id.et_profile_blood);
        etCity = root.findViewById(R.id.et_profile_city);
        etNotes = root.findViewById(R.id.et_profile_notes);

        etName.setText(tokenManager.getUserName());

        Button btnSave = root.findViewById(R.id.btn_save_profile);
        btnSave.setOnClickListener(v -> saveProfile());

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
                int notesIdx = cursor.getColumnIndex(DatabaseHelper.KEY_EMERGENCY_NOTES);
                int weightIdx = cursor.getColumnIndex(DatabaseHelper.KEY_WEIGHT);
                int heightIdx = cursor.getColumnIndex(DatabaseHelper.KEY_HEIGHT);

                if (nameIdx >= 0 && !cursor.isNull(nameIdx)) etName.setText(cursor.getString(nameIdx));
                if (ageIdx >= 0 && !cursor.isNull(ageIdx)) {
                    int ageVal = cursor.getInt(ageIdx);
                    if (ageVal > 0) etAge.setText(String.valueOf(ageVal));
                }
                if (dobIdx >= 0 && !cursor.isNull(dobIdx)) etDob.setText(cursor.getString(dobIdx));
                if (bloodIdx >= 0 && !cursor.isNull(bloodIdx)) etBlood.setText(cursor.getString(bloodIdx));
                if (cityIdx >= 0 && !cursor.isNull(cityIdx)) etCity.setText(cursor.getString(cityIdx));
                if (notesIdx >= 0 && !cursor.isNull(notesIdx)) etNotes.setText(cursor.getString(notesIdx));
                if (weightIdx >= 0 && !cursor.isNull(weightIdx)) {
                    double weightVal = cursor.getDouble(weightIdx);
                    if (weightVal > 0) etWeight.setText(String.valueOf(weightVal));
                }
                if (heightIdx >= 0 && !cursor.isNull(heightIdx)) {
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
                    String name = dto.optString("name", etName.getText().toString());
                    int age = dto.optInt("age", -1);
                    String dob = dto.optString("dateOfBirth", etDob.getText().toString());
                    String blood = dto.optString("bloodGroup", etBlood.getText().toString());
                    String city = dto.optString("city", etCity.getText().toString());
                    String notes = dto.optString("emergencyMedicalNotes", etNotes.getText().toString());
                    double weight = dto.optDouble("weight", -1);
                    double height = dto.optDouble("height", -1);

                    if (dto.has("name")) etName.setText(name);
                    if (age > 0) etAge.setText(String.valueOf(age));
                    if (dto.has("dateOfBirth")) etDob.setText(dob);
                    if (dto.has("bloodGroup")) etBlood.setText(blood);
                    if (dto.has("city")) etCity.setText(city);
                    if (dto.has("emergencyMedicalNotes")) etNotes.setText(notes);
                    if (weight > 0) etWeight.setText(String.valueOf(weight));
                    if (height > 0) etHeight.setText(String.valueOf(height));

                    String email = tokenManager.getUserEmail();
                    if (email != null && !email.isEmpty()) {
                        dbHelper.updateUserProfile(email, name, age, dob, blood, city, notes, weight, height);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onError(String message) {
                // Using offline cached profile data silently
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String blood = etBlood.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        int age = -1;
        try { age = Integer.parseInt(etAge.getText().toString().trim()); } catch (Exception ignored) {}
        double weight = -1;
        try { weight = Double.parseDouble(etWeight.getText().toString().trim()); } catch (Exception ignored) {}
        double height = -1;
        try { height = Double.parseDouble(etHeight.getText().toString().trim()); } catch (Exception ignored) {}

        String email = tokenManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            dbHelper.updateUserProfile(email, name, age, dob, blood, city, notes, weight, height);
        }

        try {
            JSONObject dto = new JSONObject();
            dto.put("name", name);
            if (age > 0) dto.put("age", age);
            dto.put("dateOfBirth", dob);
            dto.put("bloodGroup", blood);
            dto.put("city", city);
            dto.put("emergencyMedicalNotes", notes);
            if (weight > 0) dto.put("weight", weight);
            if (height > 0) dto.put("height", height);

            ApiClient.postRequest(requireContext(), ApiService.USER_PROFILE, dto, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(requireContext(), "Profile updated and synced online!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Offline Mode: Profile saved locally", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Offline Mode: Profile saved locally", Toast.LENGTH_SHORT).show();
        }
    }
}
