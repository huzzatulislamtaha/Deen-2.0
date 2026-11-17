package com.example.core.i18n

import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    val appTitle: String
    val navPrayer: String
    val navQibla: String
    val navLibrary: String
    val navWorship: String
    val navCalendar: String
    val settingsTitle: String
    val back: String
    val close: String
    val save: String
    val cancel: String
    val reset: String
    val delete: String
    val add: String
    val share: String
    val edit: String
    val searchPlaceholder: String
    val clearSearch: String
    val noResultsTitle: String
    val noResultsSubtitle: String
    val loading: String

    // Home Dashboard Hub
    val homeFeaturesTitle: String
    val featurePrayerTimes: String
    val featureQibla: String
    val featureQuran: String
    val featureHadith: String
    val featureDuaAzkar: String
    val featureTasbih: String
    val featurePrayerTracker: String
    val featureCalendar: String
    val featureNotebook: String
    val featureLocation: String
    val featureTimezone: String
    val featureSettings: String
    val homeNotesCountFormat: String
    val homePrayedTodayFormat: String
    val homeTapToChangeLocation: String

    // Quran Reader
    val quranTitle: String
    val quranSubtitle: String
    val quranReflectInNotebook: String

    // Prayers
    val fajr: String
    val sunrise: String
    val dhuhr: String
    val asr: String
    val maghrib: String
    val isha: String
    val nextPrayerLabel: String
    val startsIn: String
    val prayerTimetableTitle: String
    val monthlyTimetable: String
    val viewMonthlySchedule: String
    val todaySchedule: String
    val detectLocation: String
    val selectCity: String
    val locationPermissionNeeded: String
    val locationPermissionRationale: String
    val grantPermission: String
    val calculationMethodLabel: String
    val madhabLabel: String
    val madhabStandard: String
    val madhabHanafi: String
    val highLatitudeLabel: String
    val prayerNotificationsTitle: String
    val notificationPermissionNeeded: String
    val azanSoundLabel: String
    val soundDefault: String
    val soundChime: String
    val soundSilent: String
    val notificationBodyFormat: String
    val alarmScheduleWarning: String

    // Location Selection System
    val locationSettingsTitle: String
    val locationModeLabel: String
    val locationModeAuto: String
    val locationModeManual: String
    val countryLabel: String
    val divisionLabel: String
    val districtLabel: String
    val upazilaLabel: String
    val upazilaOptionalNone: String
    val searchDivisionPlaceholder: String
    val searchDistrictPlaceholder: String
    val confirmLocation: String
    val resetToDefaultDhaka: String
    val gpsDetectedBadge: String
    val referenceCoordsNotice: String
    val activeLocationSummary: String
    val useManualFallback: String
    val coordinatesLabel: String

    // Timezone System
    val timezoneSettingsTitle: String
    val timezoneModeLabel: String
    val timezoneModeAuto: String
    val timezoneModeManual: String
    val resetDeviceTimezone: String
    val useBangladeshDhakaTz: String
    val searchTimezonePlaceholder: String
    val currentLocalPreview: String
    val dstSupportedLabel: String
    val dstNotUsedLabel: String
    val selectedTimezoneIndicator: String
    val bangladeshTzNotice: String

    // Font Settings
    val fontSettingsTitle: String
    val fontPreviewSection: String
    val fontAppSizeLabel: String
    val fontSizeSmall: String
    val fontSizeMedium: String
    val fontSizeLarge: String
    val fontSizeExtraLarge: String
    val fontArabicSizeLabel: String
    val fontBengaliSizeLabel: String
    val fontEnglishSizeLabel: String
    val fontLatinFamilyLabel: String
    val resetFontDefaults: String

    // Qibla
    val qiblaTitle: String
    val qiblaDirectionLabel: String
    val distanceToKaaba: String
    val kmUnit: String
    val alignedWithQibla: String
    val turnRight: String
    val turnLeft: String
    val calibrateCompassTitle: String
    val calibrateCompassHint: String
    val sensorAccuracyLow: String
    val sensorAccuracyMedium: String
    val sensorAccuracyHigh: String
    val noCompassSensorTitle: String
    val noCompassSensorBody: String
    val bearingFromNorth: String

    // Hadith & Dua Library
    val libraryTitle: String
    val tabHadith: String
    val tabDuaAzkar: String
    val tabBookmarks: String
    val allCategories: String
    val catMorningAzkar: String
    val catEveningAzkar: String
    val catAfterPrayer: String
    val catDailyDua: String
    val catSleepDua: String
    val catTravelDua: String
    val narratorLabel: String
    val referenceLabel: String
    val gradeLabel: String
    val gradedByLabel: String
    val pronunciationLabel: String
    val bengaliMeaningLabel: String
    val englishMeaningLabel: String
    val contextLabel: String
    val repeatCountFormat: String
    val tapToRecite: String
    val reciteCompleted: String
    val bookmarkAdd: String
    val bookmarkRemove: String
    val noBookmarksTitle: String
    val noBookmarksSubtitle: String
    val verificationBadgeVerified: String
    val verificationBadgePending: String
    val verificationBadgeUnverified: String
    val verificationDetailsTitle: String
    val sourceUrlLabel: String
    val lastVerifiedLabel: String

    // Worship: Digital Tasbih & Prayer Tracker
    val worshipTitle: String
    val tabTasbih: String
    val tabPrayerTracker: String
    val targetLabel: String
    val customTarget: String
    val enterCustomTarget: String
    val addCustomDhikr: String
    val dhikrTitleEn: String
    val dhikrTitleBn: String
    val dhikrArabic: String
    val dailyTotalLabel: String
    val sessionHistoryTitle: String
    val noSessionsYet: String
    val hapticFeedbackLabel: String
    val tapCounterHint: String
    val trackerTodayTitle: String
    val trackerStatusUnmarked: String
    val trackerStatusCompleted: String
    val trackerStatusMissed: String
    val markCompleted: String
    val markMissed: String
    val clearStatus: String
    val weeklyStatsTitle: String
    val monthlyStatsTitle: String
    val completedCountFormat: String
    val missedCountFormat: String
    val manualTrackerNote: String

    // Hijri Calendar
    val calendarTitle: String
    val hijriTodayLabel: String
    val hijriOffsetLabel: String
    val hijriDaysUnit: String
    val predictedMoonNote: String
    val importantDatesTitle: String
    val eventNewYear: String
    val eventAshura: String
    val eventRamadanStart: String
    val eventLaylatulQadr: String
    val eventEidFitr: String
    val eventArafah: String
    val eventEidAdha: String
    val hijriMonths: List<String>

    // Islamic Notebook
    val notebookTitle: String
    val notebookTotalNotes: String
    val notebookFavorites: String
    val notebookPinned: String
    val notebookRecycleBin: String
    val notebookRecentlyUpdated: String
    val notebookAddNote: String
    val notebookSearchPlaceholder: String
    val notebookEmptyTitle: String
    val notebookEmptySubtitle: String
    val notebookRecycleEmptyTitle: String
    val notebookRecycleEmptySubtitle: String
    val notebookManageCategories: String
    val notebookCategoryNameEn: String
    val notebookCategoryNameBn: String
    val notebookCreateCategory: String
    val notebookRenameCategory: String
    val notebookDeleteCategoryTitle: String
    val notebookDeleteCategoryBody: String
    val notebookMoveToCategoryLabel: String
    val notebookNoteTitleLabel: String
    val notebookNoteDescriptionLabel: String
    val notebookNoteContentLabel: String
    val notebookTagsLabel: String
    val notebookCategoryLabel: String
    val notebookReferencesSection: String
    val notebookHadithCollection: String
    val notebookHadithNumber: String
    val notebookDuaSource: String
    val notebookIslamicBook: String
    val notebookPageNumber: String
    val notebookWebsiteUrl: String
    val notebookReferenceDisclaimer: String
    val notebookCreatedLabel: String
    val notebookUpdatedLabel: String
    val notebookAutosaveSaving: String
    val notebookAutosaveSaved: String
    val notebookAutosaveError: String
    val notebookRtlArabicToggle: String
    val notebookPreviewToggle: String
    val notebookEditToggle: String
    val notebookRestore: String
    val notebookDeleteForever: String
    val notebookEmptyBin: String
    val notebookRecoverDraftTitle: String
    val notebookRecoverDraftBody: String
    val notebookRecoverAction: String
    val notebookDiscardAction: String
    val notebookSortUpdatedDesc: String
    val notebookSortUpdatedAsc: String
    val notebookSortCreatedDesc: String
    val notebookSortTitleAsc: String
    val notebookPinPromptTitle: String
    val notebookPinPromptBody: String
    val notebookUnlockAction: String
    val notebookWrongPin: String

    // Settings
    val locationTimeSection: String
    val appearanceSection: String
    val personalizationSection: String
    val privacySection: String
    val languageLabel: String
    val themeLabel: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val prayerSettingsSection: String
    val locationLabel: String
    val timezoneLabel: String
    val fontSettingsLabel: String
    val notebookDefaultCatLabel: String
    val notebookAutosavePrefLabel: String
    val notebookPinPrefLabel: String
    val notebookPinSet: String
    val notebookPinDisabled: String
    val notebookPinEnterNew: String
    val notebookPinRemove: String
    val notebookPrivacyInfoTitle: String
    val notebookPrivacyInfoBody: String
    val localStorageInfoTitle: String
    val localStorageInfoBody: String
    val aboutSection: String
    val appVersionLabel: String
    val contentVerificationPolicyTitle: String
    val contentVerificationPolicyBody: String
    val privacyPolicyTitle: String
    val privacyPolicyBody: String
    val aboutDeenTitle: String
    val aboutDeenBody: String
}

object EnglishStrings : AppStrings {
    override val appTitle = "DEEN"
    override val navPrayer = "Home"
    override val navQibla = "Qibla"
    override val navLibrary = "Hadith & Dua"
    override val navWorship = "Worship"
    override val navCalendar = "Calendar"
    override val settingsTitle = "Settings"
    override val back = "Back"
    override val close = "Close"
    override val save = "Save"
    override val cancel = "Cancel"
    override val reset = "Reset"
    override val delete = "Delete"
    override val add = "Add"
    override val share = "Share"
    override val edit = "Edit"
    override val searchPlaceholder = "Search Arabic, translation, or reference..."
    override val clearSearch = "Clear search"
    override val noResultsTitle = "No matching records found"
    override val noResultsSubtitle = "Try a different keyword, book name, or category."
    override val loading = "Loading..."

    override val homeFeaturesTitle = "Islamic Companion Tools"
    override val featurePrayerTimes = "Prayer Times"
    override val featureQibla = "Qibla"
    override val featureQuran = "Quran"
    override val featureHadith = "Hadith"
    override val featureDuaAzkar = "Dua & Azkar"
    override val featureTasbih = "Digital Tasbih"
    override val featurePrayerTracker = "Prayer Tracker"
    override val featureCalendar = "Hijri Calendar"
    override val featureNotebook = "Islamic Notebook"
    override val featureLocation = "Location"
    override val featureTimezone = "Timezone"
    override val featureSettings = "Settings"
    override val homeNotesCountFormat = "%d notes"
    override val homePrayedTodayFormat = "%d/5 prayed"
    override val homeTapToChangeLocation = "Tap to select division/district"

    override val quranTitle = "Essential Quran"
    override val quranSubtitle = "Verified short Surahs and Ayat al-Kursi with Bengali and English meanings."
    override val quranReflectInNotebook = "Write Reflection"

    override val fajr = "Fajr"
    override val sunrise = "Sunrise"
    override val dhuhr = "Dhuhr"
    override val asr = "Asr"
    override val maghrib = "Maghrib"
    override val isha = "Isha"
    override val nextPrayerLabel = "Next Prayer"
    override val startsIn = "Starts in"
    override val prayerTimetableTitle = "DEEN Companion"
    override val monthlyTimetable = "Monthly Timetable"
    override val viewMonthlySchedule = "30-Day Timetable"
    override val todaySchedule = "Today's Schedule"
    override val detectLocation = "Use GPS Location"
    override val selectCity = "Location Setup"
    override val locationPermissionNeeded = "Location access enables local prayer times and Qibla direction."
    override val locationPermissionRationale = "You can grant foreground location or pick your district manually anytime."
    override val grantPermission = "Allow"
    override val calculationMethodLabel = "Calculation Method"
    override val madhabLabel = "Asr Juristic Method (Madhab)"
    override val madhabStandard = "Standard (Shafi'i, Maliki, Hanbali)"
    override val madhabHanafi = "Hanafi (2x shadow)"
    override val highLatitudeLabel = "High Latitude Adjustment"
    override val prayerNotificationsTitle = "Prayer Reminders"
    override val notificationPermissionNeeded = "Allow notifications to receive prayer time reminders."
    override val azanSoundLabel = "Alert Tone"
    override val soundDefault = "System Notification"
    override val soundChime = "Gentle Chime"
    override val soundSilent = "Silent Banner"
    override val notificationBodyFormat = "It is time for %s prayer in %s."
    override val alarmScheduleWarning = "Exact alarm permission is restricted on this device; reminders may arrive with slight system delay."

    override val locationSettingsTitle = "Location Selection"
    override val locationModeLabel = "Location Mode"
    override val locationModeAuto = "Automatic (GPS)"
    override val locationModeManual = "Manual Selection"
    override val countryLabel = "Country"
    override val divisionLabel = "Division (8 Divisions)"
    override val districtLabel = "District (64 Districts)"
    override val upazilaLabel = "Upazila (Optional)"
    override val upazilaOptionalNone = "District Headquarters (No Upazila)"
    override val searchDivisionPlaceholder = "Search division..."
    override val searchDistrictPlaceholder = "Search any of 64 districts..."
    override val confirmLocation = "Confirm Location"
    override val resetToDefaultDhaka = "Reset to Dhaka (Default)"
    override val gpsDetectedBadge = "GPS Coordinates"
    override val referenceCoordsNotice = "Using verified administrative reference coordinates (differs from exact live GPS)."
    override val activeLocationSummary = "Active Location"
    override val useManualFallback = "Switch to Manual Selection"
    override val coordinatesLabel = "Coordinates"

    override val timezoneSettingsTitle = "Timezone Settings"
    override val timezoneModeLabel = "Timezone Mode"
    override val timezoneModeAuto = "Device Automatic"
    override val timezoneModeManual = "Manual IANA Zone"
    override val resetDeviceTimezone = "Reset to Device Timezone"
    override val useBangladeshDhakaTz = "Use Asia/Dhaka (UTC+06:00)"
    override val searchTimezonePlaceholder = "Search IANA ID, city, or UTC offset..."
    override val currentLocalPreview = "Local Time Preview"
    override val dstSupportedLabel = "DST-aware"
    override val dstNotUsedLabel = "Standard time"
    override val selectedTimezoneIndicator = "Active"
    override val bangladeshTzNotice = "All 64 districts of Bangladesh observe the national timezone Asia/Dhaka (UTC+06:00)."

    override val fontSettingsTitle = "Font Settings"
    override val fontPreviewSection = "Live Script Preview"
    override val fontAppSizeLabel = "App Font Size"
    override val fontSizeSmall = "Small"
    override val fontSizeMedium = "Medium"
    override val fontSizeLarge = "Large"
    override val fontSizeExtraLarge = "XL"
    override val fontArabicSizeLabel = "Arabic Text Size"
    override val fontBengaliSizeLabel = "Bengali Text Size"
    override val fontEnglishSizeLabel = "English Text Size"
    override val fontLatinFamilyLabel = "English Font Family"
    override val resetFontDefaults = "Reset to Default"

    override val qiblaTitle = "Qibla Compass"
    override val qiblaDirectionLabel = "Qibla Bearing"
    override val distanceToKaaba = "Distance to Kaaba"
    override val kmUnit = "km"
    override val alignedWithQibla = "Aligned toward Qibla"
    override val turnRight = "Turn slightly right"
    override val turnLeft = "Turn slightly left"
    override val calibrateCompassTitle = "Compass Calibration"
    override val calibrateCompassHint = "Move your phone in a figure-8 motion away from magnetic cases or metal."
    override val sensorAccuracyLow = "Low accuracy — please calibrate"
    override val sensorAccuracyMedium = "Moderate accuracy"
    override val sensorAccuracyHigh = "High accuracy"
    override val noCompassSensorTitle = "Magnetometer Unavailable"
    override val noCompassSensorBody = "This device lacks a magnetic compass sensor. Use the true-north bearing below as reference."
    override val bearingFromNorth = "Bearing from True North"

    override val libraryTitle = "Hadith & Dua"
    override val tabHadith = "Hadith"
    override val tabDuaAzkar = "Dua & Azkar"
    override val tabBookmarks = "Saved"
    override val allCategories = "All"
    override val catMorningAzkar = "Morning Azkar"
    override val catEveningAzkar = "Evening Azkar"
    override val catAfterPrayer = "After Prayer"
    override val catDailyDua = "Daily Duas"
    override val catSleepDua = "Sleep Duas"
    override val catTravelDua = "Travel Duas"
    override val narratorLabel = "Narrator"
    override val referenceLabel = "Reference"
    override val gradeLabel = "Authenticity"
    override val gradedByLabel = "Grading Source"
    override val pronunciationLabel = "Bengali Pronunciation"
    override val bengaliMeaningLabel = "Bengali Translation"
    override val englishMeaningLabel = "English Translation"
    override val contextLabel = "Recitation Context"
    override val repeatCountFormat = "Recite %dx"
    override val tapToRecite = "Tap to count"
    override val reciteCompleted = "Completed"
    override val bookmarkAdd = "Save bookmark"
    override val bookmarkRemove = "Remove bookmark"
    override val noBookmarksTitle = "No saved items yet"
    override val noBookmarksSubtitle = "Bookmark any Hadith or Dua to keep it handy offline."
    override val verificationBadgeVerified = "Source Verified"
    override val verificationBadgePending = "Pending Scholarly Review"
    override val verificationBadgeUnverified = "Unverified"
    override val verificationDetailsTitle = "Content Verification Record"
    override val sourceUrlLabel = "Primary Source"
    override val lastVerifiedLabel = "Last Checked"

    override val worshipTitle = "Worship"
    override val tabTasbih = "Digital Tasbih"
    override val tabPrayerTracker = "Prayer Tracker"
    override val targetLabel = "Target"
    override val customTarget = "Custom"
    override val enterCustomTarget = "Enter Target Count"
    override val addCustomDhikr = "New Dhikr"
    override val dhikrTitleEn = "English Title"
    override val dhikrTitleBn = "Bengali Title"
    override val dhikrArabic = "Arabic Text"
    override val dailyTotalLabel = "Today's Total"
    override val sessionHistoryTitle = "Completed Sessions"
    override val noSessionsYet = "Complete a target cycle to record your Tasbih history."
    override val hapticFeedbackLabel = "Haptic Vibration"
    override val tapCounterHint = "Tap ring to count"
    override val trackerTodayTitle = "Daily Prayer Log"
    override val trackerStatusUnmarked = "Unmarked"
    override val trackerStatusCompleted = "Prayed"
    override val trackerStatusMissed = "Missed"
    override val markCompleted = "Prayed"
    override val markMissed = "Missed"
    override val clearStatus = "Clear"
    override val weeklyStatsTitle = "Last 7 Days"
    override val monthlyStatsTitle = "Last 30 Days"
    override val completedCountFormat = "%d prayed"
    override val missedCountFormat = "%d missed"
    override val manualTrackerNote = "Prayers are never auto-marked. Record each prayer manually."

    override val calendarTitle = "Hijri Calendar"
    override val hijriTodayLabel = "Islamic Date"
    override val hijriOffsetLabel = "Moon-Sighting Adjustment"
    override val hijriDaysUnit = "days"
    override val predictedMoonNote = "Calculated astronomically (predicted). Use the adjustment control to match local moon-sighting announcements."
    override val importantDatesTitle = "Key Islamic Dates"
    override val eventNewYear = "Islamic New Year (1 Muharram)"
    override val eventAshura = "Day of Ashura (10 Muharram)"
    override val eventRamadanStart = "Start of Ramadan (1 Ramadan)"
    override val eventLaylatulQadr = "27th Night of Ramadan"
    override val eventEidFitr = "Eid al-Fitr (1 Shawwal)"
    override val eventArafah = "Day of Arafah (9 Dhul Hijjah)"
    override val eventEidAdha = "Eid al-Adha (10 Dhul Hijjah)"
    override val hijriMonths = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul Qi'dah", "Dhul Hijjah"
    )

    override val notebookTitle = "Islamic Notebook"
    override val notebookTotalNotes = "Total Notes"
    override val notebookFavorites = "Favorites"
    override val notebookPinned = "Pinned"
    override val notebookRecycleBin = "Recycle Bin"
    override val notebookRecentlyUpdated = "Recently Updated"
    override val notebookAddNote = "New Note"
    override val notebookSearchPlaceholder = "Search notes, tags, books, or Hadith refs..."
    override val notebookEmptyTitle = "Your Islamic Notebook is empty"
    override val notebookEmptySubtitle = "Record Quran reflections, Friday khutbah takeaways, Hadith studies, and Ramadan goals offline."
    override val notebookRecycleEmptyTitle = "Recycle Bin is empty"
    override val notebookRecycleEmptySubtitle = "Deleted notes appear here so you can restore them anytime."
    override val notebookManageCategories = "Categories"
    override val notebookCategoryNameEn = "Category Name (English)"
    override val notebookCategoryNameBn = "Category Name (Bengali)"
    override val notebookCreateCategory = "Add Custom Category"
    override val notebookRenameCategory = "Rename Category"
    override val notebookDeleteCategoryTitle = "Delete Custom Category"
    override val notebookDeleteCategoryBody = "Existing notes in this category will never be deleted. Choose where to move them:"
    override val notebookMoveToCategoryLabel = "Move notes to"
    override val notebookNoteTitleLabel = "Note Title"
    override val notebookNoteDescriptionLabel = "Short Summary / Description (optional)"
    override val notebookNoteContentLabel = "Write your reflection in Arabic, Bengali, or English..."
    override val notebookTagsLabel = "Tags (comma-separated)"
    override val notebookCategoryLabel = "Category"
    override val notebookReferencesSection = "Islamic References (Optional)"
    override val notebookHadithCollection = "Hadith Collection"
    override val notebookHadithNumber = "Hadith Number"
    override val notebookDuaSource = "Dua Source"
    override val notebookIslamicBook = "Islamic Book Name"
    override val notebookPageNumber = "Page Number"
    override val notebookWebsiteUrl = "Website URL"
    override val notebookReferenceDisclaimer = "Personal study notes are user-authored and are never automatically marked as authentic or attributed as verified prophetic quotes."
    override val notebookCreatedLabel = "Created"
    override val notebookUpdatedLabel = "Updated"
    override val notebookAutosaveSaving = "Saving locally..."
    override val notebookAutosaveSaved = "Saved"
    override val notebookAutosaveError = "Save failed — draft preserved"
    override val notebookRtlArabicToggle = "RTL Arabic"
    override val notebookPreviewToggle = "Preview"
    override val notebookEditToggle = "Edit"
    override val notebookRestore = "Restore"
    override val notebookDeleteForever = "Delete Permanently"
    override val notebookEmptyBin = "Empty Bin"
    override val notebookRecoverDraftTitle = "Unsaved Note Draft Found"
    override val notebookRecoverDraftBody = "Would you like to recover your previous unfinished note?"
    override val notebookRecoverAction = "Recover"
    override val notebookDiscardAction = "Discard"
    override val notebookSortUpdatedDesc = "Recently Updated"
    override val notebookSortUpdatedAsc = "Oldest Updated"
    override val notebookSortCreatedDesc = "Newest Created"
    override val notebookSortTitleAsc = "Title A–Z"
    override val notebookPinPromptTitle = "Notebook Locked"
    override val notebookPinPromptBody = "Enter your 4-digit PIN to view your private local notes."
    override val notebookUnlockAction = "Unlock"
    override val notebookWrongPin = "Incorrect PIN. Please try again."

    override val locationTimeSection = "Location & Time"
    override val appearanceSection = "Appearance & Typography"
    override val personalizationSection = "Personalization"
    override val privacySection = "Privacy & Trust"
    override val languageLabel = "App Language"
    override val themeLabel = "Theme"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val prayerSettingsSection = "Prayer & Calendar Settings"
    override val locationLabel = "Selected Location"
    override val timezoneLabel = "Selected Timezone"
    override val fontSettingsLabel = "Font Settings"
    override val notebookDefaultCatLabel = "Default Notebook Category"
    override val notebookAutosavePrefLabel = "Notebook Autosave"
    override val notebookPinPrefLabel = "Notebook PIN Lock"
    override val notebookPinSet = "Enabled (4-digit local PIN)"
    override val notebookPinDisabled = "Disabled"
    override val notebookPinEnterNew = "Enter 4-digit PIN"
    override val notebookPinRemove = "Disable PIN Lock"
    override val notebookPrivacyInfoTitle = "Notebook Privacy"
    override val notebookPrivacyInfoBody = "All personal notes and custom categories are stored exclusively on your device inside the local Room database. Notes are never uploaded, logged, or sent to analytics. Database files are unencrypted standard SQLite; enabling the 4-digit PIN adds an in-app screen lock."
    override val localStorageInfoTitle = "Local Data Storage"
    override val localStorageInfoBody = "Prayer schedules, bookmarks, Tasbih sessions, Prayer Tracker logs, location preferences, timezone choices, and Notebook entries stay on your device."
    override val aboutSection = "About & Trust"
    override val appVersionLabel = "Version"
    override val contentVerificationPolicyTitle = "Religious Content Integrity"
    override val contentVerificationPolicyBody = "Arabic Quran excerpts, Hadiths, and Duas in DEEN are checked against primary collections (Sahih al-Bukhari, Sahih Muslim, Sunan Abi Dawud, Jami' at-Tirmidhi, Sunan Ibn Majah, and Sunnah.com) with explicit authenticity grades and verification status."
    override val privacyPolicyTitle = "Privacy Policy"
    override val privacyPolicyBody = "DEEN operates offline on your device. Location coordinates are used strictly on-device to compute prayer times and Qibla bearing. Your prayer logs, Tasbih sessions, bookmarks, and personal notes remain stored locally on your device."
    override val aboutDeenTitle = "About DEEN"
    override val aboutDeenBody = "DEEN is a calm, accurate, bilingual (Bengali & English) Islamic companion built for authentic daily worship, Bangladesh 64-district prayer guidance, and private Islamic study."
}

object BengaliStrings : AppStrings {
    override val appTitle = "দ্বীন (DEEN)"
    override val navPrayer = "হোম"
    override val navQibla = "কিবলা"
    override val navLibrary = "হাদিস ও দোয়া"
    override val navWorship = "ইবাদত"
    override val navCalendar = "ক্যালেন্ডার"
    override val settingsTitle = "সেটিংস"
    override val back = "ফিরে যান"
    override val close = "বন্ধ করুন"
    override val save = "সংরক্ষণ"
    override val cancel = "বাতিল"
    override val reset = "রিসেট"
    override val delete = "মুছুন"
    override val add = "যোগ করুন"
    override val share = "শেয়ার"
    override val edit = "সম্পাদনা"
    override val searchPlaceholder = "আরবি, অর্থ বা রেফারেন্স খুঁজুন..."
    override val clearSearch = "অনুসন্ধান মুছুন"
    override val noResultsTitle = "কোনো তথ্য পাওয়া যায়নি"
    override val noResultsSubtitle = "অন্য কোনো শব্দ, কিতাব বা বিভাগ দিয়ে চেষ্টা করুন।"
    override val loading = "লোড হচ্ছে..."

    override val homeFeaturesTitle = "ইসলামি দৈনন্দিন টুলস"
    override val featurePrayerTimes = "নামাজের সময়"
    override val featureQibla = "কিবলা"
    override val featureQuran = "কুরআন"
    override val featureHadith = "হাদিস"
    override val featureDuaAzkar = "দোয়া ও আযকার"
    override val featureTasbih = "ডিজিটাল তাসবীহ"
    override val featurePrayerTracker = "নামাজ ট্র্যাকার"
    override val featureCalendar = "হিজরি ক্যালেন্ডার"
    override val featureNotebook = "ইসলামিক নোটবুক"
    override val featureLocation = "অবস্থান"
    override val featureTimezone = "টাইমজোন"
    override val featureSettings = "সেটিংস"
    override val homeNotesCountFormat = "%dটি নোট"
    override val homePrayedTodayFormat = "%d/৫ আদায়"
    override val homeTapToChangeLocation = "বিভাগ বা জেলা পরিবর্তন করতে ট্যাপ করুন"

    override val quranTitle = "প্রয়োজনীয় সূরা ও আয়াত"
    override val quranSubtitle = "বাংলা ও ইংরেজি অর্থসহ যাচাইকৃত সূরা আল-ফাতিহা, আয়াতুল কুরসী ও কুল সূরাসমূহ।"
    override val quranReflectInNotebook = "নোটবুকে লিখুন"

    override val fajr = "ফজর"
    override val sunrise = "সূর্যোদয়"
    override val dhuhr = "যোহর"
    override val asr = "আসর"
    override val maghrib = "মাগরিব"
    override val isha = "ইশা"
    override val nextPrayerLabel = "পরবর্তী ওয়াক্ত"
    override val startsIn = "বাকি সময়"
    override val prayerTimetableTitle = "দ্বীন সঙ্গী"
    override val monthlyTimetable = "মাসিক সময়সূচি"
    override val viewMonthlySchedule = "৩০ দিনের সময়সূচি"
    override val todaySchedule = "আজকের সময়সূচি"
    override val detectLocation = "জিপিএস অবস্থান নিন"
    override val selectCity = "অবস্থান নির্ধারণ"
    override val locationPermissionNeeded = "সঠিক নামাজের সময় ও কিবলার জন্য অবস্থানের অনুমতি প্রয়োজন।"
    override val locationPermissionRationale = "আপনি অবস্থানের অনুমতি দিতে পারেন অথবা ম্যানুয়ালি জেলা নির্বাচন করতে পারেন।"
    override val grantPermission = "অনুমতি দিন"
    override val calculationMethodLabel = "হিসাব পদ্ধতি"
    override val madhabLabel = "আসর মাযহাব পদ্ধতি"
    override val madhabStandard = "জুমহুর (শাফেয়ী, মালেকী, হাম্বলী)"
    override val madhabHanafi = "হানাফী (ছায়া ২ গুণ)"
    override val highLatitudeLabel = "উচ্চ অক্ষাংশ সমন্বয়"
    override val prayerNotificationsTitle = "নামাজের অনুস্মারক"
    override val notificationPermissionNeeded = "নামাজের ওয়াক্তের অ্যালার্ট পেতে নোটিফিকেশন অনুমতি দিন।"
    override val azanSoundLabel = "অ্যালার্ট শব্দ"
    override val soundDefault = "ডিফল্ট নোটিফিকেশন"
    override val soundChime = "মৃদু ধ্বনি"
    override val soundSilent = "নীরব বিজ্ঞপ্তি"
    override val notificationBodyFormat = "%s নামাজের ওয়াক্ত হয়েছে (%s)।"
    override val alarmScheduleWarning = "এই ডিভাইসে সঠিক সময়ের অ্যালার্ম সীমিত; সিস্টেম অনুযায়ী কিছুটা দেরি হতে পারে।"

    override val locationSettingsTitle = "অবস্থান নির্বাচন"
    override val locationModeLabel = "অবস্থানের ধরন"
    override val locationModeAuto = "স্বয়ংক্রিয় (জিপিএস)"
    override val locationModeManual = "ম্যানুয়াল নির্বাচন"
    override val countryLabel = "দেশ"
    override val divisionLabel = "বিভাগ (৮টি বিভাগ)"
    override val districtLabel = "জেলা (৬৪টি জেলা)"
    override val upazilaLabel = "উপজেলা (ঐচ্ছিক)"
    override val upazilaOptionalNone = "জেলা সদর (উপজেলা ছাড়া)"
    override val searchDivisionPlaceholder = "বিভাগ খুঁজুন..."
    override val searchDistrictPlaceholder = "৬৪ জেলার যেকোনোটি খুঁজুন..."
    override val confirmLocation = "অবস্থান নিশ্চিত করুন"
    override val resetToDefaultDhaka = "ডিফল্ট ঢাকায় ফিরুন"
    override val gpsDetectedBadge = "জিপিএস স্থানাঙ্ক"
    override val referenceCoordsNotice = "যাচাইকৃত জেলা/উপজেলা রেফারেন্স স্থানাঙ্ক ব্যবহৃত হচ্ছে (সরাসরি জিপিএস নয়)।"
    override val activeLocationSummary = "বর্তমান নির্বাচিত অবস্থান"
    override val useManualFallback = "ম্যানুয়াল নির্বাচনে যান"
    override val coordinatesLabel = "স্থানাঙ্ক"

    override val timezoneSettingsTitle = "টাইমজোন সেটিংস"
    override val timezoneModeLabel = "টাইমজোন মোড"
    override val timezoneModeAuto = "ডিভাইস স্বয়ংক্রিয়"
    override val timezoneModeManual = "ম্যানুয়াল IANA টাইমজোন"
    override val resetDeviceTimezone = "ডিভাইস টাইমজোনে ফিরুন"
    override val useBangladeshDhakaTz = "Asia/Dhaka (UTC+06:00) ব্যবহার করুন"
    override val searchTimezonePlaceholder = "IANA কোড, শহর বা UTC অফসেট খুঁজুন..."
    override val currentLocalPreview = "স্থানীয় সময়ের পূর্বরূপ"
    override val dstSupportedLabel = "ডিএসটি সমর্থিত"
    override val dstNotUsedLabel = "স্ট্যান্ডার্ড সময়"
    override val selectedTimezoneIndicator = "সক্রিয়"
    override val bangladeshTzNotice = "বাংলাদেশের ৬৪টি জেলাতেই একই জাতীয় সময় Asia/Dhaka (UTC+06:00) প্রযোজ্য।"

    override val fontSettingsTitle = "ফন্ট ও লেখার আকার"
    override val fontPreviewSection = "তাৎক্ষণিক ফন্ট পূর্বরূপ"
    override val fontAppSizeLabel = "অ্যাপের ফন্ট সাইজ"
    override val fontSizeSmall = "ছোট"
    override val fontSizeMedium = "মাঝারি"
    override val fontSizeLarge = "বড়"
    override val fontSizeExtraLarge = "অতিরিক্ত বড়"
    override val fontArabicSizeLabel = "আরবি লেখার আকার"
    override val fontBengaliSizeLabel = "বাংলা লেখার আকার"
    override val fontEnglishSizeLabel = "ইংরেজি লেখার আকার"
    override val fontLatinFamilyLabel = "ইংরেজি ফন্ট স্টাইল"
    override val resetFontDefaults = "ডিফল্ট রিসেট করুন"

    override val qiblaTitle = "কিবলা কম্পাস"
    override val qiblaDirectionLabel = "কিবলার দিক"
    override val distanceToKaaba = "কাবা শরীফের দূরত্ব"
    override val kmUnit = "কিমি"
    override val alignedWithQibla = "কিবলামুখী সঠিক দিক"
    override val turnRight = "সামান্য ডানে ঘোরান"
    override val turnLeft = "সামান্য বামে ঘোরান"
    override val calibrateCompassTitle = "কম্পাস ক্যালিব্রেশন"
    override val calibrateCompassHint = "চুম্বকীয় কেস বা ধাতু থেকে দূরে রেখে ফোনটি ৮ (figure-8) আকারে ঘোরান।"
    override val sensorAccuracyLow = "নির্ভুলতা কম — ক্যালিব্রেট করুন"
    override val sensorAccuracyMedium = "মাঝারি নির্ভুলতা"
    override val sensorAccuracyHigh = "উচ্চ নির্ভুলতা"
    override val noCompassSensorTitle = "কম্পাস সেন্সর নেই"
    override val noCompassSensorBody = "এই ডিভাইসে ম্যাগনেটোমিটার সেন্সর পাওয়া যায়নি। নিচের প্রকৃত উত্তর কোণ অনুসরণ করুন।"
    override val bearingFromNorth = "প্রকৃত উত্তর থেকে কোণ"

    override val libraryTitle = "হাদিস ও দোয়া"
    override val tabHadith = "হাদিস"
    override val tabDuaAzkar = "দোয়া ও আযকার"
    override val tabBookmarks = "সংরক্ষিত"
    override val allCategories = "সবগুলো"
    override val catMorningAzkar = "সকালের আযকার"
    override val catEveningAzkar = "সন্ধ্যার আযকার"
    override val catAfterPrayer = "নামাজের পর"
    override val catDailyDua = "দৈনন্দিন দোয়া"
    override val catSleepDua = "ঘুমের দোয়া"
    override val catTravelDua = "সফরের দোয়া"
    override val narratorLabel = "বর্ণনাকারী"
    override val referenceLabel = "রেফারেন্স"
    override val gradeLabel = "মান (হুকুম)"
    override val gradedByLabel = "তাহকীক সূত্র"
    override val pronunciationLabel = "বাংলা উচ্চারণ"
    override val bengaliMeaningLabel = "বাংলা অর্থ"
    override val englishMeaningLabel = "ইংরেজি অর্থ"
    override val contextLabel = "পাঠের সময় ও প্রেক্ষাপট"
    override val repeatCountFormat = "%d বার পাঠ করুন"
    override val tapToRecite = "গণনা করতে ট্যাপ করুন"
    override val reciteCompleted = "সম্পন্ন হয়েছে"
    override val bookmarkAdd = "সংরক্ষণ করুন"
    override val bookmarkRemove = "সংরক্ষণ সরান"
    override val noBookmarksTitle = "এখনও কোনো কিছু সংরক্ষিত নেই"
    override val noBookmarksSubtitle = "অফলাইনে দ্রুত পড়ার জন্য যেকোনো হাদিস বা দোয়া বুকমার্ক করুন।"
    override val verificationBadgeVerified = "মূল উৎস যাচাইকৃত"
    override val verificationBadgePending = "আলেম পর্যালোচনাধীন"
    override val verificationBadgeUnverified = "অযাচাইকৃত"
    override val verificationDetailsTitle = "তথ্য যাচাইকরণ বিবরণ"
    override val sourceUrlLabel = "মূল উৎস লিংক"
    override val lastVerifiedLabel = "সর্বশেষ যাচাই"

    override val worshipTitle = "ইবাদত"
    override val tabTasbih = "ডিজিটাল তাসবীহ"
    override val tabPrayerTracker = "নামাজ ট্র্যাকার"
    override val targetLabel = "লক্ষ্যমাত্রা"
    override val customTarget = "কাস্টম"
    override val enterCustomTarget = "লক্ষ্য সংখ্যা লিখুন"
    override val addCustomDhikr = "নতুন যিকির"
    override val dhikrTitleEn = "ইংরেজি নাম"
    override val dhikrTitleBn = "বাংলা নাম"
    override val dhikrArabic = "আরবি পাঠ"
    override val dailyTotalLabel = "আজকের মোট"
    override val sessionHistoryTitle = "সম্পন্ন সেশনসমূহ"
    override val noSessionsYet = "তাসবীহ ইতিহাস সংরক্ষণ করতে একটি চক্র পূর্ণ করুন।"
    override val hapticFeedbackLabel = "ভাইব্রেশন ফিডব্যাক"
    override val tapCounterHint = "গণনা করতে বৃত্তে ট্যাপ করুন"
    override val trackerTodayTitle = "দৈনিক নামাজের হিসাব"
    override val trackerStatusUnmarked = "চিহ্নিত নয়"
    override val trackerStatusCompleted = "আদায়"
    override val trackerStatusMissed = "কাযা / মিস"
    override val markCompleted = "আদায়"
    override val markMissed = "মিস"
    override val clearStatus = "মুছুন"
    override val weeklyStatsTitle = "গত ৭ দিন"
    override val monthlyStatsTitle = "গত ৩০ দিন"
    override val completedCountFormat = "%d ওয়াক্ত আদায়"
    override val missedCountFormat = "%d ওয়াক্ত মিস"
    override val manualTrackerNote = "নামাজ কখনো স্বয়ংক্রিয়ভাবে পূর্ণ ধরা হয় না; প্রতিটি ওয়াক্ত নিজে চিহ্নিত করুন।"

    override val calendarTitle = "হিজরি ক্যালেন্ডার"
    override val hijriTodayLabel = "হিজরি তারিখ"
    override val hijriOffsetLabel = "চাঁদ দেখা সমন্বয়"
    override val hijriDaysUnit = "দিন"
    override val predictedMoonNote = "জ্যোতির্বিজ্ঞান ভিত্তিক সম্ভাব্য তারিখ। স্থানীয় চাঁদ দেখার ঘোষণার সাথে মেলাতে দিন সমন্বয় করুন।"
    override val importantDatesTitle = "গুরুত্বপূর্ণ ইসলামি দিবস"
    override val eventNewYear = "ইসলামি নববর্ষ (১ মহররম)"
    override val eventAshura = "পবিত্র আশুরা (১০ মহররম)"
    override val eventRamadanStart = "রমজান শুরু (১ রমজান)"
    override val eventLaylatulQadr = "২৭শে রমজানের রাত"
    override val eventEidFitr = "ঈদুল ফিতর (১ শাওয়াল)"
    override val eventArafah = "আরাফার দিন (৯ জিলহজ)"
    override val eventEidAdha = "ঈদুল আযহা (১০ জিলহজ)"
    override val hijriMonths = listOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    override val notebookTitle = "ইসলামিক নোটবুক"
    override val notebookTotalNotes = "মোট নোট"
    override val notebookFavorites = "প্রিয় নোট"
    override val notebookPinned = "পিন করা"
    override val notebookRecycleBin = "রিসাইকেল বিন"
    override val notebookRecentlyUpdated = "সাম্প্রতিক আপডেট"
    override val notebookAddNote = "নতুন নোট"
    override val notebookSearchPlaceholder = "নোট, ট্যাগ, বই বা হাদিস নম্বর খুঁজুন..."
    override val notebookEmptyTitle = "আপনার ইসলামিক নোটবুক খালি"
    override val notebookEmptySubtitle = "কুরআন তাদাব্বুর, জুমার নোট, হাদিস পড়া ও রমজানের আমল পরিকল্পনা অফলাইনে সংরক্ষণ করুন।"
    override val notebookRecycleEmptyTitle = "রিসাইকেল বিন খালি"
    override val notebookRecycleEmptySubtitle = "মুছে ফেলা নোট এখানে থাকবে যাতে প্রয়োজনে পুনরুদ্ধার করা যায়।"
    override val notebookManageCategories = "ক্যাটাগরি"
    override val notebookCategoryNameEn = "ক্যাটাগরির নাম (ইংরেজি)"
    override val notebookCategoryNameBn = "ক্যাটাগরির নাম (বাংলা)"
    override val notebookCreateCategory = "নতুন ক্যাটাগরি যোগ করুন"
    override val notebookRenameCategory = "নাম পরিবর্তন করুন"
    override val notebookDeleteCategoryTitle = "কাস্টম ক্যাটাগরি মুছুন"
    override val notebookDeleteCategoryBody = "এই ক্যাটাগরির কোনো নোট মুছে ফেলা হবে না। নোটগুলো কোন ক্যাটাগরিতে নেবেন নির্বাচন করুন:"
    override val notebookMoveToCategoryLabel = "নোট স্থানান্তর করুন"
    override val notebookNoteTitleLabel = "নোটের শিরোনাম"
    override val notebookNoteDescriptionLabel = "সংক্ষিপ্ত বিবরণ (ঐচ্ছিক)"
    override val notebookNoteContentLabel = "আরবি, বাংলা বা ইংরেজিতে আপনার নোট লিখুন..."
    override val notebookTagsLabel = "ট্যাগ (কমা দিয়ে আলাদা করুন)"
    override val notebookCategoryLabel = "ক্যাটাগরি"
    override val notebookReferencesSection = "ইসলামি রেফারেন্স (ঐচ্ছিক)"
    override val notebookHadithCollection = "হাদিস গ্রন্থ"
    override val notebookHadithNumber = "হাদিস নম্বর"
    override val notebookDuaSource = "দোয়ার উৎস"
    override val notebookIslamicBook = "ইসলামি বইয়ের নাম"
    override val notebookPageNumber = "পৃষ্ঠা নম্বর"
    override val notebookWebsiteUrl = "ওয়েবসাইট লিংক"
    override val notebookReferenceDisclaimer = "ব্যক্তিগত নোট ব্যবহারকারীর নিজস্ব লেখা; এগুলো স্বয়ংক্রিয়ভাবে সহীহ বা হাদিসের উদ্ধৃতি হিসেবে চিহ্নিত করা হয় না।"
    override val notebookCreatedLabel = "তৈরি"
    override val notebookUpdatedLabel = "আপডেট"
    override val notebookAutosaveSaving = "সংরক্ষণ হচ্ছে..."
    override val notebookAutosaveSaved = "সংরক্ষিত"
    override val notebookAutosaveError = "সংরক্ষণ ব্যর্থ — খসড়া রাখা আছে"
    override val notebookRtlArabicToggle = "RTL আরবি"
    override val notebookPreviewToggle = "প্রিভিউ"
    override val notebookEditToggle = "সম্পাদনা"
    override val notebookRestore = "পুনরুদ্ধার"
    override val notebookDeleteForever = "স্থায়ীভাবে মুছুন"
    override val notebookEmptyBin = "বিন খালি করুন"
    override val notebookRecoverDraftTitle = "অসংরক্ষিত খসড়া পাওয়া গেছে"
    override val notebookRecoverDraftBody = "আগের অসম্পূর্ণ নোটটি কি পুনরুদ্ধার করতে চান?"
    override val notebookRecoverAction = "পুনরুদ্ধার করুন"
    override val notebookDiscardAction = "বাতিল করুন"
    override val notebookSortUpdatedDesc = "সাম্প্রতিক আপডেট"
    override val notebookSortUpdatedAsc = "পুরনো আপডেট"
    override val notebookSortCreatedDesc = "নতুন তৈরি"
    override val notebookSortTitleAsc = "শিরোনাম ক্রম"
    override val notebookPinPromptTitle = "নোটবুক লক করা"
    override val notebookPinPromptBody = "আপনার ব্যক্তিগত নোট দেখতে ৪-সংখ্যার পিন লিখুন।"
    override val notebookUnlockAction = "আনলক করুন"
    override val notebookWrongPin = "ভুল পিন। আবার চেষ্টা করুন।"

    override val locationTimeSection = "অবস্থান ও সময়"
    override val appearanceSection = "দৃশ্যমানতা ও ফন্ট"
    override val personalizationSection = "ব্যক্তিগত পছন্দ"
    override val privacySection = "গোপনীয়তা ও সুরক্ষা"
    override val languageLabel = "অ্যাপের ভাষা"
    override val themeLabel = "থিম মোড"
    override val themeSystem = "সিস্টেম"
    override val themeLight = "লাইট"
    override val themeDark = "ডার্ক"
    override val prayerSettingsSection = "নামাজ ও ক্যালেন্ডার সেটিংস"
    override val locationLabel = "নির্বাচিত অবস্থান"
    override val timezoneLabel = "নির্বাচিত টাইমজোন"
    override val fontSettingsLabel = "ফন্ট ও লেখার আকার"
    override val notebookDefaultCatLabel = "নোটবুকের ডিফল্ট ক্যাটাগরি"
    override val notebookAutosavePrefLabel = "নোটবুক অটোসেভ"
    override val notebookPinPrefLabel = "নোটবুক পিন লক"
    override val notebookPinSet = "সক্রিয় (৪-সংখ্যার পিন)"
    override val notebookPinDisabled = "নিষ্ক্রিয়"
    override val notebookPinEnterNew = "৪-সংখ্যার পিন লিখুন"
    override val notebookPinRemove = "পিন লক বন্ধ করুন"
    override val notebookPrivacyInfoTitle = "নোটবুক গোপনীয়তা"
    override val notebookPrivacyInfoBody = "আপনার সকল ব্যক্তিগত নোট ও কাস্টম ক্যাটাগরি শুধুমাত্র আপনার ডিভাইসের Room ডাটাবেসে সংরক্ষিত থাকে। নোট কখনো আপলোড, লগ বা অ্যানালিটিক্সে পাঠানো হয় না। ডাটাবেস এনক্রিপ্ট করা নয়; ৪-সংখ্যার পিন কেবল অ্যাপের ভেতরে লক স্ক্রিন যুক্ত করে।"
    override val localStorageInfoTitle = "লোকাল ডাটা সংরক্ষণ"
    override val localStorageInfoBody = "নামাজের সময়সূচি, বুকমার্ক, তাসবীহ সেশন, নামাজ ট্র্যাকার, অবস্থান, টাইমজোন ও নোটবুকের তথ্য আপনার ফোনেই নিরাপদে থাকে।"
    override val aboutSection = "অ্যাপ তথ্য ও নীতিমালা"
    override val appVersionLabel = "সংস্করণ"
    override val contentVerificationPolicyTitle = "ইসলামি তথ্যের বিশুদ্ধতা নীতি"
    override val contentVerificationPolicyBody = "দ্বীন অ্যাপের কুরআন, হাদিস ও দোয়া মূল গ্রন্থ (সহীহ বুখারী, সহীহ মুসলিম, সুনানে আবু দাউদ, জামে তিরমিযী, সুনানে ইবনে মাজাহ ও Sunnah.com) যাচাই করে মান ও রেফারেন্সসহ সংযুক্ত করা হয়েছে।"
    override val privacyPolicyTitle = "গোপনীয়তা নীতি"
    override val privacyPolicyBody = "দ্বীন সম্পূর্ণ অফলাইন-বান্ধব অ্যাপ। আপনার অবস্থানের তথ্য কেবল আপনার ডিভাইসেই নামাজের সময় ও কিবলা নির্ণয়ের জন্য ব্যবহৃত হয়। আপনার নামাজের হিসাব, তাসবীহ, বুকমার্ক ও নোট কেবল আপনার ফোনেই সংরক্ষিত থাকে।"
    override val aboutDeenTitle = "দ্বীন সম্পর্কে"
    override val aboutDeenBody = "দ্বীন হলো বাংলা ও ইংরেজি ভাষায় বিশুদ্ধ হাদিস, দোয়া, বাংলাদেশের ৬৪ জেলার নামাজের সময়, কিবলা কম্পাস, হিজরি ক্যালেন্ডার ও ব্যক্তিগত ইসলামিক নোটবুকের নির্ভরযোগ্য সঙ্গী।"
}

fun stringsFor(language: AppLanguage): AppStrings =
    when (language) {
        AppLanguage.ENGLISH -> EnglishStrings
        AppLanguage.BENGALI -> BengaliStrings
    }

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnglishStrings }
