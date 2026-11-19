package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DeenApplication
import com.example.core.i18n.AppLanguage
import com.example.data.repository.CityCatalog
import com.example.data.repository.LocationRepository
import com.example.data.repository.NotebookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.UserPreferences
import com.example.domain.calculator.HijriCalendarCalculator
import com.example.domain.calculator.PrayerInformationCalculator
import com.example.domain.calculator.PrayerInformationState
import com.example.domain.calculator.PrayerTimesCalculator
import com.example.domain.calculator.QiblaCalculator
import com.example.domain.model.AzanSoundOption
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.DailyPrayerSchedule
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.HijriDate
import com.example.domain.model.IslamicEvent
import com.example.domain.model.Madhab
import com.example.domain.model.NextPrayerInfo
import com.example.domain.model.PrayerName
import com.example.domain.model.ThemeMode
import com.example.location.CompassReading
import com.example.location.CompassSensorManager
import com.example.location.LocationHelper
import com.example.notifications.PrayerAlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class MainUiState(
    val prefs: UserPreferences = UserPreferences(),
    val todaySchedule: DailyPrayerSchedule? = null,
    val nextPrayer: NextPrayerInfo? = null,
    val hijriToday: HijriDate = HijriDate(1448, 5, 1, isPredicted = true),
    val qiblaBearing: Double = 277.6,
    val distanceToKaabaKm: Double = 5175.0,
    val monthlySchedules: List<DailyPrayerSchedule> = emptyList(),
    val importantEvents: List<IslamicEvent> = emptyList(),
    val isDetectingLocation: Boolean = false,
    val locationError: String? = null,
    val canScheduleExactAlarms: Boolean = true,
    val prayerInfo: PrayerInformationState? = null
)

class MainViewModel(
    private val prefsRepo: PreferencesRepository,
    private val locationRepo: LocationRepository,
    private val alarmScheduler: PrayerAlarmScheduler,
    private val locationHelper: LocationHelper,
    private val compassSensorManager: CompassSensorManager
) : ViewModel() {

    private val isDetectingLocation = MutableStateFlow(false)
    private val locationError = MutableStateFlow<String?>(null)

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    }

    val uiState: StateFlow<MainUiState> = combine(
        prefsRepo.userPreferences,
        tickerFlow,
        isDetectingLocation,
        locationError
    ) { prefs, nowMillis, detecting, locErr ->
        val effectiveLocation = prefs.effectiveLocation
        val tz = TimeZone.getTimeZone(effectiveLocation.timeZoneId)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val todaySchedule = PrayerTimesCalculator.calculateDaySchedule(
            year, month, day,
            effectiveLocation, prefs.calculationMethod, prefs.madhab, prefs.highLatitudeRule
        )
        val yesterdayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
        val yesterdaySchedule = PrayerTimesCalculator.calculateDaySchedule(
            yesterdayCal.get(Calendar.YEAR),
            yesterdayCal.get(Calendar.MONTH) + 1,
            yesterdayCal.get(Calendar.DAY_OF_MONTH),
            effectiveLocation, prefs.calculationMethod, prefs.madhab, prefs.highLatitudeRule
        )
        val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrowSchedule = PrayerTimesCalculator.calculateDaySchedule(
            tomorrowCal.get(Calendar.YEAR),
            tomorrowCal.get(Calendar.MONTH) + 1,
            tomorrowCal.get(Calendar.DAY_OF_MONTH),
            effectiveLocation, prefs.calculationMethod, prefs.madhab, prefs.highLatitudeRule
        )
        val prayerInfo = PrayerInformationCalculator.evaluate(
            nowMillis = nowMillis,
            todaySchedule = todaySchedule,
            yesterdaySchedule = yesterdaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            timeZoneId = effectiveLocation.timeZoneId
        )
        val nextPrayer = PrayerTimesCalculator.findNextPrayer(
            nowMillis,
            effectiveLocation, prefs.calculationMethod, prefs.madhab, prefs.highLatitudeRule
        )
        val hijriToday = HijriCalendarCalculator.gregorianToHijri(
            year, month, day, prefs.hijriOffsetDays
        )
        val monthly = PrayerTimesCalculator.calculateMonthlySchedule(
            year, month,
            effectiveLocation, prefs.calculationMethod, prefs.madhab, prefs.highLatitudeRule
        )
        val events = HijriCalendarCalculator.importantEventsForHijriYear(
            hijriToday.year, prefs.hijriOffsetDays
        )
        val bearing = QiblaCalculator.calculateBearing(effectiveLocation.latitude, effectiveLocation.longitude)
        val dist = QiblaCalculator.calculateDistanceKm(effectiveLocation.latitude, effectiveLocation.longitude)

        MainUiState(
            prefs = prefs,
            todaySchedule = todaySchedule,
            nextPrayer = nextPrayer,
            hijriToday = hijriToday,
            qiblaBearing = bearing,
            distanceToKaabaKm = dist,
            monthlySchedules = monthly,
            importantEvents = events,
            isDetectingLocation = detecting,
            locationError = locErr,
            canScheduleExactAlarms = alarmScheduler.canScheduleExact(),
            prayerInfo = prayerInfo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    val compassReading: StateFlow<CompassReading?> = compassSensorManager.observeHeading()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun selectCity(city: CityLocation) {
        viewModelScope.launch {
            locationError.value = null
            prefsRepo.setLocation(city)
            rescheduleAlarms()
        }
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            isDetectingLocation.value = true
            locationError.value = null
            val result = locationHelper.detectCurrentCity()
            result.onSuccess { city ->
                locationRepo.applyDetectedGpsLocation(city)
                rescheduleAlarms()
            }.onFailure { err ->
                locationError.value = err.message
            }
            isDetectingLocation.value = false
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch {
            prefsRepo.setCalculationMethod(method)
            rescheduleAlarms()
        }
    }

    fun setMadhab(madhab: Madhab) {
        viewModelScope.launch {
            prefsRepo.setMadhab(madhab)
            rescheduleAlarms()
        }
    }

    fun setHighLatitudeRule(rule: HighLatitudeRule) {
        viewModelScope.launch {
            prefsRepo.setHighLatitudeRule(rule)
            rescheduleAlarms()
        }
    }

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch { prefsRepo.setHijriOffsetDays(offset) }
    }

    fun setPrayerNotification(prayer: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setPrayerNotificationEnabled(prayer, enabled)
            rescheduleAlarms()
        }
    }

    fun setAzanSoundOption(option: AzanSoundOption) {
        viewModelScope.launch {
            prefsRepo.setAzanSoundOption(option)
            rescheduleAlarms()
        }
    }

    fun setNotebookDefaultCategory(categoryId: String) {
        viewModelScope.launch { prefsRepo.setNotebookDefaultCategory(categoryId) }
    }

    fun setNotebookAutosaveEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setNotebookAutosaveEnabled(enabled) }
    }

    fun setNotebookPin(pin: String) {
        if (pin.length == 4 && pin.all { it.isDigit() }) {
            viewModelScope.launch {
                prefsRepo.setNotebookPinHash(NotebookRepository.hashPin(pin))
            }
        }
    }

    fun clearNotebookPin() {
        viewModelScope.launch {
            prefsRepo.setNotebookPinHash("")
        }
    }

    fun availableCities(): List<CityLocation> = CityCatalog.cities

    fun rescheduleAlarms() {
        viewModelScope.launch {
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
        }
    }
}

class DeenViewModelFactory(private val app: DeenApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(
                app.preferencesRepository,
                app.locationRepository,
                app.prayerAlarmScheduler,
                app.locationHelper,
                app.compassSensorManager
            ) as T
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> LibraryViewModel(
                app.contentRepository
            ) as T
            modelClass.isAssignableFrom(WorshipViewModel::class.java) -> WorshipViewModel(
                app.worshipRepository,
                app.preferencesRepository
            ) as T
            modelClass.isAssignableFrom(NotebookViewModel::class.java) -> NotebookViewModel(
                app.notebookRepository,
                app.preferencesRepository
            ) as T
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> LocationViewModel(
                app.locationRepository,
                app.preferencesRepository,
                app.locationHelper,
                app.prayerAlarmScheduler
            ) as T
            modelClass.isAssignableFrom(TimezoneViewModel::class.java) -> TimezoneViewModel(
                app.timezoneRepository,
                app.preferencesRepository,
                app.prayerAlarmScheduler
            ) as T
            modelClass.isAssignableFrom(FontSettingsViewModel::class.java) -> FontSettingsViewModel(
                app.preferencesRepository
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
