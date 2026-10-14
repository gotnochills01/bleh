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
- [x] **Complete UI Refactoring Across All Screens**:
  - `HomeFragment`: M3 cycle cards, phase chip badges, and dynamic AI cycle guidance.
  - `TrackerFragment`: Period start logging with M3 sliders and mood recording.
  - `AssistanceFragment`: Healthcare mapping buttons and age-tailored specialist guidance.
  - `AwarenessFragment`: Interactive health quiz card and category cards.
  - `ProfileFragment`: Full user profile management (DOB, blood group, city, emergency notes, height, weight).
  - Feature Activities: `SettingsActivity`, `SymptomLoggerActivity`, `EmergencyContactsActivity`, `QuizActivity`, `HealthTipsActivity`, `AiChatActivity`.
- [x] **Technical & Offline Foundation**:
  - Database Helper v3 schema with full user profile storage, `moods` table, and `is_synced` status flags.
  - User-data isolation strictly enforced by `WHERE user_email = ?`.
  - `SQLiteToMySQLMigrator` sync engine optimized to extract strictly unsynced records (`is_synced = 0`).

---

## Verification & Build Results
1. **Android Debug Build**: Executed `./gradlew assembleDebug --no-daemon`. Result: `BUILD SUCCESSFUL`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: `BUILD SUCCESSFUL` (All tests passed).
3. **Spring Boot Backend**: Executed `mvn test -f backend/pom.xml`. Result: `BUILD SUCCESSFUL`.

---

## Remaining Roadmap by Phase

### Phase 2: Local Notification Engine (Pending)
- Native Android background scheduling with `WorkManager` / `AlarmManager` for cycle and medication reminders.

### Phase 3: Comprehensive Test Coverage & CI Pipelines (Pending)
- Integration testing for sync edge-cases and offline queue management.

---

## Files Modified
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/layout/activity_landing.xml`
- `app/src/main/res/layout/activity_login.xml`
- `app/src/main/res/layout/activity_signup.xml`
- `app/src/main/res/layout/activity_welcome.xml`
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/res/layout/fragment_tracker.xml`
- `app/src/main/res/layout/fragment_assistance.xml`
- `app/src/main/res/layout/fragment_awareness.xml`
- `app/src/main/res/layout/fragment_profile.xml`
- `app/src/main/res/layout/activity_settings.xml`
- `app/src/main/res/layout/activity_symptom_logger.xml`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`
- `app/src/main/java/com/example/mahila/ui/tracker/TrackerFragment.java`
- `app/src/main/java/com/example/mahila/utils/SQLiteToMySQLMigrator.java`
- `app/src/test/java/com/example/mahila/DatabaseHelperTest.java`
- `app/src/test/java/com/example/mahila/Phase1VerificationTest.java`
- `.gitignore`
- `MAHILA_PROGRESS.md`
