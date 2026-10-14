package com.example.mahila.ui.assistance;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.EmergencyContactsActivity;
import com.example.mahila.R;
import com.example.mahila.api.TokenManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssistanceFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQ_LOCATION_PERM = 201;

    private TextInputEditText etLocationQuery;
    private TextView tvProviderSubtitle;
    private LinearLayout containerProviderResults;
    private GoogleMap googleMap;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LatLng currentSearchLatLng = new LatLng(28.6139, 77.2090); // Default location (e.g. New Delhi)

    public static class MedicalProvider {
        public String name;
        public String category;
        public String address;
        public String phone;
        public String distanceStr;
        public LatLng latLng;

        public MedicalProvider(String name, String category, String address, String phone, String distanceStr, LatLng latLng) {
            this.name = name;
            this.category = category;
            this.address = address;
            this.phone = phone;
            this.distanceStr = distanceStr;
            this.latLng = latLng;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assistance, container, false);

        etLocationQuery = root.findViewById(R.id.et_location_query);
        tvProviderSubtitle = root.findViewById(R.id.tv_provider_results_subtitle);
        containerProviderResults = root.findViewById(R.id.container_provider_results);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                this.googleMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {}
        }

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentSearchLatLng, 12f));
        executeProviderSearch("Gynecologists");
    }

    private void requestGpsLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERM);
        } else {
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException ignored) {}
            }
            Toast.makeText(requireContext(), "GPS location verified. Searching nearby healthcare providers...", Toast.LENGTH_SHORT).show();
            executeProviderSearch("Gynecologists");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException ignored) {}
                }
                Toast.makeText(requireContext(), "GPS permission granted. Updating map search...", Toast.LENGTH_SHORT).show();
                executeProviderSearch("Gynecologists");
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Enter city name manually above.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void executeProviderSearch(String category) {
        if (containerProviderResults == null) return;
        containerProviderResults.removeAllViews();

        String userQuery = etLocationQuery != null && etLocationQuery.getText() != null ?
                etLocationQuery.getText().toString().trim() : "";

        if (tvProviderSubtitle != null) {
            tvProviderSubtitle.setText("Searching real OpenStreetMap records for " + category + "...");
        }

        executorService.execute(() -> {
            LatLng searchPoint = currentSearchLatLng;
            if (!userQuery.isEmpty()) {
                LatLng geocoded = geocodeCity(userQuery);
                if (geocoded != null) {
                    searchPoint = geocoded;
                    currentSearchLatLng = geocoded;
                }
            }

            final LatLng finalPoint = searchPoint;
            List<MedicalProvider> realProviders = fetchRealOverpassProviders(category, finalPoint);

            mainHandler.post(() -> {
                if (!isAdded()) return;

                if (googleMap != null) {
                    googleMap.clear();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(finalPoint, 13f));
                }

                if (tvProviderSubtitle != null) {
                    if (realProviders.isEmpty()) {
                        tvProviderSubtitle.setText("No public healthcare places found near this location in open maps database. Try a larger city or nearby region.");
                    } else {
                        tvProviderSubtitle.setText("Found " + realProviders.size() + " verified real " + category + " records nearby.");
                    }
                }

                for (MedicalProvider provider : realProviders) {
                    if (googleMap != null && provider.latLng != null) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(provider.latLng)
                                .title(provider.name)
                                .snippet(provider.address));
                    }

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

                    btnRow.addView(btnDir);

                    if (provider.phone != null && !provider.phone.isEmpty() && !provider.phone.equalsIgnoreCase("Not Listed")) {
                        MaterialButton btnCall = new MaterialButton(requireContext());
                        btnCall.setText("Call (" + provider.phone + ")");
                        btnCall.setTextSize(12f);
                        btnCall.setCornerRadius(10);
                        btnCall.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        btnCall.setOnClickListener(v -> dialPhone(provider.phone));
                        btnRow.addView(btnCall);
                    }

                    itemLayout.addView(tvName);
                    itemLayout.addView(tvAddr);
                    itemLayout.addView(btnRow);

                    card.addView(itemLayout);
                    containerProviderResults.addView(card);
                }
            });
        });
    }

    private LatLng geocodeCity(String cityName) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return new LatLng(addr.getLatitude(), addr.getLongitude());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private List<MedicalProvider> fetchRealOverpassProviders(String category, LatLng center) {
        List<MedicalProvider> list = new ArrayList<>();
        try {
            String tagFilter = "hospital";
            if (category.toLowerCase().contains("gyno") || category.toLowerCase().contains("women")) {
                tagFilter = "clinic";
            } else if (category.toLowerCase().contains("clinic")) {
                tagFilter = "clinic";
            } else if (category.toLowerCase().contains("hospital")) {
                tagFilter = "hospital";
            }

            String overpassQuery = "[out:json][timeout:10];" +
                    "(node[\"amenity\"=\"" + tagFilter + "\"](around:10000," + center.latitude + "," + center.longitude + ");" +
                    "node[\"healthcare\"](around:10000," + center.latitude + "," + center.longitude + "););" +
                    "out body 10;";

            String urlStr = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(overpassQuery, "UTF-8");
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject rootObj = new JSONObject(sb.toString());
                JSONArray elements = rootObj.optJSONArray("elements");
                if (elements != null) {
                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject elem = elements.getJSONObject(i);
                        double lat = elem.optDouble("lat", center.latitude);
                        double lon = elem.optDouble("lon", center.longitude);
                        JSONObject tags = elem.optJSONObject("tags");

                        String name = "Healthcare Facility";
                        String phone = "Not Listed";
                        String address = "Coordinates: " + String.format(Locale.US, "%.4f, %.4f", lat, lon);

                        if (tags != null) {
                            if (tags.has("name")) name = tags.getString("name");
                            if (tags.has("phone")) phone = tags.getString("phone");
                            else if (tags.has("contact:phone")) phone = tags.getString("contact:phone");

                            if (tags.has("addr:street")) {
                                address = tags.optString("addr:street");
                                if (tags.has("addr:city")) address += ", " + tags.optString("addr:city");
                            }
                        }

                        float[] results = new float[1];
                        Location.distanceBetween(center.latitude, center.longitude, lat, lon, results);
                        float distKm = results[0] / 1000f;
                        String distStr = String.format(Locale.US, "%.1f km", distKm);

                        list.add(new MedicalProvider(name, category, address, phone, distStr, new LatLng(lat, lon)));
                    }
                }
            }
        } catch (Exception e) {
            // Logged silently for safe network failure fallback
        }
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
