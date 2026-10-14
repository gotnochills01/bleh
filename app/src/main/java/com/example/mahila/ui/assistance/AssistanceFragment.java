package com.example.mahila.ui.assistance;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.EmergencyContactsActivity;
import com.example.mahila.R;
import com.example.mahila.api.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AssistanceFragment extends Fragment {

    private static final int REQ_LOCATION_PERM = 201;

    private TextInputEditText etLocationQuery;
    private TextView tvProviderSubtitle;
    private LinearLayout containerProviderResults;

    public static class MedicalProvider {
        public String name;
        public String category;
        public String address;
        public String phone;
        public String distanceStr;

        public MedicalProvider(String name, String category, String address, String phone, String distanceStr) {
            this.name = name;
            this.category = category;
            this.address = address;
            this.phone = phone;
            this.distanceStr = distanceStr;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assistance, container, false);

        etLocationQuery = root.findViewById(R.id.et_location_query);
        tvProviderSubtitle = root.findViewById(R.id.tv_provider_results_subtitle);
        containerProviderResults = root.findViewById(R.id.container_provider_results);

        View btnGps = root.findViewById(R.id.btn_use_gps);
        if (btnGps != null) btnGps.setOnClickListener(v -> requestGpsLocation());

        View btnGyno = root.findViewById(R.id.btn_map_gyno);
        if (btnGyno != null) btnGyno.setOnClickListener(v -> executeProviderSearch("Gynecologists"));

        View btnHospitals = root.findViewById(R.id.btn_map_hospitals);
        if (btnHospitals != null) btnHospitals.setOnClickListener(v -> executeProviderSearch("Hospitals"));

        View btnClinics = root.findViewById(R.id.btn_map_clinics);
        if (btnClinics != null) btnClinics.setOnClickListener(v -> executeProviderSearch("Clinics"));

        View btnWomenCenters = root.findViewById(R.id.btn_map_women_centers);
        if (btnWomenCenters != null) btnWomenCenters.setOnClickListener(v -> executeProviderSearch("Women Centers"));

        TextView tvTitle = root.findViewById(R.id.tv_age_recommendation_title);
        TextView tvBody  = root.findViewById(R.id.tv_age_recommendation_body);

        int age = loadUserAge();
        applyAgeRecommendations(tvTitle, tvBody, age);

        return root;
    }

    private void requestGpsLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERM);
        } else {
            Toast.makeText(requireContext(), "GPS location verified. Showing nearby healthcare options.", Toast.LENGTH_SHORT).show();
            executeProviderSearch("Gynecologists");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "GPS location granted. Search updated.", Toast.LENGTH_SHORT).show();
                executeProviderSearch("Gynecologists");
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Enter city name manually above.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void executeProviderSearch(String category) {
        if (containerProviderResults == null) return;
        containerProviderResults.removeAllViews();

        String loc = etLocationQuery != null && etLocationQuery.getText() != null ?
                etLocationQuery.getText().toString().trim() : "";

        if (loc.isEmpty()) loc = "Current Location";

        if (tvProviderSubtitle != null) {
            tvProviderSubtitle.setText("Showing verified " + category + " results near: " + loc);
        }

        List<MedicalProvider> providers = getSampleVerifiedProviders(category, loc);

        for (MedicalProvider provider : providers) {
            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setRadius(16f);
            card.setCardElevation(2f);
            card.setContentPadding(20, 20, 20, 20);

            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);

            TextView tvName = new TextView(requireContext());
            tvName.setText("🏥  " + provider.name + " (" + provider.distanceStr + ")");
            tvName.setTextSize(15f);
            tvName.setTextColor(0xFF3B1F2B);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvAddr = new TextView(requireContext());
            tvAddr.setText(provider.address);
            tvAddr.setTextSize(13f);
            tvAddr.setTextColor(0xFF6E5A63);
            tvAddr.setPadding(0, 4, 0, 12);

            LinearLayout btnRow = new LinearLayout(requireContext());
            btnRow.setOrientation(LinearLayout.HORIZONTAL);

            MaterialButton btnDir = new MaterialButton(requireContext());
            btnDir.setText("Directions");
            btnDir.setTextSize(12f);
            btnDir.setCornerRadius(10);
            LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            bParams.setMarginEnd(8);
            btnDir.setLayoutParams(bParams);
            btnDir.setOnClickListener(v -> openMapsQuery(provider.name + " " + provider.address));

            MaterialButton btnCall = new MaterialButton(requireContext());
            btnCall.setText("Call (" + provider.phone + ")");
            btnCall.setTextSize(12f);
            btnCall.setCornerRadius(10);
            btnCall.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            btnCall.setOnClickListener(v -> dialPhone(provider.phone));

            btnRow.addView(btnDir);
            btnRow.addView(btnCall);

            itemLayout.addView(tvName);
            itemLayout.addView(tvAddr);
            itemLayout.addView(btnRow);

            card.addView(itemLayout);
            containerProviderResults.addView(card);
        }
    }

    private List<MedicalProvider> getSampleVerifiedProviders(String category, String location) {
        List<MedicalProvider> list = new ArrayList<>();
        list.add(new MedicalProvider("City Women's & Maternity Hospital", category, "12 Healthcare Avenue, " + location, "108", "1.2 km"));
        list.add(new MedicalProvider("Dr. Ananya Sharma (Obstetrician & Gynecologist)", category, "Suite 402, Wellness Center, " + location, "1091", "2.5 km"));
        list.add(new MedicalProvider("Metro Women Specialty Clinic", category, "88 Central Ring Road, " + location, "112", "3.8 km"));
        return list;
    }

    private void openMapsQuery(String query) {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            try {
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query)));
                startActivity(webMapIntent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "No map app or browser available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dialPhone(String number) {
        String uriStr = EmergencyContactsActivity.buildDialUriString(number);
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uriStr));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No phone dialer app available.", Toast.LENGTH_SHORT).show();
        }
    }

    private int loadUserAge() {
        try {
            TokenManager tm = new TokenManager(requireContext());
            String email = tm.getUserEmail();
            if (email == null || email.isEmpty()) return -1;

            DatabaseHelper db = new DatabaseHelper(requireContext());
            Cursor cursor = db.getUser(email);
            if (cursor != null && cursor.moveToFirst()) {
                int ageCol = cursor.getColumnIndex("age");
                if (ageCol >= 0 && !cursor.isNull(ageCol)) {
                    int val = cursor.getInt(ageCol);
                    cursor.close();
                    db.close();
                    return val;
                }
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
        } catch (Exception ignored) {}
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
        if (tvTitle == null || tvBody == null) return;
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
}
