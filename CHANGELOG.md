# CHANGELOG - Mahila Groups 7, 8, and 9 Implementation

## Base Commit Information
- **Repository:** `gotnochills01/bleh`
- **Base Commit SHA:** `dbe2ba9220ad91571967fb42e48283e5a7be252b`
- **Target Branch:** `main`

---

## Changed & Added Files Summary

### 1. Group 7 — Quiz & Health Education
- **`app/src/main/java/com/example/mahila/QuizActivity.java`**
  - Prevents rapid double-tap submissions on the "Next Question" button using an `isSubmitting` click guard and temporary button disabling.
  - Validates answer selection before proceeding (`rgOptions.getCheckedRadioButtonId() == -1`), displaying a toast prompt when unselected.
  - Calculates scores, handles question progression delays, and presents a score summary `AlertDialog` with a "Retry Quiz" reset state.
  - Preserves remote quiz fetching via `ApiService.QUIZZES` with a 10-question local WHO/ACOG/CDC educational fallback.
- **`app/src/test/java/com/example/mahila/QuizTest.java`**
  - Unit test suite testing `QuizQuestionDTO` getters/setters, answer validation, score calculation math, index progression, and retry resets.

### 2. Group 8 — Profile Picture Selection & Persistence
- **`app/src/main/java/com/example/mahila/AppPreferences.java`**
  - Adds `saveProfileImagePath`, `getProfileImagePath`, and `clearProfileImagePath` methods isolated by user email (`PREFIX_PROFILE_IMAGE + userEmail`) so distinct accounts do not share avatars.
- **`app/src/main/java/com/example/mahila/ui/profile/ProfileFragment.java`**
  - Integrates Android system photo picker via `ActivityResultContracts.PickVisualMedia`.
  - Resizes and compresses selected images to JPEG format stored safely in `context.getFilesDir()`.
  - Renders avatar images on view creation/resume, falling back gracefully to `ic_person`.
- **`app/src/main/res/layout/fragment_profile.xml`**
  - Adds Material 3 card container with a circular ShapeableImageView (`img_profile_avatar`) and "Change Profile Picture" button (`btn_change_photo`).
- **`app/src/main/java/com/example/mahila/ui/home/HomeFragment.java`**
  - Renders the signed-in user's avatar image in the home card (`img_profile`) with `ic_person` fallback.
- **`app/src/test/java/com/example/mahila/ProfileAvatarTest.java`**
  - Unit test suite testing preference path saving, multi-user isolation, clear actions, and missing file fallback.

### 3. Group 9 — Settings, Language, and Notifications
- **`app/src/main/java/com/example/mahila/SettingsActivity.java`**
  - Implements `attachBaseContext` overriding configuration locale using `LocaleHelper`.
  - Maps six regional language choices (`en`, `kn`, `hi`, `ta`, `te`, `ml`), persisting selection in `TokenManager`.
- **`app/src/main/java/com/example/mahila/utils/LocaleHelper.java`**
  - Updates `Configuration` locale for target activity context.
- **`app/src/test/java/com/example/mahila/SettingsLanguageTest.java`**
  - Unit test suite testing language code mappings, locale resolution, and default fallback handling.

### 4. Progress & Verification Documentation
- **`MAHILA_PROGRESS.md`**
  - Updated progress document detailing Groups 1–9 status, launch routes, and 100% test suite pass results.
