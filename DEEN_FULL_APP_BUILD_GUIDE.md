# DEEN — Complete Application Build & Development Guide

## 1. Development Environment

Verified directly from `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, and `app/build.gradle.kts`:

| Component | Configured / Recommended Version | Source |
|---|---|---|
| **Android Gradle Plugin (AGP)** | `9.1.1` | `gradle/libs.versions.toml` |
| **Gradle** | `9.3.1` | `gradle/wrapper/gradle-wrapper.properties` |
| **Kotlin** | `2.2.10` | `gradle/libs.versions.toml` |
| **KSP (Kotlin Symbol Processing)** | `2.3.5` | `gradle/libs.versions.toml` |
| **JDK** | JDK 17 or JDK 21 (bytecode target `JavaVersion.VERSION_11`) | `app/build.gradle.kts` |
| **Compile SDK** | `36` (Minor API 1) | `app/build.gradle.kts` |
| **Target SDK** | `36` | `app/build.gradle.kts` |
| **Minimum SDK** | `24` (Android 7.0) | `app/build.gradle.kts` |
| **Jetpack Compose BOM** | `2024.09.00` | `gradle/libs.versions.toml` |
| **Room** | `2.7.0` | `gradle/libs.versions.toml` |
| **Navigation Compose** | `2.8.9` | `gradle/libs.versions.toml` |
| **DataStore Preferences** | `1.1.7` | `gradle/libs.versions.toml` |
| **Play Services Location** | `21.3.0` | `gradle/libs.versions.toml` |
| **Robolectric** | `4.16.1` | `gradle/libs.versions.toml` |
| **Android Studio** | Android Studio Meerkat / Narwhal or newer supporting AGP `9.1.1` and SDK `36` | — |

### Device & Emulator Setup
- **Android Emulator**: Create an AVD with API 35/36, Google Play system image, and enable Virtual Sensors in Extended Controls (`... -> Virtual sensors`) to test Qibla compass rotation and mock GPS location.
- **Physical Android Device**: Enable **Developer Options -> USB Debugging**, connect via USB, and approve the RSA debugging fingerprint. Physical devices are mandatory for final compass magnetometer calibration, real GPS lock, and OEM battery/Doze notification verification.

---

## 2. Project Setup

1. **Open Project**: Open the repository root folder in Android Studio (`File -> Open`).
2. **Gradle Sync**: Android Studio uses `gradle/wrapper/gradle-wrapper.properties` (`9.3.1`) and `gradle/libs.versions.toml` automatically. Never create a `local.properties` containing API keys in cloud build environments; set `ANDROID_SDK_ROOT` in your shell or let Android Studio manage SDK paths locally.
3. **Environment Secrets**: The project configures the Secrets Gradle Plugin (`secrets { propertiesFileName = ".env" }`) and `MissingGoogleServicesStrategy.WARN`. DEEN runs 100% offline with zero required external API keys or Firebase configuration.
4. **First Build**: Run `gradle :app:assembleDebug` (or `./gradlew assembleDebug` locally with wrapper) to generate `R`, Room DAOs via KSP, and the debug APK.

---

## 3. Project Folder Structure

```text
app/src/main/
├── AndroidManifest.xml                  # Permissions (Location, Notifications, Boot, Exact Alarm) & Receivers
├── java/com/example/
│   ├── DeenApplication.kt               # Singleton dependency container, Room seeding & channel creation
│   ├── MainActivity.kt                  # edge-to-edge, Compose NavHost, NavigationBar/NavigationRail, EN/BN CompositionLocal
│   ├── core/i18n/
│   │   ├── AppLanguage.kt               # EN / BN enum
│   │   └── AppStrings.kt                # Complete bilingual UI string catalog (instant reactive switching)
│   ├── data/
│   │   ├── content/
│   │   │   └── VerifiedIslamicContent.kt # Primary-sourced Hadiths (10) & Duas/Azkar (16) with verification metadata
│   │   ├── local/database/
│   │   │   ├── Entities.kt              # Room entities: hadiths, duas, bookmarks, tasbih_presets, tasbih_sessions, prayer_tracker_logs
│   │   │   ├── Daos.kt                  # Flow-based Room DAOs & suspend write queries
│   │   │   └── DeenDatabase.kt          # RoomDatabase (v1) + initial seeding callback
│   │   └── repository/
│   │       ├── CityCatalog.kt           # Offline Bangladesh + global city coordinates & IANA timezones
│   │       ├── PreferencesRepository.kt # DataStore Preferences persistence
│   │       ├── ContentRepository.kt     # Hadith/Dua + Bookmark repository
│   │       └── WorshipRepository.kt     # Digital Tasbih & manual Prayer Tracker repository
│   ├── domain/
│   │   ├── model/DeenModels.kt          # Immutable domain models & enums
│   │   └── calculator/
│   │       ├── PrayerTimesCalculator.kt # Meeus/PrayTimes astronomical calculator (7 methods, Standard/Hanafi, DST, High-Lat)
│   │       ├── QiblaCalculator.kt       # Spherical trigonometry bearing & Haversine distance to Kaaba
│   │       └── HijriCalendarCalculator.kt # Tabular Hijri <-> Gregorian converter + moon-sighting offset + events
│   ├── location/
│   │   ├── LocationHelper.kt            # FusedLocationProviderClient + Geocoder reverse lookup
│   │   └── CompassSensorManager.kt      # RotationVector / Accel+Magnetometer circular-filtered heading flow
│   ├── notifications/
│   │   ├── PrayerAlarmScheduler.kt      # NotificationChannel setup & AlarmManager exact/inexact scheduler
│   │   ├── PrayerAlarmReceiver.kt       # Fires localized EN/BN notification & schedules next day
│   │   └── BootRescheduleReceiver.kt    # Restores alarms on BOOT_COMPLETED / TIME_SET / TIMEZONE_CHANGED
│   └── ui/
│       ├── components/DeenComponents.kt # DeenCard, ArabicTextBlock, VerificationBadge, EmptyStateView
│       ├── screens/
│       │   ├── PrayerHomeScreen.kt      # Next prayer hero card, countdown, city/GPS picker, toggles, 30-day dialog
│       │   ├── QiblaScreen.kt           # Live animated Qibla compass Canvas, alignment indicator, calibration guide
│       │   ├── LibraryScreen.kt         # Hadith, Dua & Azkar, Bookmarks, search, filters, details, repetition counter, share
│       │   ├── WorshipScreen.kt         # Digital Tasbih + manual Prayer Tracker + weekly/monthly charts
│       │   ├── CalendarScreen.kt        # Dual Hijri/Gregorian grid, offset stepper, Islamic milestones
│       │   └── SettingsScreen.kt        # Instant EN/BN, Light/Dark/System, Prayer calc, Madhab, Sound, Policies
│       ├── theme/
│       │   ├── Color.kt                 # Warm Cream / Emerald Green / Subtle Gold (Light) & Obsidian-Emerald (Dark)
│       │   ├── Type.kt                  # Bundled Amiri, Noto Sans Bengali, Plus Jakarta Sans, Inter fonts
│       │   └── Theme.kt                 # DeenTheme + system status bar icon contrast
│       └── viewmodel/
│           ├── MainViewModel.kt         # Live 1s countdown ticker, prayer/Qibla/Hijri state, preferences actions
│           ├── LibraryViewModel.kt      # Search, category filter, bookmarks
│           └── WorshipViewModel.kt      # Tasbih counter/sessions & manual prayer tracker stats
└── res/
    ├── drawable/                        # Adaptive icon foreground/background + img_deen_hero_banner.jpg
    ├── font/                            # amiri.ttf, noto_sans_bengali.ttf, plus_jakarta_sans.ttf, inter.ttf
    └── mipmap-*/                        # Custom square & round launcher PNGs (mdpi..xxxhdpi)
```

---

## 4. Application Architecture

DEEN follows unidirectional **MVVM + Repository + Pure Domain Calculator** architecture:

```text
Compose Screens (stateless UI + LocalAppStrings)
        │  collectAsStateWithLifecycle()
        ▼
ViewModels (MainViewModel, LibraryViewModel, WorshipViewModel)
        │  Domain Calculators (PrayerTimesCalculator, QiblaCalculator, HijriCalendarCalculator)
        ▼
Repositories (PreferencesRepository, ContentRepository, WorshipRepository)
        │
        ├── Room Database (DeenDatabase: Hadith, Dua, Bookmarks, Tasbih, PrayerTracker)
        ├── DataStore Preferences (Language, Theme, Location, Method, Madhab, Alerts)
        └── Platform Services (FusedLocationProviderClient, SensorManager, AlarmManager)
```

---

## 5. Feature Development Guide

1. **Prayer Times & Azan**: `MainViewModel` ticks every 1s over `PreferencesRepository.userPreferences`, invoking `PrayerTimesCalculator.calculateDaySchedule` and `findNextPrayer` (handles post-Isha midnight rollover to tomorrow's Fajr). Toggling switches updates DataStore and invokes `PrayerAlarmScheduler.rescheduleAll`.
2. **Qibla Compass**: `QiblaCalculator.calculateBearing` computes True North bearing to Kaaba (`21.422487, 39.826206`). `CompassSensorManager` emits circular-smoothed azimuth readings. `QiblaScreen` rotates the dial smoothly and highlights emerald when `|delta| <= 4°`.
3. **Hadith & Dua/Azkar**: Seeded into Room on first DB creation (`DeenDatabase`). `LibraryViewModel` combines `hadithDao`, `duaDao`, and `bookmarkDao` into reactive UI state. Bookmarks live in a separate table (`bookmarks`) so religious dataset updates never overwrite user saves.
4. **Digital Tasbih**: Current count & active target persist in DataStore; completed cycles & resets save into `tasbih_sessions` in Room (`dateKey = yyyy-MM-dd`). Custom presets persist in `tasbih_presets`.
5. **Prayer Tracker**: Strictly manual (`PrayerStatus.COMPLETED` / `MISSED` / `UNMARKED`). `WorshipViewModel` computes 7-day and 30-day aggregates from `prayer_tracker_logs`.
6. **Hijri Calendar**: `HijriCalendarCalculator.gregorianToHijri` converts dates with user-configurable `-2..+2` day moon-sighting offset and discloses predicted status clearly.
7. **Instant Language & Theme Switching**: `MainActivity` provides `LocalAppStrings` from `prefs.language` and wraps content in `DeenTheme(prefs.themeMode)`. Switching EN <-> BN or Light <-> Dark re-composes immediately with zero Activity recreation.

---

## 6. Islamic Content Management

- **Primary Source**: `app/src/main/java/com/example/data/content/VerifiedIslamicContent.kt` (seeded into `hadiths` and `duas` Room tables).
- **Mandatory Fields**: Every record carries `id`, `arabicText` (full Harakat), `bengaliPronunciation` (for Duas), `bengaliMeaning`, `englishMeaning`, `sourceBook`, `reference`, `authenticityGrade`, `gradingScholar` (for Hadiths), `sourceUrl`, `verificationStatus`, `lastVerifiedDate`, and `verificationNotes`.
- **Adding/Updating Content Safely**:
  1. Verify exact Arabic Matn, book/hadith number, narrator, and scholar grade against `sunnah.com` or primary collections.
  2. Never overwrite `bookmarks`, `tasbih_sessions`, or `prayer_tracker_logs` during schema/content updates—increment `DeenDatabase` version with a non-destructive `Migration(1, 2)` and upsert only `hadiths` / `duas`.

---

## 7. How to Run the App

### Linux / Cloud Environment (`gradle`)
```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

### Windows PowerShell (Local Android Studio with Wrapper)
```powershell
.\gradlew.bat --refresh-dependencies
.\gradlew.bat clean assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat installDebug
```

---

## 8. Debugging Guide

- **Location not updating**: Ensure `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` is granted in App Settings or choose a city manually from `CityCatalog`. On emulators, inject coordinates via Extended Controls -> Location.
- **Notifications delayed or missing on Android 13+**: Grant `POST_NOTIFICATIONS` from the Home banner or Android Settings. On Android 12+, if "Alarms & reminders" is revoked, `PrayerAlarmScheduler` automatically falls back to `setAndAllowWhileIdle()`.
- **Compass frozen / inaccurate**: Test on physical hardware away from magnetic phone cases and perform a figure-8 calibration motion. Emulators without virtual magnetometer input show the `Magnetometer Unavailable` fallback card.
- **Arabic / Bengali font clipping**: Verify `app/src/main/res/font/amiri.ttf` and `noto_sans_bengali.ttf` are present and referenced via `Type.kt` (`lineHeight = 42.sp` for Arabic display).

---

## 9. APK & AAB Build Guide

- **Debug APK**: `gradle :app:assembleDebug` -> output at `app/build/outputs/apk/debug/app-debug.apk`.
- **Upload Keystore Generation (One-Time, Outside Repo)**:
  ```powershell
  keytool -genkeypair -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
  ```
- **Signed Release Build**: Export `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` as environment variables (never commit passwords or `.jks` files to Git), then run:
  ```powershell
  gradle :app:bundleRelease
  gradle :app:assembleRelease
  ```

---

## 10. Google Play Release Guide

- **Application ID**: `com.aistudio.deen.islamic` (immutable once published on Google Play).
- **Versioning**: Increment `versionCode` (integer) and `versionName` (`1.0`, `1.1`, ...) in `app/build.gradle.kts` before every upload.
- **Permissions & Data Safety**: Uses only `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` (foreground only—no background location), `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, and `VIBRATE`. No broad storage permissions (`READ_EXTERNAL_STORAGE` / `READ_MEDIA_*`). All user data stays locally on-device.
- **Release Track Progression**: Upload signed `.aab` to **Internal Testing** -> **Closed Testing** (verify 12+ testers / 14 days if required for personal Play Console accounts) -> **Production**.

---

## 11. Maintenance & Final Pre-Release Checklist

- [ ] Increment `versionCode` & `versionName`
- [ ] Run `gradle :app:testDebugUnitTest` and confirm 100% pass
- [ ] Verify `VerifiedIslamicContent.kt` changes are scholarly reviewed
- [ ] Verify Light & Dark Mode contrast on every screen
- [ ] Verify Bengali & English strings have zero untranslated labels
- [ ] Test signed release `.aab` / `.apk` on physical device (GPS, Qibla sensor, Azan alarm)
