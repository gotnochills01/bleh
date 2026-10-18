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

## Technical Diagnostics & Security Refinements

### 1. Root Cause Analysis & Connection Handling
- **Error Stream Reading**: `ApiClient.java` inspects `conn.getErrorStream()` on non-2xx HTTP responses. When Spring Boot returns an HTTP 400 (Bad Request), 401 (Unauthorized), or 403 (Forbidden), `ApiClient` parses JSON error bodies (`{"message": "..."}`) and returns formatted error descriptions.
- **Connection Cleanup**: `ApiClient` enforces `finally { conn.disconnect(); }` execution to prevent socket leaks.
- **UI Button Recovery**: `LoginActivity.java` guarantees button re-enabling (`btnLogin.setEnabled(true); btnLogin.setText("Login");`) across all async network callbacks.

### 2. Authentication & Data Protection
- Enforced server-side authentication with JWT tokens without storing plaintext passwords in SQLite or `SharedPreferences`.
- Cleared stale JWT tokens in guest mode via `TokenManager.clear()` and isolated guest profile sessions.

---

## Evidence-Based Personalized Nutrition & Food Recommendations Feature

### 1. Feature Architecture & Backend Schema
- **MySQL Schema (`backend/src/main/resources/schema.sql`)**:
  - Added `nutrition_recommendations` table with columns: `id`, `cycle_phase`, `meal_category`, `food_item_name`, `key_nutrients`, `health_benefits`, `dietary_type`, `budget_level`, `evidence_level`, `safety_warnings`, and `evidence_url`.
  - Pre-seeded 10+ evidence-based records covering Menstrual, Follicular, Ovulatory, Luteal, and General Health cycle phases with WHO dietary standards and PubMed links.
- **Spring Boot Components**:
  - `NutritionRecommendation.java` (JPA Entity with Lombok annotations).
  - `NutritionRecommendationDTO.java` (Data transfer object).
  - `NutritionRepository.java` (Repository supporting phase, dietary type, and budget level query filters).
  - `NutritionController.java` (`GET /api/nutrition` and `GET /api/nutrition/{id}`).

### 2. Android SQLite Caching & API Client
- **Database Schema (`DatabaseHelper.java`)**: Added `nutrition_cache` table (version 6) and implemented `saveNutritionItemToCache(...)` and `getCachedNutritionRecommendations(phase, dietaryType, budgetLevel)` for full offline caching.
- **API Service Route (`ApiService.java`)**: Added `NUTRITION = "api/nutrition"` endpoint constant.

### 3. Android UI & Activity
- **Layouts**: Created `activity_nutrition.xml` and `item_nutrition_recommendation.xml` with Material Design 3 styling, cycle phase chip filters (`Menstrual`, `Follicular`, `Ovulatory`, `Luteal`, `All Phases`), dietary preference chip filters (`Vegetarian`, `Vegan`, `Non-Veg`, `All`), budget tier chip filters (`Budget Friendly`, `Moderate`, `All`), evidence disclaimer card, and direct PubMed research link buttons.
- **Activity**: Created `NutritionActivity.java` and `NutritionAdapter.java` providing cycle phase auto-detection via `CycleCalculator`, filter state management, local SQLite cache loading, and asynchronous Spring Boot backend sync.
- **Navigation**: Registered `drawer_nutrition` in `drawer_menu.xml`, wired click handling in `MainActivity.java`, and added quick access banner card on `fragment_home.xml` / `HomeFragment.java`.

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
2. **Android Unit Tests**: Executed `./gradlew test --no-daemon`. Result: **PASSED 100%** (`NutritionTest`, `RemindersTest`, `EmergencyContactsTest`, `CycleCalculatorTest`, `Phase2HealthTrackingTest`, `DatabaseHelperTest`, `Phase1VerificationTest` all passed).
3. **Spring Boot Backend Test Suite**: Executed `mvn test -f backend/pom.xml`. Result: **BUILD SUCCESSFUL** (`BackendIntegrationTest` passed with 0 failures).
