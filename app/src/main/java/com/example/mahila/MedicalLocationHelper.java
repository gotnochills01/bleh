package com.example.mahila;

import com.google.android.gms.maps.model.LatLng;

public final class MedicalLocationHelper {

    private MedicalLocationHelper() {
    }

    public static boolean isValidLocation(LatLng location) {
        if (location == null) {
            return false;
        }

        if (!Double.isFinite(location.latitude) || !Double.isFinite(location.longitude)) {
            return false;
        }

        return Math.abs(location.latitude) <= 90.0 && Math.abs(location.longitude) <= 180.0;
    }

    public static String buildUnavailableMessage(String category) {
        String cleanCategory = category == null || category.trim().isEmpty() ? "healthcare providers" : category.trim();
        return "Location unavailable. Use GPS or enter a city name above to search for " + cleanCategory + " nearby.";
    }

    public static String formatLocation(LatLng location) {
        if (!isValidLocation(location)) {
            return "Location unavailable";
        }
        return String.format(java.util.Locale.US, "%.4f, %.4f", location.latitude, location.longitude);
    }
}
