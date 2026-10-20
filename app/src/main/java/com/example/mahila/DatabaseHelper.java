package com.example.mahila;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mahila.db";
    private static final int DATABASE_VERSION = 6;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PERIODS = "periods";
    public static final String TABLE_SYMPTOMS = "symptoms";
    public static final String TABLE_CONTACTS = "contacts";
    public static final String TABLE_MOODS = "moods";
    public static final String TABLE_REMINDERS = "reminders";
    public static final String TABLE_NUTRITION_CACHE = "nutrition_cache";

    // Common Columns
    public static final String KEY_ID = "id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_IS_SYNCED = "is_synced";

    // Users Table Columns
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AGE = "age";
    public static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    public static final String KEY_BLOOD_GROUP = "blood_group";
    public static final String KEY_CITY = "city";
    public static final String KEY_EMERGENCY_NOTES = "emergency_notes";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_DIAGNOSED_CONDITIONS = "diagnosed_conditions";
    public static final String KEY_HEALTH_PREFERENCES = "health_preferences";

    // Periods Table Columns
    public static final String KEY_PERIOD_DATE = "period_date";
    public static final String KEY_FLOW_LEVEL = "flow_level";
    public static final String KEY_PAIN_SCALE = "pain_scale";

    // Symptoms Table Columns
    public static final String KEY_SYMPTOM = "symptom";
    public static final String KEY_SEVERITY = "severity";
    public static final String KEY_CREATED_AT = "created_at";

    // Contacts Table Columns
    public static final String KEY_CONTACT_NAME = "contact_name";
    public static final String KEY_CONTACT_PHONE = "contact_phone";

    // Moods Table Columns
    public static final String KEY_MOOD_TYPE = "mood_type";
    public static final String KEY_INTENSITY = "intensity";
    public static final String KEY_LOGGED_DATE = "logged_date";

    // Reminders Table Columns
    public static final String KEY_REMINDER_TITLE = "title";
    public static final String KEY_REMINDER_CATEGORY = "category";
    public static final String KEY_REMINDER_TIME = "time_millis";
    public static final String KEY_REMINDER_ENABLED = "is_enabled";

    // Nutrition Cache Table Columns
    public static final String KEY_CYCLE_PHASE = "cycle_phase";
    public static final String KEY_MEAL_CATEGORY = "meal_category";
    public static final String KEY_FOOD_ITEM_NAME = "food_item_name";
    public static final String KEY_KEY_NUTRIENTS = "key_nutrients";
    public static final String KEY_HEALTH_BENEFITS = "health_benefits";
    public static final String KEY_DIETARY_TYPE = "dietary_type";
    public static final String KEY_BUDGET_LEVEL = "budget_level";
    public static final String KEY_EVIDENCE_LEVEL = "evidence_level";
    public static final String KEY_SAFETY_WARNINGS = "safety_warnings";
    public static final String KEY_EVIDENCE_URL = "evidence_url";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_NAME + " TEXT, "
                + KEY_EMAIL + " TEXT UNIQUE, "
                + KEY_PASSWORD + " TEXT, "
                + KEY_AGE + " INTEGER DEFAULT 0, "
                + KEY_DATE_OF_BIRTH + " TEXT, "
                + KEY_BLOOD_GROUP + " TEXT, "
                + KEY_CITY + " TEXT, "
                + KEY_EMERGENCY_NOTES + " TEXT, "
                + KEY_WEIGHT + " REAL DEFAULT 0.0, "
                + KEY_HEIGHT + " REAL DEFAULT 0.0, "
                + KEY_DIAGNOSED_CONDITIONS + " TEXT, "
                + KEY_HEALTH_PREFERENCES + " TEXT" + ")";

        String CREATE_PERIODS_TABLE = "CREATE TABLE " + TABLE_PERIODS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_EMAIL + " TEXT, "
                + KEY_PERIOD_DATE + " INTEGER, "
                + KEY_FLOW_LEVEL + " TEXT DEFAULT 'MEDIUM', "
                + KEY_PAIN_SCALE + " INTEGER DEFAULT 0, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";

        String CREATE_SYMPTOMS_TABLE = "CREATE TABLE " + TABLE_SYMPTOMS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_EMAIL + " TEXT, "
                + KEY_SYMPTOM + " TEXT, "
                + KEY_SEVERITY + " TEXT DEFAULT 'MODERATE', "
                + KEY_CREATED_AT + " TEXT, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_EMAIL + " TEXT, "
                + KEY_CONTACT_NAME + " TEXT, "
                + KEY_CONTACT_PHONE + " TEXT, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";

        String CREATE_MOODS_TABLE = "CREATE TABLE " + TABLE_MOODS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_EMAIL + " TEXT, "
                + KEY_MOOD_TYPE + " TEXT, "
                + KEY_INTENSITY + " INTEGER DEFAULT 3, "
                + KEY_LOGGED_DATE + " TEXT, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";

        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_EMAIL + " TEXT, "
                + KEY_REMINDER_TITLE + " TEXT, "
                + KEY_REMINDER_CATEGORY + " TEXT, "
                + KEY_REMINDER_TIME + " INTEGER, "
                + KEY_REMINDER_ENABLED + " INTEGER DEFAULT 1, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";

        String CREATE_NUTRITION_TABLE = "CREATE TABLE " + TABLE_NUTRITION_CACHE + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_CYCLE_PHASE + " TEXT, "
                + KEY_MEAL_CATEGORY + " TEXT, "
                + KEY_FOOD_ITEM_NAME + " TEXT, "
                + KEY_KEY_NUTRIENTS + " TEXT, "
                + KEY_HEALTH_BENEFITS + " TEXT, "
                + KEY_DIETARY_TYPE + " TEXT, "
                + KEY_BUDGET_LEVEL + " TEXT, "
                + KEY_EVIDENCE_LEVEL + " TEXT, "
                + KEY_SAFETY_WARNINGS + " TEXT, "
                + KEY_EVIDENCE_URL + " TEXT" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PERIODS_TABLE);
        db.execSQL(CREATE_SYMPTOMS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_MOODS_TABLE);
        db.execSQL(CREATE_REMINDERS_TABLE);
        db.execSQL(CREATE_NUTRITION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_AGE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_DATE_OF_BIRTH + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_BLOOD_GROUP + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_CITY + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_EMERGENCY_NOTES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_WEIGHT + " REAL DEFAULT 0.0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_HEIGHT + " REAL DEFAULT 0.0");

            db.execSQL("ALTER TABLE " + TABLE_PERIODS + " ADD COLUMN " + KEY_FLOW_LEVEL + " TEXT DEFAULT 'MEDIUM'");
            db.execSQL("ALTER TABLE " + TABLE_PERIODS + " ADD COLUMN " + KEY_PAIN_SCALE + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PERIODS + " ADD COLUMN " + KEY_IS_SYNCED + " INTEGER DEFAULT 0");

            db.execSQL("ALTER TABLE " + TABLE_SYMPTOMS + " ADD COLUMN " + KEY_IS_SYNCED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_IS_SYNCED + " INTEGER DEFAULT 0");

            String CREATE_MOODS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MOODS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_USER_EMAIL + " TEXT, "
                    + KEY_MOOD_TYPE + " TEXT, "
                    + KEY_INTENSITY + " INTEGER DEFAULT 3, "
                    + KEY_LOGGED_DATE + " TEXT, "
                    + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";
            db.execSQL(CREATE_MOODS_TABLE);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_DIAGNOSED_CONDITIONS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_HEALTH_PREFERENCES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_SYMPTOMS + " ADD COLUMN " + KEY_SEVERITY + " TEXT DEFAULT 'MODERATE'");
        }
        if (oldVersion < 5) {
            String CREATE_REMINDERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_REMINDERS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_USER_EMAIL + " TEXT, "
                    + KEY_REMINDER_TITLE + " TEXT, "
                    + KEY_REMINDER_CATEGORY + " TEXT, "
                    + KEY_REMINDER_TIME + " INTEGER, "
                    + KEY_REMINDER_ENABLED + " INTEGER DEFAULT 1, "
                    + KEY_IS_SYNCED + " INTEGER DEFAULT 0" + ")";
            db.execSQL(CREATE_REMINDERS_TABLE);
        }
        if (oldVersion < 6) {
            String CREATE_NUTRITION_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NUTRITION_CACHE + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_CYCLE_PHASE + " TEXT, "
                    + KEY_MEAL_CATEGORY + " TEXT, "
                    + KEY_FOOD_ITEM_NAME + " TEXT, "
                    + KEY_KEY_NUTRIENTS + " TEXT, "
                    + KEY_HEALTH_BENEFITS + " TEXT, "
                    + KEY_DIETARY_TYPE + " TEXT, "
                    + KEY_BUDGET_LEVEL + " TEXT, "
                    + KEY_EVIDENCE_LEVEL + " TEXT, "
                    + KEY_SAFETY_WARNINGS + " TEXT, "
                    + KEY_EVIDENCE_URL + " TEXT" + ")";
            db.execSQL(CREATE_NUTRITION_TABLE);
        }
    }

    public static final class LoginUserUpdate {
        private final String name;
        private final String email;
        private final String password;
        private final int age;

        public LoginUserUpdate(String name, String email, String password, int age) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public int getAge() {
            return age;
        }

        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            values.put(KEY_EMAIL, email);
            values.put(KEY_PASSWORD, password);
            values.put(KEY_AGE, age);
            return values;
        }
    }

    public static LoginUserUpdate buildUserUpdateForLogin(String name, String email, String password, int age,
                                                         String existingPassword, int existingAge) {
        String finalPassword = (password == null || password.isEmpty()) ? existingPassword : password;
        int finalAge = age > 0 ? age : existingAge;
        return new LoginUserUpdate(name, email, finalPassword, finalAge);
    }

    // Insert User
    public boolean insertUser(String name, String email, String password) {
        return insertUser(name, email, password, 0);
    }

    public boolean insertUser(String name, String email, String password, int age) {
        SQLiteDatabase db = this.getWritableDatabase();

        String existingPassword = "";
        int existingAge = 0;
        String[] projection = {KEY_PASSWORD, KEY_AGE};
        Cursor existingUser = db.query(TABLE_USERS, projection, KEY_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (existingUser != null) {
            if (existingUser.moveToFirst()) {
                int passwordIndex = existingUser.getColumnIndex(KEY_PASSWORD);
                int ageIndex = existingUser.getColumnIndex(KEY_AGE);
                if (passwordIndex != -1) {
                    existingPassword = existingUser.getString(passwordIndex);
                }
                if (ageIndex != -1) {
                    existingAge = existingUser.getInt(ageIndex);
                }
            }
            existingUser.close();
        }

        LoginUserUpdate loginUserUpdate = buildUserUpdateForLogin(name, email, password, age, existingPassword, existingAge);
        ContentValues values = loginUserUpdate.toContentValues();
        int updatedRows = db.update(TABLE_USERS, values, KEY_EMAIL + " = ?", new String[]{email});
        if (updatedRows > 0) {
            return true;
        }

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean updateUser(String email, String name, int age) {
        return updateUserProfile(email, name, age, null, null, null, null, -1, -1, null, null);
    }

    public boolean updateUserProfile(String email, String name, int age, String dob, String bloodGroup, String city, String emergencyNotes, double weight, double height) {
        return updateUserProfile(email, name, age, dob, bloodGroup, city, emergencyNotes, weight, height, null, null);
    }

    public boolean updateUserProfile(String email, String name, int age, String dob, String bloodGroup, String city, String emergencyNotes, double weight, double height, String conditions, String preferences) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (name != null) values.put(KEY_NAME, name);
        if (age >= 0) values.put(KEY_AGE, age);
        if (dob != null) values.put(KEY_DATE_OF_BIRTH, dob);
        if (bloodGroup != null) values.put(KEY_BLOOD_GROUP, bloodGroup);
        if (city != null) values.put(KEY_CITY, city);
        if (emergencyNotes != null) values.put(KEY_EMERGENCY_NOTES, emergencyNotes);
        if (weight >= 0) values.put(KEY_WEIGHT, weight);
        if (height >= 0) values.put(KEY_HEIGHT, height);
        if (conditions != null) values.put(KEY_DIAGNOSED_CONDITIONS, conditions);
        if (preferences != null) values.put(KEY_HEALTH_PREFERENCES, preferences);

        int count = db.update(TABLE_USERS, values, KEY_EMAIL + " = ?", new String[]{email});
        if (count == 0) {
            values.put(KEY_EMAIL, email);
            long res = db.insert(TABLE_USERS, null, values);
            return res != -1;
        }
        return count > 0;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_ID};
        String selection = KEY_EMAIL + " = ? AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }

    public Cursor getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        return db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
    }

    // Periods
    public boolean savePeriod(String userEmail, long periodDate) {
        return savePeriod(userEmail, periodDate, "MEDIUM", 0, false);
    }

    public boolean savePeriod(String userEmail, long periodDate, String flowLevel, int painScale, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_PERIOD_DATE, periodDate);
        values.put(KEY_FLOW_LEVEL, flowLevel != null ? flowLevel : "MEDIUM");
        values.put(KEY_PAIN_SCALE, painScale);
        values.put(KEY_IS_SYNCED, isSynced ? 1 : 0);

        long result = db.insert(TABLE_PERIODS, null, values);
        return result != -1;
    }

    public boolean updatePeriod(long id, String userEmail, long periodDate, String flowLevel, int painScale) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PERIOD_DATE, periodDate);
        values.put(KEY_FLOW_LEVEL, flowLevel);
        values.put(KEY_PAIN_SCALE, painScale);
        values.put(KEY_IS_SYNCED, 0);

        int count = db.update(TABLE_PERIODS, values, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public boolean deletePeriod(long id, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_PERIODS, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public Cursor getPeriod(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_PERIODS, null, selection, selectionArgs, null, null, KEY_PERIOD_DATE + " DESC");
    }

    public Cursor getUnsyncedPeriods(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ? AND " + KEY_IS_SYNCED + " = 0";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_PERIODS, null, selection, selectionArgs, null, null, KEY_PERIOD_DATE + " DESC");
    }

    // Symptoms
    public boolean saveSymptom(String userEmail, String symptom, String createdAt) {
        return saveSymptom(userEmail, symptom, "MODERATE", createdAt, false);
    }

    public boolean saveSymptom(String userEmail, String symptom, String severity, String createdAt, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_SYMPTOM, symptom);
        values.put(KEY_SEVERITY, severity != null ? severity : "MODERATE");
        values.put(KEY_CREATED_AT, createdAt);
        values.put(KEY_IS_SYNCED, isSynced ? 1 : 0);

        long result = db.insert(TABLE_SYMPTOMS, null, values);
        return result != -1;
    }

    public boolean deleteSymptom(long id, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_SYMPTOMS, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public Cursor getSymptoms(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_SYMPTOMS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    public Cursor getUnsyncedSymptoms(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ? AND " + KEY_IS_SYNCED + " = 0";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_SYMPTOMS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    // Contacts
    public boolean saveContact(String userEmail, String contactName, String contactPhone) {
        return saveContact(userEmail, contactName, contactPhone, false);
    }

    public boolean saveContact(String userEmail, String contactName, String contactPhone, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_CONTACT_NAME, contactName);
        values.put(KEY_CONTACT_PHONE, contactPhone);
        values.put(KEY_IS_SYNCED, isSynced ? 1 : 0);

        long result = db.insert(TABLE_CONTACTS, null, values);
        return result != -1;
    }

    public Cursor getContacts(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_CONTACTS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    public Cursor getUnsyncedContacts(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ? AND " + KEY_IS_SYNCED + " = 0";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_CONTACTS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    // Moods
    public boolean saveMood(String userEmail, String moodType, int intensity, String loggedDate, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_MOOD_TYPE, moodType);
        values.put(KEY_INTENSITY, intensity);
        values.put(KEY_LOGGED_DATE, loggedDate);
        values.put(KEY_IS_SYNCED, isSynced ? 1 : 0);

        long result = db.insert(TABLE_MOODS, null, values);
        return result != -1;
    }

    public boolean deleteMood(long id, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_MOODS, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public Cursor getMoods(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_MOODS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    public Cursor getUnsyncedMoods(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ? AND " + KEY_IS_SYNCED + " = 0";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_MOODS, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
    }

    // Reminders Table CRUD
    public boolean saveReminder(String userEmail, String title, String category, long timeMillis, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_REMINDER_TITLE, title);
        values.put(KEY_REMINDER_CATEGORY, category);
        values.put(KEY_REMINDER_TIME, timeMillis);
        values.put(KEY_REMINDER_ENABLED, isEnabled ? 1 : 0);
        values.put(KEY_IS_SYNCED, 0);

        long result = db.insert(TABLE_REMINDERS, null, values);
        return result != -1;
    }

    public boolean updateReminder(long id, String userEmail, boolean isEnabled, long timeMillis) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REMINDER_ENABLED, isEnabled ? 1 : 0);
        if (timeMillis > 0) values.put(KEY_REMINDER_TIME, timeMillis);
        values.put(KEY_IS_SYNCED, 0);

        int count = db.update(TABLE_REMINDERS, values, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public boolean deleteReminder(long id, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_REMINDERS, KEY_ID + " = ? AND " + KEY_USER_EMAIL + " = ?", new String[]{String.valueOf(id), userEmail});
        return count > 0;
    }

    public Cursor getReminders(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_REMINDERS, null, selection, selectionArgs, null, null, KEY_REMINDER_TIME + " ASC");
    }

    // Nutrition Cache
    public void saveNutritionItemToCache(long id, String cyclePhase, String mealCategory, String foodName, String keyNutrients, String healthBenefits, String dietaryType, String budgetLevel, String evidenceLevel, String safetyWarnings, String evidenceUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_CYCLE_PHASE, cyclePhase);
        values.put(KEY_MEAL_CATEGORY, mealCategory);
        values.put(KEY_FOOD_ITEM_NAME, foodName);
        values.put(KEY_KEY_NUTRIENTS, keyNutrients);
        values.put(KEY_HEALTH_BENEFITS, healthBenefits);
        values.put(KEY_DIETARY_TYPE, dietaryType);
        values.put(KEY_BUDGET_LEVEL, budgetLevel);
        values.put(KEY_EVIDENCE_LEVEL, evidenceLevel);
        values.put(KEY_SAFETY_WARNINGS, safetyWarnings);
        values.put(KEY_EVIDENCE_URL, evidenceUrl);

        db.insertWithOnConflict(TABLE_NUTRITION_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getCachedNutritionRecommendations(String phase, String dietaryType, String budgetLevel) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder("1=1");
        java.util.List<String> args = new java.util.ArrayList<>();

        if (phase != null && !phase.isEmpty() && !"ALL".equalsIgnoreCase(phase)) {
            selection.append(" AND (UPPER(").append(KEY_CYCLE_PHASE).append(") = ? OR UPPER(").append(KEY_CYCLE_PHASE).append(") = 'GENERAL')");
            args.add(phase.toUpperCase());
        }

        if (dietaryType != null && !dietaryType.isEmpty() && !"ALL".equalsIgnoreCase(dietaryType)) {
            selection.append(" AND (UPPER(").append(KEY_DIETARY_TYPE).append(") = ? OR UPPER(").append(KEY_DIETARY_TYPE).append(") = 'ALL')");
            args.add(dietaryType.toUpperCase());
        }

        if (budgetLevel != null && !budgetLevel.isEmpty() && !"ALL".equalsIgnoreCase(budgetLevel)) {
            selection.append(" AND UPPER(").append(KEY_BUDGET_LEVEL).append(") = ?");
            args.add(budgetLevel.toUpperCase());
        }

        return db.query(TABLE_NUTRITION_CACHE, null, selection.toString(), args.toArray(new String[0]), null, null, KEY_ID + " ASC");
    }

    // Mark synced
    public void markPeriodsSynced(String userEmail) {
        updateSyncedFlag(TABLE_PERIODS, userEmail);
    }

    public void markSymptomsSynced(String userEmail) {
        updateSyncedFlag(TABLE_SYMPTOMS, userEmail);
    }

    public void markContactsSynced(String userEmail) {
        updateSyncedFlag(TABLE_CONTACTS, userEmail);
    }

    public void markMoodsSynced(String userEmail) {
        updateSyncedFlag(TABLE_MOODS, userEmail);
    }

    public void markAllSynced(String userEmail) {
        markPeriodsSynced(userEmail);
        markSymptomsSynced(userEmail);
        markContactsSynced(userEmail);
        markMoodsSynced(userEmail);
    }

    private void updateSyncedFlag(String tableName, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_SYNCED, 1);
        db.update(tableName, values, KEY_USER_EMAIL + " = ?", new String[]{userEmail});
    }
}
