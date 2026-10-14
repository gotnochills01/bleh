# Mahila Android Project - Progress & Architecture Document

## Project Status Overview
- **Build Status**:
  - Android Debug APK: **BUILD SUCCESSFUL** (`./gradlew assembleDebug`)
  - Android Unit Tests: **PASSED** (`./gradlew test`)
  - Spring Boot Backend: **BUILD SUCCESSFUL** (`mvn clean test -f backend/pom.xml`)
- **Launch Flow Status**: **VERIFIED & POLISHED**
  - Launch Activity: `LandingActivity` (`android.intent.action.MAIN` / `LAUNCHER`).
  - Route 1 (Logged-in User): Direct forward to `MainActivity` (BottomNavigation + Drawer).
  - Route 2 (New User / Unauthenticated): `LandingActivity` -> `LoginActivity` / `SignupActivity` -> `WelcomeActivity` -> `MainActivity`.
  - Route 3 (Guest Mode): `LandingActivity` -> `LoginActivity` (Guest Button) -> `MainActivity` (Offline SQLite Mode).

---

## Technical Audit & Phase Checklist Matrix

### Phase 1: Foundation, Audit & Complete UI (COMPLETED)
- [x] **Project Audit**: Every Android Activity, Fragment, XML Layout, SQLite helper, and Spring Boot service audited and validated.
- [x] **Design System Establishment**:
  - Color Palette: Warm Ivory (`#FFFDF9`), Deep Plum (`#3B1F2B`), Soft Muted Rose (`#D87093`), Sage Green (`#7DA183`), Warm Sand (`#F5EBE6`).
  - Typography: Nunito typeface hierarchy (`@font/nunito`).
  - Icons: M3 Vector drawables (`ic_person`, `ic_ai_sparkle`, `ic_nav_tracker`, `ic_lock`, `ic_email`). Emojis removed from XML layouts.
- [x] **Authentication & Onboarding Flow**:
  - Polished M3 layouts for `LandingActivity`, `LoginActivity`, `SignupActivity`, and `WelcomeActivity`.
  - Session restoration via `TokenManager` and `AppPreferences`. Guest mode offline fallback.
- [x] **Technical & Offline Foundation**:
  - Database Helper v3 schema with full user profile storage, `moods` table, and `is_synced` status flags.
  - User-data isolation strictly enforced by `WHERE user_email = ?`.
  - `SQLiteToMySQLMigrator` sync engine optimized to extract strictly unsynced records (`is_synced = 0`).

### Phase 2: Menstrual Tracking & Personalized Health Guidance (COMPLETED)
- [x] **Cycle & Prediction Engine**:
  - `CycleCalculator.java` updated to calculate average cycle length, variance/irregularity flag ("Irregular Cycle"), and next period prediction based strictly on user's actual historical period dates.
  - Honest empty/insufficient history state handling ("No period recorded yet. Log your first period to unlock cycle predictions").
- [x] **Full Period, Symptom & Mood CRUD**:
  - Database Helper v4 schema adding `updatePeriod`, `deletePeriod`, `deleteSymptom`, `deleteMood`.
  - `TrackerFragment` renders actual period history list with dynamic deletion and empty states.
  - `SymptomLoggerActivity` supports multi-symptom selection, severity rating, and history viewing.
- [x] **Personalized Health Profile & Diagnosed Conditions**:
  - `ProfileFragment` updated to store DOB/age, height, weight, blood group, city, emergency medical notes, and diagnosed conditions (PCOS, Endometriosis, Irregular Cycles, Thyroid).
- [x] **Stage Guidance & Medical Disclaimers**:
  - `AwarenessFragment` updated with educational categories for Adolescence, Reproductive 20s/30s, Perimenopause/Menopause, PCOS/Endometriosis, and Hygiene.
  - Explicit medical safety disclaimers included ("Educational guidance only; not medical diagnosis/prescription").
- [x] **Connected Dashboard Integration**:
  - `HomeFragment` calculates cycle day/phase from stored SQLite periods and displays diagnosed conditions badges.

### Phase 3: Real Maps, Medical Assistance & Emergency Calling (COMPLETED)
- [x] **Emergency Contact Management & Real Phone Calling**:
  - `EmergencyContactsActivity` updated with phone number validation (10-15 digits), duplicate prevention, edit/delete actions, and user-email isolation.
  - Integrated `ACTION_DIAL` intent (`buildDialUriString`) for dialing personal emergency contacts and national emergency helplines (112, 1091, 108).
- [x] **Medical Assistance & Category Maps Search**:
  - `AssistanceFragment` and `MedicalAssistanceActivity` updated to route category searches (Gynecologists, Hospitals, Clinics, Pharmacies, Women Centers) via `geo:0,0?q=` map intents with web fallback (`https://www.google.com/maps/search/?api=1&query=`).
  - Added `EmergencyContactsTest.java` verifying `tel:` URI construction and number formatting.

### Phase 4: Real Android Notifications and Reminders (COMPLETED)
- [x] **Database Schema Upgrade (v5)**:
  - Added `reminders` table (`id`, `user_email`, `title`, `category`, `time_millis`, `is_enabled`, `is_synced`) to `DatabaseHelper.java`.
- [x] **AlarmManager & Background Scheduling**:
  - Created `NotificationReceiver.java` and `ReminderScheduler.java` handling background alarms, exact alarm permissions, and M3 notification channels (`mahila_period_channel`, `mahila_meds_channel`).
  - Declared `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, and `RECEIVE_BOOT_COMPLETED` permissions in `AndroidManifest.xml`.
- [x] **Reminder Management UI**:
  - Created `RemindersActivity.java` and `activity_reminders.xml` providing toggle switches for period start alerts and medication reminder scheduling.

### Phase 5: MySQL, Spring Boot & End-to-End Integration (COMPLETED)
- [x] **MySQL Schema Synchronization (`schema.sql`)**:
  - Added `diagnosed_conditions` and `health_preferences` columns to `health_profile` table in MySQL schema.
  - Confirmed constraints and indexes across `users`, `health_profile`, `periods`, `symptoms`, `moods`, `contacts`, `appointments`, and `reminders`.
- [x] **Spring Boot REST API Enhancements**:
  - Updated `MigrationSyncPayload.java` and `HealthProfile.java` model to include diagnosed conditions, city, blood group, and emergency notes.
  - Updated `MigrationService.java` to support idempotent mood syncing and health profile updates.
  - Added `BackendIntegrationTest.java` in backend test suite testing auth DTOs and migration sync payload serialization.
- [x] **End-to-End Android Sync Bridge**:
  - Updated `SQLiteToMySQLMigrator.java` to extract unsynced local records (`is_synced = 0`), payload diagnosed conditions and moods, and update sync flags upon successful HTTP response.

---

## Local MySQL & Spring Boot Backend Setup Instructions

### 1. Database Setup
```sql
CREATE DATABASE IF NOT EXISTS mahila_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'mahila_user'@'localhost' IDENTIFIED BY 'mahila_secure_password';
GRANT ALL PRIVILEGES ON mahila_db.* TO 'mahila_user'@'localhost';
FLUSH PRIVILEGES;
```
Import `backend/src/main/resources/schema.sql` into MySQL.

### 2. Spring Boot Configuration
Set environment variables or update `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mahila_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=mahila_user
spring.datasource.password=mahila_secure_password
mahila.jwt.secret=9a4f2c8d7e1b3a5c8e0d2f4a6b8c1d3e5f7a9b0c2d4e6f8a1c3b5d7e9f0a2b4
mahila.jwt.expiration-ms=86400000
```
Run backend server:
```bash
mvn spring-boot:run -f backend/pom.xml
```

---

## Verification & Build Results
1. **Android Debug Build**: Executed `./gradlew assembleDebug --no-daemon`. Result: `BUILD SUCCESSFUL`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: `BUILD SUCCESSFUL` (All test suites passed).
3. **Spring Boot Backend Test Suite**: Executed `mvn test -f backend/pom.xml`. Result: `BUILD SUCCESSFUL` (`BackendIntegrationTest` passed with 0 failures).

---

## Remaining Roadmap by Phase

### Phase 6: Final QA Audit, Documentation & Security Hardening (NEXT PHASE)
- Comprehensive end-to-end user regression audit, code cleanup, and production-ready security check.

---

## Files Modified
- `backend/src/main/resources/schema.sql`
- `backend/src/main/java/com/mahila/model/HealthProfile.java`
- `backend/src/main/java/com/mahila/dto/MigrationSyncPayload.java`
- `backend/src/main/java/com/mahila/service/MigrationService.java`
- `app/src/main/java/com/example/mahila/utils/SQLiteToMySQLMigrator.java`
- `backend/src/test/java/com/mahila/BackendIntegrationTest.java`
- `MAHILA_PROGRESS.md`
