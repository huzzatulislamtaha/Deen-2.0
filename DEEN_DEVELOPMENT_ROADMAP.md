# DEEN — Development Roadmap

## Current Baseline & Reality Check
- **Initial Workspace State**: Clean Android Jetpack Compose template (`My Application`, `com.example`, `MainActivity` greeting screen, no prior DEEN implementation).
- **Implemented Release Candidate (v1.0)**: Complete offline-first DEEN Android application (`com.aistudio.deen.islamic`) built in Kotlin + Jetpack Compose + Material 3 + Room + DataStore + Navigation Compose + AlarmManager + FusedLocationProviderClient + SensorManager.

---

## Feature Completion Matrix (Post-Implementation)

| Feature | Initial Status | Current Status | Key Files | Verification / Notes |
|---|---|---|---|---|
| **App Identity & Launcher Icon** | NOT IMPLEMENTED | COMPLETE | `metadata.json`, `settings.gradle.kts`, `strings.xml`, `ic_launcher_*` | Adaptive icon + 5 mipmap densities generated; `DEEN` title synced |
| **Prayer Times Engine** | NOT IMPLEMENTED | COMPLETE | `PrayerTimesCalculator.kt`, `CityCatalog.kt`, `PrayerHomeScreen.kt` | Astronomical Meeus/PrayTimes engine, 7 methods, Standard & Hanafi, DST-aware, 30-day timetable, midnight wraparound |
| **Azan / Prayer Notifications** | NOT IMPLEMENTED | COMPLETE | `PrayerAlarmScheduler.kt`, `PrayerAlarmReceiver.kt`, `BootRescheduleReceiver.kt` | Exact-alarm with fallback, EN/BN localized notifications, per-prayer toggles, reboot/timezone rescheduling; device audio/Doze testing pending on physical hardware |
| **Qibla Compass** | NOT IMPLEMENTED | COMPLETE | `QiblaCalculator.kt`, `CompassSensorManager.kt`, `QiblaScreen.kt` | Spherical trigonometry bearing & Haversine distance verified; magnetometer fusion + fallback implemented; physical compass calibration requires real hardware |
| **Hadith Library** | NOT IMPLEMENTED | COMPLETE (Core Dataset) | `VerifiedIslamicContent.kt`, `DeenDatabase.kt`, `LibraryScreen.kt` | 10 primary-sourced Hadiths verified against Sunnah.com; Bengali translations marked for scholarly human review; expandable via Room upsert |
| **Dua & Azkar** | NOT IMPLEMENTED | COMPLETE (Core Dataset) | `VerifiedIslamicContent.kt`, `DeenDatabase.kt`, `LibraryScreen.kt` | 16 verified Duas/Azkar across all 6 categories with Bengali pronunciation, EN/BN meanings, interactive repetition counter, Share & Bookmarks; audio recitation deferred until licensed audio files are supplied |
| **Digital Tasbih** | NOT IMPLEMENTED | COMPLETE | `WorshipRepository.kt`, `WorshipViewModel.kt`, `WorshipScreen.kt` | Animated progress ring, 6 built-in presets, custom Dhikr creation, custom targets, haptic feedback, daily total & session history persisted in Room/DataStore |
| **Prayer Tracker** | NOT IMPLEMENTED | COMPLETE | `PrayerTrackerDao`, `WorshipRepository.kt`, `WorshipScreen.kt` | Strict manual logging (`Prayed` / `Missed` / `Clear`), date browser, 7-day & 30-day statistics; never auto-completes |
| **Hijri Calendar** | NOT IMPLEMENTED | COMPLETE | `HijriCalendarCalculator.kt`, `CalendarScreen.kt` | Kuwaiti/Tabular Hijri converter, dual Gregorian/Hijri grid, `-2..+2` moon-sighting offset, predicted vs confirmed disclosure, 7 Islamic milestones |
| **Language System (EN / BN)** | NOT IMPLEMENTED | COMPLETE | `AppLanguage.kt`, `AppStrings.kt`, `MainActivity.kt` | Instant reactive EN/BN switch with zero Activity recreation and zero mixed-language dialogs |
| **Light & Dark Mode** | NOT IMPLEMENTED | COMPLETE | `Color.kt`, `Type.kt`, `Theme.kt` | Deen Warm Cream / Emerald Green / Gold Light theme & deep Obsidian-Emerald Dark theme + System follow |

---

## Phased Post-v1.0 Roadmap

### Phase 1 — Scholarly & Physical-Device Sign-Off (Pre-Play-Store)
1. **Qualified Islamic Scholar Review**: Independent proofreading of all Bengali phonetic transliterations and Bengali translations in `VerifiedIslamicContent.kt` by a qualified Bangladeshi Islamic scholar/institution.
2. **Physical Device QA**: Verify Qibla magnetometer behavior, GPS lock indoors/outdoors, Doze-mode `AlarmManager` prayer notification timing across OEM battery savers (Xiaomi MIUI, Samsung OneUI, Oppo ColorOS), and small-screen (`320dp` width) layout density.
3. **Upload Keystore & Release Signing**: Generate production upload keystore (`upload-keystore.jks`) outside source control and build signed `.aab` (`gradle :app:bundleRelease`).

### Phase 2 — Content Expansion & Audio (v1.1)
1. **Expanded Verified Corpus**: Add full 40 Hadith an-Nawawi and complete Hisn al-Muslim chapters using versioned JSON/Room migrations (`Migration(1, 2)`) that preserve user `bookmarks`, `tasbih_sessions`, and `prayer_tracker_logs`.
2. **Licensed Azan & Dua Audio**: Bundle or stream properly licensed Azan audio files (`res/raw/`) with foreground `MediaPlayback` service controls and full Play policy attribution.

### Phase 3 — Ecosystem & Accessibility Polish (v1.2)
1. **Home Screen Widget (Glance)**: Compact next-prayer & Hijri date widget.
2. **Optional Cloud Backup**: Encrypted Export/Import JSON backup of bookmarks, prayer logs, and Tasbih history (or optional Google Sign-In + Firestore sync).
3. **TalkBack & Font-Scaling Certification**: Full accessibility audit at 200% system font scale.
