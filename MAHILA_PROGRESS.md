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

## Technical Diagnostics: Android Network Connection & Login Error Handling

### 1. Root Cause Analysis
- **Error Stream Reading**: `ApiClient.java` previously did not inspect `conn.getErrorStream()` on non-2xx HTTP responses. When Spring Boot returned an HTTP 400 (Bad Request), 401 (Unauthorized), or 403 (Forbidden), `conn.getInputStream()` threw an `IOException`, which was caught as a generic connection error.
- **Connection Cleanup**: `ApiClient` lacked a `finally { conn.disconnect(); }` block, causing socket connections to remain open during server errors.
- **UI Button Recovery**: `LoginActivity.java` previously disabled `btnLogin` on click without guaranteeing its restoration in all JSON processing error paths.

### 2. Resolution Implemented
- **`ApiClient.java` Diagnostics Refactoring**:
  - Implemented `readStream(InputStream)` to read both success and error streams (`conn.getErrorStream()`).
  - Added `formatHttpErrorMessage()` to parse Spring Boot's JSON error response (`{"message": "..."}`) and return specific error descriptions (e.g., `Server Error (401): Invalid email or password`).
  - Enforced 10-second connect (`CONNECT_TIMEOUT_MS = 10000`) and read (`READ_TIMEOUT_MS = 10000`) timeouts.
  - Added URL normalization via `buildFullUrl()`.
  - Added guaranteed `conn.disconnect()` execution in a `finally` block.
  - Safely logged HTTP request URLs and status codes using Android `Log.d` / `Log.e` without exposing passwords or tokens.
- **`LoginActivity.java` UI State & Security**:
  - Guaranteed `btnLogin.setEnabled(true); btnLogin.setText("Login");` is executed in `onSuccess`, `onError`, and exception handlers.
  - Enforced server-side authentication without storing raw passwords locally in SQLite or `SharedPreferences`.
  - Enhanced Guest Mode login by calling `TokenManager.clear()` followed by `saveSession(null, null, "Guest", "Guest", "en")` to purge stale JWT tokens or user IDs.

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

## Verification & Build Results
1. **Compilation / Android Debug APK**: Executed `./gradlew assembleDebug --no-daemon`. Result: **BUILD SUCCESSFUL**. Generated APK: `app/build/outputs/apk/debug/app-debug.apk`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: **PASSED 100%** (`RemindersTest`, `EmergencyContactsTest`, `CycleCalculatorTest`, `Phase2HealthTrackingTest`, `DatabaseHelperTest`, `Phase1VerificationTest` all passed).
3. **Spring Boot Backend Test Suite**: Executed `mvn test -f backend/pom.xml`. Result: **BUILD SUCCESSFUL** (`BackendIntegrationTest` passed with 0 failures).

---

## Files Modified in Diagnostic Batch
- `app/src/main/java/com/example/mahila/api/ApiClient.java`
- `app/src/main/java/com/example/mahila/LoginActivity.java`
- `app/src/main/java/com/example/mahila/api/TokenManager.java`
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `MAHILA_PROGRESS.md`
