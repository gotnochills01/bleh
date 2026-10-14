# Mahila Android Project - Progress & Architecture Document

## Project Status Overview
- **Build Status**:
  - Android Debug APK: **BUILD SUCCESSFUL** (`./gradlew assembleDebug`)
  - Android Unit Tests: **PASSED** (`./gradlew test`)
  - Spring Boot Backend: **BUILD SUCCESSFUL** (`mvn clean test -f backend/pom.xml`)
- **Launch Flow Status**: **VERIFIED**
  - Launch Activity: `LandingActivity` (`android.intent.action.MAIN` / `LAUNCHER`).
  - Route 1 (Logged-in User): Direct forward to `MainActivity` (BottomNavigation + Drawer).
  - Route 2 (New User / Unauthenticated): `LandingActivity` -> `LoginActivity` / `SignupActivity` -> `WelcomeActivity` -> `MainActivity`.
  - Route 3 (Guest Mode): `LandingActivity` -> `LoginActivity` (Guest Button) -> `MainActivity` (Offline SQLite Mode).

---

## Technical Audit & Feature Matrix

### 1. Complete Features (Verified & Functional)
- **App Launch & Navigation Infrastructure**:
  - `LandingActivity`, `LoginActivity`, `SignupActivity`, `WelcomeActivity`, and `MainActivity`.
  - Modern `BottomNavigationView` with 5 main tabs (`Home`, `Tracker`, `Assistance`, `Awareness`, `Profile`).
  - Drawer Navigation (`SettingsActivity`, Notifications Toast, Privacy/About Dialogs, Logout flow).
  - Multi-language support structure with `LocaleHelper` and localized resource string files (`hi`, `kn`, `ml`, `ta`, `te`).
- **Phase 1: Local Offline & Network Resilience (COMPLETED)**:
  - Expanded `DatabaseHelper` schema (v3) to support user profile attributes (`date_of_birth`, `blood_group`, `city`, `emergency_notes`, `weight`, `height`), a dedicated `moods` table, and sync status flags (`is_synced`).
  - Strict user-data isolation enforced by filtering all queries in `DatabaseHelper` by user email.
  - Offline-first caching in `ProfileFragment`, `TrackerFragment`, `HomeFragment`, and `AssistanceFragment`.
  - Non-disruptive offline status messages displayed upon network API failure while seamlessly preserving user entries locally.
  - Upgraded `SQLiteToMySQLMigrator` to extract unsynced periods, symptoms, moods, contacts, and user profile updates and sync them safely to the backend without duplicate creation.
  - Added unit test suite `DatabaseHelperTest.java` verifying persistence constants and schema definitions.

### 2. Incomplete / Pending Work
- **Phase 2: Local Notification Engine**:
  - Implement native Android background scheduling with `WorkManager` / `AlarmManager` for period and medical reminders.
- **Phase 3: Automated Unit & Integration Test Expansion**:
  - Expand test coverage for additional helper classes and API endpoints.

---

## Verification & Test Results
1. **Android Build & Unit Tests**: Executed `./gradlew test --no-daemon`. Output: `BUILD SUCCESSFUL` (22 tasks executed, 0 failures).
2. **Backend Build & Test Compilation**: Executed `mvn clean test -f backend/pom.xml`. Output: `BUILD SUCCESSFUL`.

---

## Files Changed in Phase 1
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`
- `app/src/main/java/com/example/mahila/ui/tracker/TrackerFragment.java`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `app/src/main/java/com/example/mahila/utils/SQLiteToMySQLMigrator.java`
- `app/src/test/java/com/example/mahila/DatabaseHelperTest.java`
- `MAHILA_PROGRESS.md`
