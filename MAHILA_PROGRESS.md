# Mahila Android Project - Progress & Architecture Document

## Project Status Overview
- **Build Status**:
  - Android Debug APK: **BUILD SUCCESSFUL** (`./gradlew assembleDebug`)
    - APK Location: `app/build/outputs/apk/debug/app-debug.apk`
  - Android Unit Tests: **PASSED 100%** (`./gradlew test`)
  - Spring Boot Backend: **BUILD SUCCESSFUL** (`mvn clean test -f backend/pom.xml`)
- **Launch Flow Status**: **VERIFIED & POLISHED**
  - Launch Activity: `LandingActivity` (`android.intent.action.MAIN` / `LAUNCHER`).
  - Route 1 (Logged-in User): Direct forward to `MainActivity` (BottomNavigation + Drawer).
  - Route 2 (New User / Unauthenticated): `LandingActivity` -> `LoginActivity` / `SignupActivity` -> `WelcomeActivity` -> `MainActivity`.
  - Route 3 (Guest Mode): `LandingActivity` -> `LoginActivity` (Guest Button) -> `MainActivity` (Guest Mode, Stale JWT Cleared).

---

## Group 3: UI Alignment & Readability Enhancements

### 1. Screens & Layouts Refactored
- **AI Health Assistant (`activity_ai_chat.xml`)**: Applied Warm Ivory (`#FFFDF9`) canvas, M3 outlined text input box with `56dp` touch target height, `48dp` send button, and `clipToPadding="false"` message scrolling container.
- **Menstrual Health Quiz (`activity_quiz.xml`)**: Updated container padding to `20dp`, Nunito typography for question prompts, `16dp` M3 card corner radius with Warm Sand stroke, and `56dp` submit button touch targets.
- **Symptom Logger (`activity_symptom_logger.xml`)**: Standardized chip group spacing (`app:chipSpacingHorizontal="8dp"` and `app:chipSpacingVertical="8dp"`), `56dp` save button height, and Warm Sand history card container.
- **Health Tips (`activity_health_tips.xml`)**: Standardized toolbar title with navigation insets, `16dp` M3 card corner radii with `2dp` elevation, and `56dp` action button touch targets.
- **Settings & Preferences (`activity_settings.xml`)**: Standardized M3 switch containers, language dropdown spinner, and `56dp` touch target heights for `btn_apply_language`, `btn_reminders`, `btn_export_data`, and `btn_logout`.
- **Reminders & Alerts (`activity_reminders.xml`)**: Standardized M3 switch controls, medication text input, and `56dp` scheduling button.

---

## Group 2: GPS, Maps, Nearby Assistance & Emergency Calling

### 1. Features Implemented & Resolved
- **GPS Permission & Rationale Handling (`AssistanceFragment.java`)**:
  - Implemented runtime checks for `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`.
  - Added user permission rationale dialogs explaining GPS usage for discovering nearby healthcare facilities.
  - Added a permanently denied dialog option directing users straight to App Info Settings (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`) while allowing manual city search fallback.
- **Location Detection & User States**:
  - Automatically detects GPS/Network location, updates map camera position, and checks hardware GPS provider status.
  - Displays user-friendly error/loading messages when GPS is disabled or when location/network access fails (`tv_provider_results_subtitle`).
- **Map Display & Provider Search**:
  - Renders Google Map markers (`MarkerOptions`) and card list views (`container_provider_results`) for nearby Gynecologists, Hospitals, Clinics, Women Centers, and Pharmacies via real-time OpenStreetMap Overpass API queries.
  - Displays distance calculation in kilometers, directions launching intent, and location sharing sheet link (`ACTION_SEND`).
- **Emergency Calling Safety Compliance (`EmergencyContactsActivity.java`)**:
  - Refactored emergency contacts and healthcare provider call actions to execute exclusively via `Intent.ACTION_DIAL` with pre-filled `tel:number` URIs.
  - Prevents automatic phone calls, leaving final placement to explicit user confirmation in the native phone dialer app.
  - Preserves full emergency contact CRUD (add, edit, delete, 10–15 digit validation, duplicate check) with local SQLite `DatabaseHelper`.

---

## Authentication, Session Management & Physical Phone Fixes

### 1. Root Causes Found
1. **Existing Account Login Error on Physical Devices**: `LoginActivity` attempted HTTP requests exclusively to `10.0.2.2:8080`, which is valid only for Android Emulator. On physical phones or offline environments, network connection failed without checking saved local SQLite credentials.
2. **Repeated Taps on Signup/Login**: Neither `LoginActivity` nor `SignupActivity` disabled action buttons or displayed loading states during async network calls. Rapid double-tapping caused duplicate requests, triggering "Email already registered" error on second submission.
3. **Save/Update Redirecting to Launch Screen**: Mismatch between `TokenManager` (`jwt_token`, `user_email`) and `AppPreferences` (`is_logged_in`, `current_user_email`). When saving a period date or profile detail, `AppPreferences.isLoggedIn()` returned false or empty email if unsynced, causing app restart on `LandingActivity`.

### 2. Solutions Implemented
- **Dynamic LAN IP Configuration (`ApiClient.java`)**: Added `setCustomBaseUrl("http://<YOUR_LAN_IP>:8080/")` support for physical phone testing over local Wi-Fi while preserving default emulator `10.0.2.2:8080` fallback.
- **Button Loading States & Double-Tap Prevention**: `LoginActivity.java` and `SignupActivity.java` disable buttons and show progress text ("Logging in...", "Creating Account...") during network calls, re-enabling them cleanly in success/failure callbacks.
- **Local SQLite Auth Fallback**: `LoginActivity.java` checks saved SQLite user credentials if backend is unreachable (offline mode), keeping physical phone users logged in seamlessly.
- **Unified Session Persistence**: Synchronized `TokenManager` and `AppPreferences` across login, signup, guest mode, and data saves. Ensured period and profile update methods in `DatabaseHelper` never erase session tokens.

---

## Technical Diagnostics & Security Refinements

### 1. Connection Handling & Diagnostics
- `ApiClient.java` inspects `conn.getErrorStream()` on non-2xx HTTP responses, formatting HTTP 400/401/403/500 errors gracefully.
- `ApiClient` enforces `finally { conn.disconnect(); }` execution to prevent socket leaks.

### 2. Authentication & Data Protection
- Enforced server-side authentication with JWT tokens without storing plaintext passwords in SQLite or `SharedPreferences`.
- Cleared stale JWT tokens in guest mode via `TokenManager.clear()` and isolated guest profile sessions.

---

## Verification & Build Results
1. **Compilation / Android Debug APK**: Executed `./gradlew assembleDebug --no-daemon`. Result: **BUILD SUCCESSFUL**. Generated APK: `app/build/outputs/apk/debug/app-debug.apk`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: **PASSED 100%** (`AuthSessionTest`, `AnalyticsEngineTest`, `NutritionTest`, `RemindersTest`, `EmergencyContactsTest`, `CycleCalculatorTest`, `Phase2HealthTrackingTest`, `DatabaseHelperTest`, `Phase1VerificationTest` all passed).
3. **Spring Boot Backend Test Suite**: Executed `mvn test -f backend/pom.xml`. Result: **BUILD SUCCESSFUL** (`BackendIntegrationTest` passed with 0 failures).
