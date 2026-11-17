package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.i18n.AppLanguage
import com.example.domain.model.AzanSoundOption
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.DeenFontConfig
import com.example.domain.model.FontScalePreset
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.LatinFontChoice
import com.example.domain.model.LocationMode
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerNotificationPrefs
import com.example.domain.model.ThemeMode
import com.example.domain.model.TimezoneMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.TimeZone

private val Context.deenDataStore: DataStore<Preferences> by preferencesDataStore(name = "deen_prefs")

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val location: CityLocation = CityCatalog.defaultCity,
    val locationMode: LocationMode = LocationMode.MANUAL,
    val selectedCountryCode: String = "BD",
    val selectedDivisionId: String = "div_dhaka",
    val selectedDistrictId: String = "dist_dhaka",
    val selectedUpazilaId: String = "",
    val timezoneMode: TimezoneMode = TimezoneMode.MANUAL,
    val selectedTimezoneId: String = "Asia/Dhaka",
    val calculationMethod: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
    val hijriOffsetDays: Int = 0,
    val notificationPrefs: PrayerNotificationPrefs = PrayerNotificationPrefs(),
    val tasbihPresetId: String = "preset_subhanallah",
    val tasbihTarget: Int = 33,
    val tasbihCurrentCount: Int = 0,
    val tasbihHapticEnabled: Boolean = true,
    val fontConfig: DeenFontConfig = DeenFontConfig(),
    val notebookDefaultCategoryId: String = "cat_general",
    val notebookAutosaveEnabled: Boolean = true,
    val notebookPinHash: String = "",
    val unsavedNoteDraftJson: String = ""
) {
    val effectiveTimeZoneId: String
        get() = when (timezoneMode) {
            TimezoneMode.AUTOMATIC -> TimeZone.getDefault().id
            TimezoneMode.MANUAL -> selectedTimezoneId.ifBlank { location.timeZoneId.ifBlank { "Asia/Dhaka" } }
        }

    val effectiveLocation: CityLocation
        get() = if (location.timeZoneId == effectiveTimeZoneId) {
            location
        } else {
            location.copy(timeZoneId = effectiveTimeZoneId)
        }

    val isNotebookLockEnabled: Boolean
        get() = notebookPinHash.isNotBlank()
}

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CITY_ID = stringPreferencesKey("city_id")
        val CITY_NAME_EN = stringPreferencesKey("city_name_en")
        val CITY_NAME_BN = stringPreferencesKey("city_name_bn")
        val COUNTRY_EN = stringPreferencesKey("country_en")
        val COUNTRY_BN = stringPreferencesKey("country_bn")
        val DIVISION_EN = stringPreferencesKey("division_en")
        val DIVISION_BN = stringPreferencesKey("division_bn")
        val UPAZILA_EN = stringPreferencesKey("upazila_en")
        val UPAZILA_BN = stringPreferencesKey("upazila_bn")
        val IS_REFERENCE_COORD = booleanPreferencesKey("is_reference_coord")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val TIMEZONE_ID = stringPreferencesKey("timezone_id")
        val IS_GPS = booleanPreferencesKey("is_gps")
        val LOCATION_MODE = stringPreferencesKey("location_mode")
        val SELECTED_COUNTRY_CODE = stringPreferencesKey("selected_country_code")
        val SELECTED_DIVISION_ID = stringPreferencesKey("selected_division_id")
        val SELECTED_DISTRICT_ID = stringPreferencesKey("selected_district_id")
        val SELECTED_UPAZILA_ID = stringPreferencesKey("selected_upazila_id")
        val TIMEZONE_MODE = stringPreferencesKey("timezone_mode")
        val SELECTED_TIMEZONE_ID = stringPreferencesKey("selected_timezone_id")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val MADHAB = stringPreferencesKey("madhab")
        val HIGH_LAT = stringPreferencesKey("high_lat")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val NOTIF_FAJR = booleanPreferencesKey("notif_fajr")
        val NOTIF_DHUHR = booleanPreferencesKey("notif_dhuhr")
        val NOTIF_ASR = booleanPreferencesKey("notif_asr")
        val NOTIF_MAGHRIB = booleanPreferencesKey("notif_maghrib")
        val NOTIF_ISHA = booleanPreferencesKey("notif_isha")
        val AZAN_SOUND = stringPreferencesKey("azan_sound")
        val TASBIH_PRESET_ID = stringPreferencesKey("tasbih_preset_id")
        val TASBIH_TARGET = intPreferencesKey("tasbih_target")
        val TASBIH_COUNT = intPreferencesKey("tasbih_count")
        val TASBIH_HAPTIC = booleanPreferencesKey("tasbih_haptic")
        val FONT_SCALE_PRESET = stringPreferencesKey("font_scale_preset")
        val ARABIC_FONT_SCALE = floatPreferencesKey("arabic_font_scale")
        val BENGALI_FONT_SCALE = floatPreferencesKey("bengali_font_scale")
        val ENGLISH_FONT_SCALE = floatPreferencesKey("english_font_scale")
        val LATIN_FONT_CHOICE = stringPreferencesKey("latin_font_choice")
        val NOTEBOOK_DEFAULT_CATEGORY_ID = stringPreferencesKey("notebook_default_category_id")
        val NOTEBOOK_AUTOSAVE_ENABLED = booleanPreferencesKey("notebook_autosave_enabled")
        val NOTEBOOK_PIN_HASH = stringPreferencesKey("notebook_pin_hash")
        val UNSAVED_NOTE_DRAFT = stringPreferencesKey("unsaved_note_draft")
    }

    val userPreferences: Flow<UserPreferences> = context.deenDataStore.data.map { prefs ->
        val defaultCity = CityCatalog.defaultCity
        val city = CityLocation(
            id = prefs[Keys.CITY_ID] ?: defaultCity.id,
            nameEn = prefs[Keys.CITY_NAME_EN] ?: defaultCity.nameEn,
            nameBn = prefs[Keys.CITY_NAME_BN] ?: defaultCity.nameBn,
            countryEn = prefs[Keys.COUNTRY_EN] ?: defaultCity.countryEn,
            countryBn = prefs[Keys.COUNTRY_BN] ?: defaultCity.countryBn,
            latitude = prefs[Keys.LATITUDE] ?: defaultCity.latitude,
            longitude = prefs[Keys.LONGITUDE] ?: defaultCity.longitude,
            timeZoneId = prefs[Keys.TIMEZONE_ID] ?: defaultCity.timeZoneId,
            isGpsDetected = prefs[Keys.IS_GPS] ?: false,
            divisionNameEn = prefs[Keys.DIVISION_EN] ?: defaultCity.divisionNameEn,
            divisionNameBn = prefs[Keys.DIVISION_BN] ?: defaultCity.divisionNameBn,
            upazilaNameEn = prefs[Keys.UPAZILA_EN] ?: "",
            upazilaNameBn = prefs[Keys.UPAZILA_BN] ?: "",
            isReferenceCoordinate = prefs[Keys.IS_REFERENCE_COORD] ?: !(prefs[Keys.IS_GPS] ?: false)
        )
        UserPreferences(
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            themeMode = ThemeMode.fromKey(prefs[Keys.THEME_MODE]),
            location = city,
            locationMode = LocationMode.fromKey(prefs[Keys.LOCATION_MODE]),
            selectedCountryCode = prefs[Keys.SELECTED_COUNTRY_CODE] ?: "BD",
            selectedDivisionId = prefs[Keys.SELECTED_DIVISION_ID] ?: "div_dhaka",
            selectedDistrictId = prefs[Keys.SELECTED_DISTRICT_ID] ?: "dist_dhaka",
            selectedUpazilaId = prefs[Keys.SELECTED_UPAZILA_ID] ?: "",
            timezoneMode = TimezoneMode.fromKey(prefs[Keys.TIMEZONE_MODE]),
            selectedTimezoneId = prefs[Keys.SELECTED_TIMEZONE_ID] ?: city.timeZoneId.ifBlank { "Asia/Dhaka" },
            calculationMethod = CalculationMethod.fromKey(prefs[Keys.CALC_METHOD]),
            madhab = Madhab.fromKey(prefs[Keys.MADHAB]),
            highLatitudeRule = HighLatitudeRule.fromKey(prefs[Keys.HIGH_LAT]),
            hijriOffsetDays = (prefs[Keys.HIJRI_OFFSET] ?: 0).coerceIn(-2, 2),
            notificationPrefs = PrayerNotificationPrefs(
                fajrEnabled = prefs[Keys.NOTIF_FAJR] ?: true,
                dhuhrEnabled = prefs[Keys.NOTIF_DHUHR] ?: true,
                asrEnabled = prefs[Keys.NOTIF_ASR] ?: true,
                maghribEnabled = prefs[Keys.NOTIF_MAGHRIB] ?: true,
                ishaEnabled = prefs[Keys.NOTIF_ISHA] ?: true,
                soundOption = AzanSoundOption.fromKey(prefs[Keys.AZAN_SOUND])
            ),
            tasbihPresetId = prefs[Keys.TASBIH_PRESET_ID] ?: "preset_subhanallah",
            tasbihTarget = (prefs[Keys.TASBIH_TARGET] ?: 33).coerceAtLeast(1),
            tasbihCurrentCount = (prefs[Keys.TASBIH_COUNT] ?: 0).coerceAtLeast(0),
            tasbihHapticEnabled = prefs[Keys.TASBIH_HAPTIC] ?: true,
            fontConfig = DeenFontConfig(
                appScalePreset = FontScalePreset.fromKey(prefs[Keys.FONT_SCALE_PRESET]),
                arabicFontScale = (prefs[Keys.ARABIC_FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.50f),
                bengaliFontScale = (prefs[Keys.BENGALI_FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.45f),
                englishFontScale = (prefs[Keys.ENGLISH_FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.45f),
                latinFontChoice = LatinFontChoice.fromKey(prefs[Keys.LATIN_FONT_CHOICE])
            ),
            notebookDefaultCategoryId = prefs[Keys.NOTEBOOK_DEFAULT_CATEGORY_ID] ?: "cat_general",
            notebookAutosaveEnabled = prefs[Keys.NOTEBOOK_AUTOSAVE_ENABLED] ?: true,
            notebookPinHash = prefs[Keys.NOTEBOOK_PIN_HASH] ?: "",
            unsavedNoteDraftJson = prefs[Keys.UNSAVED_NOTE_DRAFT] ?: ""
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.deenDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.deenDataStore.edit { it[Keys.THEME_MODE] = mode.key }
    }

    suspend fun setLocation(
        city: CityLocation,
        mode: LocationMode = if (city.isGpsDetected) LocationMode.AUTOMATIC else LocationMode.MANUAL,
        countryCode: String? = null,
        divisionId: String? = null,
        districtId: String? = null,
        upazilaId: String? = null,
        syncTimezoneIfAutomaticOrLocationDefault: Boolean = true
    ) {
        context.deenDataStore.edit {
            it[Keys.CITY_ID] = city.id
            it[Keys.CITY_NAME_EN] = city.nameEn
            it[Keys.CITY_NAME_BN] = city.nameBn
            it[Keys.COUNTRY_EN] = city.countryEn
            it[Keys.COUNTRY_BN] = city.countryBn
            it[Keys.DIVISION_EN] = city.divisionNameEn
            it[Keys.DIVISION_BN] = city.divisionNameBn
            it[Keys.UPAZILA_EN] = city.upazilaNameEn
            it[Keys.UPAZILA_BN] = city.upazilaNameBn
            it[Keys.IS_REFERENCE_COORD] = city.isReferenceCoordinate
            it[Keys.LATITUDE] = city.latitude
            it[Keys.LONGITUDE] = city.longitude
            it[Keys.TIMEZONE_ID] = city.timeZoneId
            it[Keys.IS_GPS] = city.isGpsDetected
            it[Keys.LOCATION_MODE] = mode.key
            if (countryCode != null) it[Keys.SELECTED_COUNTRY_CODE] = countryCode
            if (divisionId != null) it[Keys.SELECTED_DIVISION_ID] = divisionId
            if (districtId != null) it[Keys.SELECTED_DISTRICT_ID] = districtId
            if (upazilaId != null) it[Keys.SELECTED_UPAZILA_ID] = upazilaId
            if (syncTimezoneIfAutomaticOrLocationDefault && city.timeZoneId.isNotBlank()) {
                it[Keys.SELECTED_TIMEZONE_ID] = city.timeZoneId
                it[Keys.TIMEZONE_MODE] = TimezoneMode.MANUAL.key
            }
        }
    }

    suspend fun setLocationMode(mode: LocationMode) {
        context.deenDataStore.edit { it[Keys.LOCATION_MODE] = mode.key }
    }

    suspend fun setTimezoneSettings(mode: TimezoneMode, timezoneId: String) {
        context.deenDataStore.edit {
            it[Keys.TIMEZONE_MODE] = mode.key
            if (timezoneId.isNotBlank()) {
                it[Keys.SELECTED_TIMEZONE_ID] = timezoneId
            }
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.deenDataStore.edit { it[Keys.CALC_METHOD] = method.key }
    }

    suspend fun setMadhab(madhab: Madhab) {
        context.deenDataStore.edit { it[Keys.MADHAB] = madhab.key }
    }

    suspend fun setHighLatitudeRule(rule: HighLatitudeRule) {
        context.deenDataStore.edit { it[Keys.HIGH_LAT] = rule.key }
    }

    suspend fun setHijriOffsetDays(offset: Int) {
        context.deenDataStore.edit { it[Keys.HIJRI_OFFSET] = offset.coerceIn(-2, 2) }
    }

    suspend fun setPrayerNotificationEnabled(prayer: PrayerName, enabled: Boolean) {
        context.deenDataStore.edit {
            when (prayer) {
                PrayerName.FAJR -> it[Keys.NOTIF_FAJR] = enabled
                PrayerName.DHUHR -> it[Keys.NOTIF_DHUHR] = enabled
                PrayerName.ASR -> it[Keys.NOTIF_ASR] = enabled
                PrayerName.MAGHRIB -> it[Keys.NOTIF_MAGHRIB] = enabled
                PrayerName.ISHA -> it[Keys.NOTIF_ISHA] = enabled
                PrayerName.SUNRISE -> Unit
            }
        }
    }

    suspend fun setAzanSoundOption(option: AzanSoundOption) {
        context.deenDataStore.edit { it[Keys.AZAN_SOUND] = option.key }
    }

    suspend fun setTasbihState(presetId: String, target: Int, count: Int) {
        context.deenDataStore.edit {
            it[Keys.TASBIH_PRESET_ID] = presetId
            it[Keys.TASBIH_TARGET] = target.coerceAtLeast(1)
            it[Keys.TASBIH_COUNT] = count.coerceAtLeast(0)
        }
    }

    suspend fun setTasbihHapticEnabled(enabled: Boolean) {
        context.deenDataStore.edit { it[Keys.TASBIH_HAPTIC] = enabled }
    }

    suspend fun setFontConfig(config: DeenFontConfig) {
        context.deenDataStore.edit {
            it[Keys.FONT_SCALE_PRESET] = config.appScalePreset.key
            it[Keys.ARABIC_FONT_SCALE] = config.arabicFontScale.coerceIn(0.85f, 1.50f)
            it[Keys.BENGALI_FONT_SCALE] = config.bengaliFontScale.coerceIn(0.85f, 1.45f)
            it[Keys.ENGLISH_FONT_SCALE] = config.englishFontScale.coerceIn(0.85f, 1.45f)
            it[Keys.LATIN_FONT_CHOICE] = config.latinFontChoice.key
        }
    }

    suspend fun resetFontConfig() {
        setFontConfig(DeenFontConfig())
    }

    suspend fun setNotebookDefaultCategory(categoryId: String) {
        context.deenDataStore.edit { it[Keys.NOTEBOOK_DEFAULT_CATEGORY_ID] = categoryId }
    }

    suspend fun setNotebookAutosaveEnabled(enabled: Boolean) {
        context.deenDataStore.edit { it[Keys.NOTEBOOK_AUTOSAVE_ENABLED] = enabled }
    }

    suspend fun setNotebookPinHash(hash: String) {
        context.deenDataStore.edit { it[Keys.NOTEBOOK_PIN_HASH] = hash }
    }

    suspend fun saveUnsavedNoteDraft(draftJson: String) {
        context.deenDataStore.edit { it[Keys.UNSAVED_NOTE_DRAFT] = draftJson }
    }
}
