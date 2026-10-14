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

---

## Verification & Build Results
1. **Android Debug Build**: Executed `./gradlew assembleDebug --no-daemon`. Result: `BUILD SUCCESSFUL`.
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: `BUILD SUCCESSFUL` (`CycleCalculatorTest`, `Phase2HealthTrackingTest`, `DatabaseHelperTest`, `Phase1VerificationTest` all passed).
3. **Spring Boot Backend**: Executed `mvn test -f backend/pom.xml`. Result: `BUILD SUCCESSFUL`.

---

## Remaining Roadmap by Phase

### Phase 3: Local Notification Engine & Background Scheduling (NEXT PHASE)
- Native Android background scheduling with `WorkManager` / `AlarmManager` for period reminders, ovulation alerts, and medication notifications.

---

## Files Modified
- `app/src/main/java/com/example/mahila/utils/CycleCalculator.java`
- `app/src/main/java/com/example/mahila/DatabaseHelper.java`
- `app/src/main/java/com/example/mahila/ui/tracker/TrackerFragment.java`
- `app/src/main/java/com/example/mahila/SymptomLoggerActivity.java`
- `app/src/main/res/layout/fragment_profile.xml`
- `app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`
- `app/src/main/java/com/example/mahila/ui/awareness/AwarenessFragment.java`
- `app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`
- `app/src/test/java/com/example/mahila/CycleCalculatorTest.java`
- `app/src/test/java/com/example/mahila/Phase2HealthTrackingTest.java`
- `MAHILA_PROGRESS.md`
