package com.example.mahila.ui.assistance;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.R;
import com.example.mahila.api.TokenManager;

public class AssistanceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assistance, container, false);

        root.findViewById(R.id.btn_map_gyno).setOnClickListener(v -> searchMaps("Gynecologists near me"));
        root.findViewById(R.id.btn_map_hospitals).setOnClickListener(v -> searchMaps("Hospitals near me"));
        root.findViewById(R.id.btn_map_clinics).setOnClickListener(v -> searchMaps("Clinics near me"));
        root.findViewById(R.id.btn_map_women_centers).setOnClickListener(v -> searchMaps("Women Health Centers near me"));

        TextView tvTitle = root.findViewById(R.id.tv_age_recommendation_title);
        TextView tvBody  = root.findViewById(R.id.tv_age_recommendation_body);

        // Read age from user profile in SQLite
        int age = loadUserAge();
        applyAgeRecommendations(tvTitle, tvBody, age);

        return root;
    }

    private int loadUserAge() {
        try {
            TokenManager tm = new TokenManager(requireContext());
            String email = tm.getUserEmail();
            if (email == null || email.isEmpty()) return -1;

            DatabaseHelper db = new DatabaseHelper(requireContext());
            Cursor cursor = db.getUser(email);
            if (cursor != null && cursor.moveToFirst()) {
                // Try age column first
                int ageCol = cursor.getColumnIndex("age");
                if (ageCol >= 0 && !cursor.isNull(ageCol)) {
                    int val = cursor.getInt(ageCol);
                    cursor.close();
                    db.close();
                    return val;
                }
                // Fallback: derive from date_of_birth
                int dobCol = cursor.getColumnIndex("date_of_birth");
                if (dobCol >= 0 && !cursor.isNull(dobCol)) {
                    String dob = cursor.getString(dobCol);
                    cursor.close();
                    db.close();
                    return calculateAgeFromDob(dob);
                }
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    private int calculateAgeFromDob(String dob) {
        try {
            String[] parts = dob.split("-");
            int birthYear = Integer.parseInt(parts[0]);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            return currentYear - birthYear;
        } catch (Exception e) {
            return -1;
        }
    }

    private void applyAgeRecommendations(TextView tvTitle, TextView tvBody, int age) {
        if (age >= 13 && age <= 19) {
            tvTitle.setText("Recommendations for Age 13–19 (Adolescent)");
            tvBody.setText("• Adolescent Gynecologists\n• Menstrual Education Specialists\n• PCOS Awareness Experts");
        } else if (age >= 20 && age <= 35) {
            tvTitle.setText("Recommendations for Age 20–35 (Reproductive)");
            tvBody.setText("• Fertility Specialists\n• Obstetricians & Gynecologists\n• PCOS & Thyroid Experts");
        } else if (age >= 36 && age <= 50) {
            tvTitle.setText("Recommendations for Age 36–50 (Hormonal)");
            tvBody.setText("• Hormonal Health Specialists\n• Fibroid Experts\n• Endometriosis Specialists");
        } else if (age > 50) {
            tvTitle.setText("Recommendations for Age 50+ (Wellness)");
            tvBody.setText("• Menopause Specialists\n• Bone Health (Osteoporosis) Experts\n• Women's Preventive Wellness");
        } else {
            tvTitle.setText("Recommended Specialists");
            tvBody.setText("Complete your profile with your date of birth to see personalised specialist recommendations.");
        }
    }

    private void searchMaps(String query) {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))));
        }
    }
}
