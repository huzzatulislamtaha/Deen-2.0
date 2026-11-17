package com.example.domain.model

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

enum class AzanSoundOption(val key: String) {
    DEFAULT_NOTIFICATION("default"),
    GENTLE_CHIME("chime"),
    SILENT("silent");

    companion object {
        fun fromKey(key: String?): AzanSoundOption = entries.firstOrNull { it.key == key } ?: DEFAULT_NOTIFICATION
    }
}

enum class LocationMode(val key: String) {
    AUTOMATIC("automatic"),
    MANUAL("manual");

    companion object {
        fun fromKey(key: String?): LocationMode = entries.firstOrNull { it.key == key } ?: MANUAL
    }
}

enum class TimezoneMode(val key: String) {
    AUTOMATIC("automatic"),
    MANUAL("manual");

    companion object {
        fun fromKey(key: String?): TimezoneMode = entries.firstOrNull { it.key == key } ?: MANUAL
    }
}

enum class FontScalePreset(val key: String, val scaleFactor: Float) {
    SMALL("small", 0.88f),
    MEDIUM("medium", 1.00f),
    LARGE("large", 1.14f),
    EXTRA_LARGE("extra_large", 1.28f);

    companion object {
        fun fromKey(key: String?): FontScalePreset =
            entries.firstOrNull { it.key == key } ?: MEDIUM
    }
}

enum class LatinFontChoice(val key: String, val displayName: String) {
    PLUS_JAKARTA("plus_jakarta", "Plus Jakarta Sans"),
    INTER("inter", "Inter");

    companion object {
        fun fromKey(key: String?): LatinFontChoice =
            entries.firstOrNull { it.key == key } ?: PLUS_JAKARTA
    }
}

data class DeenFontConfig(
    val appScalePreset: FontScalePreset = FontScalePreset.MEDIUM,
    val arabicFontScale: Float = 1.0f,
    val bengaliFontScale: Float = 1.0f,
    val englishFontScale: Float = 1.0f,
    val latinFontChoice: LatinFontChoice = LatinFontChoice.PLUS_JAKARTA
) {
    val effectiveEnglishScale: Float
        get() = (appScalePreset.scaleFactor * englishFontScale).coerceIn(0.75f, 1.85f)

    val effectiveBengaliScale: Float
        get() = (appScalePreset.scaleFactor * bengaliFontScale).coerceIn(0.75f, 1.85f)

    val effectiveArabicScale: Float
        get() = (appScalePreset.scaleFactor * arabicFontScale).coerceIn(0.75f, 1.95f)
}

enum class BdDivision(
    val id: String,
    val nameEn: String,
    val nameBn: String
) {
    DHAKA("div_dhaka", "Dhaka", "ঢাকা"),
    CHATTOGRAM("div_chattogram", "Chattogram", "চট্টগ্রাম"),
    RAJSHAHI("div_rajshahi", "Rajshahi", "রাজশাহী"),
    KHULNA("div_khulna", "Khulna", "খুলনা"),
    BARISHAL("div_barishal", "Barishal", "বরিশাল"),
    SYLHET("div_sylhet", "Sylhet", "সিলেট"),
    RANGPUR("div_rangpur", "Rangpur", "রংপুর"),
    MYMENSINGH("div_mymensingh", "Mymensingh", "ময়মনসিংহ");

    companion object {
        fun fromId(id: String?): BdDivision? = entries.firstOrNull { it.id == id }
    }
}

data class BdUpazila(
    val id: String,
    val districtId: String,
    val nameEn: String,
    val nameBn: String,
    val latitude: Double,
    val longitude: Double
)

data class BdDistrict(
    val id: String,
    val division: BdDivision,
    val nameEn: String,
    val nameBn: String,
    val latitude: Double,
    val longitude: Double,
    val upazilas: List<BdUpazila> = emptyList()
)

data class CountryOption(
    val code: String,
    val nameEn: String,
    val nameBn: String,
    val defaultTimeZoneId: String
)

data class TimezoneEntry(
    val id: String,
    val displayName: String,
    val offsetLabel: String,
    val offsetMillis: Int,
    val currentTimeFormatted: String,
    val usesDst: Boolean,
    val regionTag: String = ""
)

enum class NoteSortOrder(val key: String) {
    UPDATED_DESC("updated_desc"),
    UPDATED_ASC("updated_asc"),
    CREATED_DESC("created_desc"),
    TITLE_ASC("title_asc")
}

enum class NotebookFilter(val key: String) {
    ALL("all"),
    PINNED("pinned"),
    FAVORITES("favorites"),
    RECYCLE_BIN("recycle_bin")
}

enum class NoteSaveStatus {
    IDLE,
    SAVING,
    SAVED,
    ERROR
}

enum class PrayerName(val key: String, val isObligatory: Boolean) {
    FAJR("fajr", true),
    SUNRISE("sunrise", false),
    DHUHR("dhuhr", true),
    ASR("asr", true),
    MAGHRIB("maghrib", true),
    ISHA("isha", true);

    companion object {
        val obligatoryPrayers: List<PrayerName> = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
        fun fromKey(key: String?): PrayerName? = entries.firstOrNull { it.key == key }
    }
}

enum class CalculationMethod(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    KARACHI("karachi", "Univ. of Islamic Sciences, Karachi (BD)", "করাচি বিশ্ববিদ্যালয় (বাংলাদেশ)", 18.0, 18.0),
    MWL("mwl", "Muslim World League", "মুসলিম ওয়ার্ল্ড লিগ", 18.0, 17.0),
    ISNA("isna", "ISNA (North America)", "ইসনা (উত্তর আমেরিকা)", 15.0, 15.0),
    EGYPT("egypt", "Egyptian General Authority", "মিশরীয় কর্তৃপক্ষ", 19.5, 17.5),
    UMM_AL_QURA("umm_al_qura", "Umm al-Qura University, Makkah", "উম্মুল কুরা, মক্কা", 18.5, 0.0, 90),
    SINGAPORE("singapore", "MUIS (Singapore / SE Asia)", "মুইস (সিঙ্গাপুর)", 20.0, 18.0),
    TEHRAN("tehran", "Inst. of Geophysics, Tehran", "তেহরান ইনস্টিটিউট", 17.7, 14.0);

    companion object {
        fun fromKey(key: String?): CalculationMethod = entries.firstOrNull { it.key == key } ?: KARACHI
    }
}

enum class Madhab(val key: String, val shadowFactor: Int) {
    STANDARD("standard", 1),
    HANAFI("hanafi", 2);

    companion object {
        fun fromKey(key: String?): Madhab = entries.firstOrNull { it.key == key } ?: HANAFI
    }
}

enum class HighLatitudeRule(val key: String, val titleEn: String, val titleBn: String) {
    NONE("none", "None", "প্রযোজ্য নয়"),
    MIDDLE_OF_THE_NIGHT("middle_night", "Middle of the Night", "মধ্যরাত পদ্ধতি"),
    SEVENTH_OF_THE_NIGHT("seventh_night", "One-Seventh of Night", "রাতের এক-সপ্তমাংশ"),
    TWILIGHT_ANGLE("angle_based", "Angle-Based", "কোণ ভিত্তিক");

    companion object {
        fun fromKey(key: String?): HighLatitudeRule =
            entries.firstOrNull { it.key == key } ?: MIDDLE_OF_THE_NIGHT
    }
}

data class CityLocation(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val countryEn: String,
    val countryBn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val isGpsDetected: Boolean = false,
    val divisionNameEn: String = "",
    val divisionNameBn: String = "",
    val upazilaNameEn: String = "",
    val upazilaNameBn: String = "",
    val isReferenceCoordinate: Boolean = true
)

data class PrayerTimeEntry(
    val prayer: PrayerName,
    val timeMillis: Long,
    val formattedTime: String
)

data class DailyPrayerSchedule(
    val dateKey: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val entries: List<PrayerTimeEntry>
)

data class NextPrayerInfo(
    val prayer: PrayerName,
    val timeMillis: Long,
    val formattedTime: String,
    val remainingMillis: Long,
    val isTomorrow: Boolean
)

data class PrayerNotificationPrefs(
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val soundOption: AzanSoundOption = AzanSoundOption.DEFAULT_NOTIFICATION
) {
    fun isEnabled(prayer: PrayerName): Boolean = when (prayer) {
        PrayerName.FAJR -> fajrEnabled
        PrayerName.DHUHR -> dhuhrEnabled
        PrayerName.ASR -> asrEnabled
        PrayerName.MAGHRIB -> maghribEnabled
        PrayerName.ISHA -> ishaEnabled
        PrayerName.SUNRISE -> false
    }
}

enum class VerificationStatus(val key: String) {
    VERIFIED("VERIFIED"),
    PENDING_HUMAN_REVIEW("PENDING_HUMAN_REVIEW"),
    UNVERIFIED("UNVERIFIED");

    companion object {
        fun fromKey(key: String?): VerificationStatus =
            entries.firstOrNull { it.key == key } ?: UNVERIFIED
    }
}

enum class DuaCategory(val key: String) {
    MORNING_AZKAR("morning_azkar"),
    EVENING_AZKAR("evening_azkar"),
    AFTER_PRAYER("after_prayer"),
    DAILY_DUA("daily_dua"),
    SLEEP_DUA("sleep_dua"),
    TRAVEL_DUA("travel_dua");

    companion object {
        fun fromKey(key: String?): DuaCategory =
            entries.firstOrNull { it.key == key } ?: DAILY_DUA
    }
}

data class HadithItem(
    val id: String,
    val categoryEn: String,
    val categoryBn: String,
    val arabicText: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val sourceBook: String,
    val reference: String,
    val narratorEn: String,
    val narratorBn: String,
    val chapterEn: String,
    val chapterBn: String,
    val authenticityGrade: String,
    val gradingScholar: String,
    val sourceUrl: String,
    val verificationStatus: VerificationStatus,
    val lastVerifiedDate: String,
    val verificationNotes: String,
    val isBookmarked: Boolean = false
)

data class DuaItem(
    val id: String,
    val category: DuaCategory,
    val titleEn: String,
    val titleBn: String,
    val arabicText: String,
    val bengaliPronunciation: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val sourceBook: String,
    val reference: String,
    val authenticityGrade: String,
    val repetitionCount: Int,
    val contextEn: String,
    val contextBn: String,
    val sourceUrl: String,
    val verificationStatus: VerificationStatus,
    val lastVerifiedDate: String,
    val verificationNotes: String,
    val isBookmarked: Boolean = false
)

data class QuranExcerpt(
    val id: String,
    val surahNumber: Int,
    val ayahRange: String,
    val titleEn: String,
    val titleBn: String,
    val titleAr: String,
    val arabicText: String,
    val bengaliTranslation: String,
    val englishTranslation: String,
    val sourceNote: String,
    val sourceUrl: String
)

enum class PrayerStatus(val key: String) {
    UNMARKED("unmarked"),
    COMPLETED("completed"),
    MISSED("missed");

    companion object {
        fun fromKey(key: String?): PrayerStatus =
            entries.firstOrNull { it.key == key } ?: UNMARKED
    }
}

data class HijriDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,
    val isPredicted: Boolean = true
)

data class IslamicEvent(
    val id: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val gregorianDateKey: String,
    val gregorianLabel: String
)
