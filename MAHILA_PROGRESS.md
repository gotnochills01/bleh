# Mahila Android Project - Progress & Architecture Document

## Project Status Overview
- **Build Status**:
  - Android Debug APK: **BUILD SUCCESSFUL** (`./gradlew assembleDebug`)
    - APK Location: `app/build/outputs/apk/debug/app-debug.apk`
  - Android Unit Tests: **PASSED** (`./gradlew test`)
  - Spring Boot Backend: **BUILD SUCCESSFUL** (`mvn clean test -f backend/pom.xml`)
- **Launch Flow Status**: **VERIFIED & POLISHED**
  - Launch Activity: `LandingActivity` (`android.intent.action.MAIN` / `LAUNCHER`).
  - Route 1 (Logged-in User): Direct forward to `MainActivity` (BottomNavigation + Drawer).
  - Route 2 (New User / Unauthenticated): `LandingActivity` -> `LoginActivity` / `SignupActivity` -> `WelcomeActivity` -> `MainActivity`.
  - Route 3 (Guest Mode): `LandingActivity` -> `LoginActivity` (Guest Button) -> `MainActivity` (Offline SQLite Mode).

---

## UI Redesign & Modernization Progress (Incremental Screen Redesign)

### Completed Screen 1: Home Dashboard (`HomeFragment` / `fragment_home.xml`)
- [x] **Design System Implementation**:
  - Palette: Warm Ivory (`#FFFDF9` background), Deep Plum (`#3B1F2B` primary text/titles), Soft Muted Rose (`#D87093` cycle accent), Sage Green (`#7DA183` tertiary accent), Warm Sand (`#FBF5F1` / `#F5EBE6` surface containers).
  - Typography: Nunito font hierarchy applied (`@font/nunito`) across title (26sp bold), main display text (42sp bold), subheaders (18sp), and body (14sp).
- [x] **Layout & Readability Fixes**:
  - Eliminated text clipping, faint rose text, and overcrowded margins.
  - Set explicit touch targets for Quick Action buttons (56dp height) with Material3 tonal styling.
  - Styled AI Cycle Insight card with 4dp line spacing and high-contrast text on Warm Sand containers.
- [x] **Data Binding Preservation**:
  - Preserved genuine `CycleCalculator` logic calculating cycle day, phase, next period, fertile window, and profile condition notes from SQLite history without hardcoded mock data.

---

## Final QA Feature Audit Checklist Matrix

### A. Launch and Authentication
- [x] **Splash & Branding**: `LandingActivity` displays Mahila branding and onboarding visual.
- [x] **Authentication Flow**: Working Login, Signup, Welcome, Session Restoration, and Logout in `LoginActivity`, `SignupActivity`, `WelcomeActivity`, `TokenManager`, and `AppPreferences`.

### B. Period and Health Tracking
- [x] **Period CRUD & Calendar**: Period start logging, editing, and deletion supported in `TrackerFragment` with local SQLite persistence and backend API synchronization.
- [x] **Cycle Predictions**: `CycleCalculator` calculates cycle day, phase, average length, and variance based strictly on user records without fabricating dates.
- [x] **Symptom & Mood Logging**: Multi-symptom selection, severity rating, and mood journal tracking supported in `SymptomLoggerActivity` and `TrackerFragment`.
- [x] **Profile & Diagnosed Conditions**: Stores DOB, height, weight, blood group, city, emergency medical notes, and diagnosed conditions (PCOS, Endometriosis, Thyroid) in `ProfileFragment`.

### C. Medical Assistance & Embedded Maps Provider
- [x] **In-App Provider Search & Results**: `AssistanceFragment` embeds `SupportMapFragment` with real OpenStreetMap Overpass query execution, marker plotting, and category filtering.
- [x] **Location Permission & Manual City Input**: `AssistanceFragment` requests `ACCESS_FINE_LOCATION` and falls back to manual city search (`et_location_query`) when permission is declined.
- [x] **Directions & Direct Calling**: Launches map intents (`geo:`) and direct dialer calls (`ACTION_DIAL`).

### D. Emergency Contacts and Actual Phone Calling
- [x] **Contact CRUD & Validation**: `EmergencyContactsActivity` validates 10–15 digit phone numbers, prevents duplicate numbers, isolates data per user, and supports add/edit/delete.
- [x] **Direct Phone Calling**: `ACTION_DIAL` intent (`buildDialUriString`) used for personal contacts and national helplines (112, 1091, 108).

### E. Real Android Notifications and Reminders
- [x] **Background Alarm Manager**: `ReminderScheduler.java` handles `AlarmManager` exact/inexact alerts and notification channels (`mahila_period_channel`, `mahila_meds_channel`).
- [x] **Boot Receiver & Permissions**: `NotificationReceiver.java` handles silent alarm rescheduling on boot (`ACTION_BOOT_COMPLETED`) without posting false banners. Declares `POST_NOTIFICATIONS` and `SCHEDULE_EXACT_ALARM`.

### F. Health Tips, AI Assistant, Quiz & Settings
- [x] **Interactive Educational Content**: `AwarenessFragment` provides life stage guidance (Adolescents, Reproductive 20s/30s, Perimenopause/Menopause, PCOS/Endometriosis) and prominent medical disclaimers.
- [x] **Interactive Quiz**: `QuizActivity` provides cycle knowledge quizzes.
- [x] **Settings & Privacy Controls**: `SettingsActivity` manages multi-language preferences and session logout.

---

## Live MySQL & Spring Boot Backend Setup Instructions

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

## Google Cloud Maps Key Configuration Instructions

To enable Google Maps rendering in `AssistanceFragment`, configure a key as follows:
1. Enable **Maps SDK for Android** and **Places API** in the Google Cloud Console.
2. In `local.properties`, add:
   ```properties
   MAPS_API_KEY=AIzaSy...your_key...
   ```

---

## Verification & Build Results
1. **Compilation / Android Debug APK**: Executed `./gradlew assembleDebug --no-daemon`. Result: **BUILD SUCCESSFUL**. Generated APK: `app/build/outputs/apk/debug/app-debug.apk`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: **PASSED** (`RemindersTest`, `EmergencyContactsTest`, `CycleCalculatorTest`, `Phase2HealthTrackingTest`, `DatabaseHelperTest`, `Phase1VerificationTest` all passed).
3. **Spring Boot Backend Test Suite**: Executed `mvn test -f backend/pom.xml`. Result: **BUILD SUCCESSFUL** (`BackendIntegrationTest` passed with 0 failures).

---

## Files Modified in Latest Redesign Batch
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `MAHILA_PROGRESS.md`
