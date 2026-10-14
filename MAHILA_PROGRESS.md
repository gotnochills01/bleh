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
- **Phase 1: Local Offline & Network Resilience (VERIFIED & COMPLETED)**:
  - **Database Migration**: `DatabaseHelper` schema (v3) preserves existing user data (`onUpgrade` adds columns without dropping user tables).
  - **Multi-User Isolation**: All queries in `DatabaseHelper` strictly filter by `user_email`.
  - **Offline Fallback & Toast Status**: `ProfileFragment`, `TrackerFragment`, `HomeFragment`, and `AssistanceFragment` render local SQLite records when API endpoints are unreachable and display non-disruptive Toast messages.
  - **Selective Entity Syncing**: `TrackerFragment` only marks the specifically saved entity as synced (`markPeriodsSynced` / `markMoodsSynced`) rather than marking all user records synced.
  - **Sync Engine Optimization**: `SQLiteToMySQLMigrator` queries only unsynced records (`getUnsyncedPeriods`, `getUnsyncedSymptoms`, `getUnsyncedContacts`, `getUnsyncedMoods`) to prevent duplicate payloads or re-transmitting synced entries.

### 2. Incomplete / Pending Work
- **Phase 2: Local Notification Engine**:
  - Implement native Android background scheduling with `WorkManager` / `AlarmManager` for period and medical reminders.
- **Phase 3: Automated Unit & Integration Test Expansion**:
  - Expand test coverage for additional helper classes and API endpoints.

---

## Focused Verification Results (Phase 1)
1. **Schema Upgrade (v2 to v3)**: Verified migration preserves existing user data (`ALTER TABLE` statements add columns `date_of_birth`, `blood_group`, `city`, `emergency_notes`, `weight`, `height` without dropping `users`, `periods`, `symptoms`, or `contacts`).
2. **Multi-User Data Isolation**: Verified that `DatabaseHelper` queries constrain results by `user_email = ?`, isolating records between users.
3. **Offline Record Preservation**: Verified offline entry creation marks `is_synced = 0` until backend confirms successful HTTP 2xx response.
4. **Sync Engine Precision**: Verified `SQLiteToMySQLMigrator` retrieves strictly unsynced records (`is_synced = 0`).
5. **UI Fallback Display**: Verified `ProfileFragment` and `HomeFragment` render cached local data when `ApiClient` returns connection errors.

---

## Files Modified in Phase 1
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`
- `app/src/main/java/com/example/mahila/ui/tracker/TrackerFragment.java`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `app/src/main/java/com/example/mahila/utils/SQLiteToMySQLMigrator.java`
- `app/src/test/java/com/example/mahila/DatabaseHelperTest.java`
- `app/src/test/java/com/example/mahila/Phase1VerificationTest.java`
- `MAHILA_PROGRESS.md`
