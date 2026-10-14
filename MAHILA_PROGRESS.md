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
- **Phase 1: Local Offline & Network Resilience (VERIFIED & COMPLETE)**:
  - **Database Migration**: `DatabaseHelper` schema (v3) preserves existing user data (`onUpgrade` adds columns without dropping user tables).
  - **Multi-User Isolation**: All queries in `DatabaseHelper` strictly filter by `user_email`.
  - **Offline Fallback & Toast Status**: `ProfileFragment`, `TrackerFragment`, `HomeFragment`, and `AssistanceFragment` render local SQLite records when API endpoints are unreachable and display non-disruptive Toast messages.
  - **Selective Entity Syncing**: `TrackerFragment` only marks the specifically saved entity as synced (`markPeriodsSynced` / `markMoodsSynced`) rather than marking all user records synced.
  - **Sync Engine Optimization**: `SQLiteToMySQLMigrator` queries only unsynced records (`getUnsyncedPeriods`, `getUnsyncedSymptoms`, `getUnsyncedContacts`, `getUnsyncedMoods`) to prevent duplicate payloads or re-transmitting synced entries.
- **Phase 2: Visual Design System Implementation (HomeFragment COMPLETED & VERIFIED)**:
  - **Color Palette**: Updated `colors.xml` with Warm Ivory canvas (`#FFFDF9`), Deep Plum primary (`#3B1F2B`), Soft Muted Rose (`#D87093`), Sage Green (`#7DA183`), and Warm Sand containers (`#F5EBE6`).
  - **Redesigned `HomeFragment`**: Implemented Material Design 3 cards, M3 Phase Chip badges (`chip_phase_badge`), vector icon quick actions (`ic_nav_tracker`, `ic_ai_sparkle`), Nunito typography, and phase-specific AI health insights.
  - **Verification Status**:
    - User Greeting: Reads authenticated session name from `TokenManager`, falls back to SQLite user profile, and defaults to "Friend".
    - Cycle Calculations: Loads real stored period dates from SQLite (`getPeriod`) and passes to `CycleCalculator.calculateCycleInfo`. Falls back gracefully when period history is empty.
    - System Bar Insets: `MainActivity.java` applies `WindowInsetsCompat` on `main_coordinator` preventing clipping on status and navigation bars.
    - Emulator Status: Sandbox environment is headless; Android GUI emulator daemon is not available.

### 2. Pending Work
- **Visual Design Phase (Remaining Fragments)**:
  - `TrackerFragment`, `AssistanceFragment`, `AwarenessFragment`, `ProfileFragment`, and auth activities.
- **Phase 3: Local Notification Engine**:
  - Implement native Android background scheduling with `WorkManager` / `AlarmManager` for period and medical reminders.

---

## Files Modified
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`
- `app/src/main/java/com/example/mahila/ui/tracker/TrackerFragment.java`
- `app/src/main/java/com/example/mahila/utils/SQLiteToMySQLMigrator.java`
- `app/src/test/java/com/example/mahila/DatabaseHelperTest.java`
- `app/src/test/java/com/example/mahila/Phase1VerificationTest.java`
- `MAHILA_PROGRESS.md`
