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

## Physical Android Phone (Vivo / LAN) Testing Guide

1. **Host Server Binding**: Ensure Spring Boot backend is running on host laptop on port 8080.
2. **Wi-Fi Connectivity**: Connect both laptop and physical Vivo Android phone to the same local Wi-Fi network.
3. **Retrieve Host LAN IP**:
   * Windows: `ipconfig` (look for IPv4 Address e.g., `192.168.1.100`)
   * Mac/Linux: `ifconfig` or `ip a`
4. **Configure App Host Base URL**:
   In `MainActivity.java` or Application initialization, configure host IP:
   ```java
   com.example.mahila.api.ApiClient.setCustomBaseUrl("http://192.168.1.100:8080/");
   ```
5. **Firewall Rule**: Allow incoming TCP port `8080` connections through host operating system firewall.

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
