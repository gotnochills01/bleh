package com.example.mahila.utils;

import android.content.Context;
import android.database.Cursor;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SQLiteToMySQLMigrator {

    public interface MigrationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void checkAndMigrate(Context context, MigrationCallback callback) {
        TokenManager tokenManager = new TokenManager(context);

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            if (callback != null) callback.onSuccess();
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("name", tokenManager.getUserName());

            // Extract User Profile Data
            Cursor uCursor = dbHelper.getUser(email);
            if (uCursor != null && uCursor.moveToFirst()) {
                int ageCol = uCursor.getColumnIndex(DatabaseHelper.KEY_AGE);
                int dobCol = uCursor.getColumnIndex(DatabaseHelper.KEY_DATE_OF_BIRTH);
                int bloodCol = uCursor.getColumnIndex(DatabaseHelper.KEY_BLOOD_GROUP);
                int cityCol = uCursor.getColumnIndex(DatabaseHelper.KEY_CITY);
                int notesCol = uCursor.getColumnIndex(DatabaseHelper.KEY_EMERGENCY_NOTES);

                if (ageCol >= 0 && !uCursor.isNull(ageCol)) payload.put("age", uCursor.getInt(ageCol));
                if (dobCol >= 0 && !uCursor.isNull(dobCol)) payload.put("dateOfBirth", uCursor.getString(dobCol));
                if (bloodCol >= 0 && !uCursor.isNull(bloodCol)) payload.put("bloodGroup", uCursor.getString(bloodCol));
                if (cityCol >= 0 && !uCursor.isNull(cityCol)) payload.put("city", uCursor.getString(cityCol));
                if (notesCol >= 0 && !uCursor.isNull(notesCol)) payload.put("emergencyMedicalNotes", uCursor.getString(notesCol));
                uCursor.close();
            }

            // Extract Unsynced Periods
            JSONArray periodsArray = new JSONArray();
            Cursor pCursor = dbHelper.getUnsyncedPeriods(email);
            if (pCursor != null && pCursor.moveToFirst()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                do {
                    int dateIdx = pCursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                    int flowIdx = pCursor.getColumnIndex(DatabaseHelper.KEY_FLOW_LEVEL);
                    int painIdx = pCursor.getColumnIndex(DatabaseHelper.KEY_PAIN_SCALE);

                    if (dateIdx >= 0) {
                        long dateMillis = pCursor.getLong(dateIdx);
                        String dateStr = sdf.format(new Date(dateMillis));
                        JSONObject pObj = new JSONObject();
                        pObj.put("startDate", dateStr);
                        pObj.put("flowLevel", flowIdx >= 0 && !pCursor.isNull(flowIdx) ? pCursor.getString(flowIdx) : "MEDIUM");
                        pObj.put("painScale", painIdx >= 0 && !pCursor.isNull(painIdx) ? pCursor.getInt(painIdx) : 3);
                        periodsArray.put(pObj);
                    }
                } while (pCursor.moveToNext());
                pCursor.close();
            }
            payload.put("periods", periodsArray);

            // Extract Unsynced Symptoms
            JSONArray symptomsArray = new JSONArray();
            Cursor sCursor = dbHelper.getUnsyncedSymptoms(email);
            if (sCursor != null && sCursor.moveToFirst()) {
                do {
                    int nameIdx = sCursor.getColumnIndex(DatabaseHelper.KEY_SYMPTOM);
                    int dateIdx = sCursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);
                    String name = nameIdx >= 0 ? sCursor.getString(nameIdx) : "Cramps";
                    String date = dateIdx >= 0 ? sCursor.getString(dateIdx) : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    JSONObject sObj = new JSONObject();
                    sObj.put("symptomName", name);
                    sObj.put("severity", "MODERATE");
                    sObj.put("loggedDate", date);
                    symptomsArray.put(sObj);
                } while (sCursor.moveToNext());
                sCursor.close();
            }
            payload.put("symptoms", symptomsArray);

            // Extract Unsynced Contacts
            JSONArray contactsArray = new JSONArray();
            Cursor cCursor = dbHelper.getUnsyncedContacts(email);
            if (cCursor != null && cCursor.moveToFirst()) {
                do {
                    int nameIdx = cCursor.getColumnIndex(DatabaseHelper.KEY_CONTACT_NAME);
                    int phoneIdx = cCursor.getColumnIndex(DatabaseHelper.KEY_CONTACT_PHONE);
                    String name = nameIdx >= 0 ? cCursor.getString(nameIdx) : "Contact";
                    String phone = phoneIdx >= 0 ? cCursor.getString(phoneIdx) : "";
                    JSONObject cObj = new JSONObject();
                    cObj.put("contactName", name);
                    cObj.put("phoneNumber", phone);
                    cObj.put("category", "OTHER");
                    cObj.put("isFavorite", false);
                    contactsArray.put(cObj);
                } while (cCursor.moveToNext());
                cCursor.close();
            }
            payload.put("contacts", contactsArray);

            // Extract Unsynced Moods
            JSONArray moodsArray = new JSONArray();
            Cursor mCursor = dbHelper.getUnsyncedMoods(email);
            if (mCursor != null && mCursor.moveToFirst()) {
                do {
                    int moodIdx = mCursor.getColumnIndex(DatabaseHelper.KEY_MOOD_TYPE);
                    int intensityIdx = mCursor.getColumnIndex(DatabaseHelper.KEY_INTENSITY);
                    int dateIdx = mCursor.getColumnIndex(DatabaseHelper.KEY_LOGGED_DATE);

                    String moodType = moodIdx >= 0 && !mCursor.isNull(moodIdx) ? mCursor.getString(moodIdx) : "HAPPY";
                    int intensity = intensityIdx >= 0 && !mCursor.isNull(intensityIdx) ? mCursor.getInt(intensityIdx) : 3;
                    String dateStr = dateIdx >= 0 && !mCursor.isNull(dateIdx) ? mCursor.getString(dateIdx) : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    JSONObject mObj = new JSONObject();
                    mObj.put("moodType", moodType);
                    mObj.put("intensity", intensity);
                    mObj.put("loggedDate", dateStr);
                    moodsArray.put(mObj);
                } while (mCursor.moveToNext());
                mCursor.close();
            }
            payload.put("moods", moodsArray);

            ApiClient.postRequest(context, ApiService.MIGRATION_SYNC, payload, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    tokenManager.setMigrated(true);
                    dbHelper.markAllSynced(email);
                    if (callback != null) callback.onSuccess();
                }

                @Override
                public void onError(String message) {
                    if (callback != null) callback.onFailure(message);
                }
            });

        } catch (Exception e) {
            if (callback != null) callback.onFailure(e.getMessage());
        }
    }
}
