package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.TimezoneRepository
import com.example.domain.model.TimezoneEntry
import com.example.domain.model.TimezoneMode
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

data class TimezoneUiState(
    val timezoneMode: TimezoneMode = TimezoneMode.MANUAL,
    val selectedTimezoneId: String = "Asia/Dhaka",
    val effectiveEntry: TimezoneEntry? = null,
    val searchQuery: String = "",
    val filteredTimezones: List<TimezoneEntry> = emptyList(),
    val deviceTimezoneId: String = java.util.TimeZone.getDefault().id
)

class TimezoneViewModel(
    private val timezoneRepo: TimezoneRepository,
    private val prefsRepo: PreferencesRepository,
    private val alarmScheduler: PrayerAlarmScheduler
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val minuteTicker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(15_000L)
        }
    }

    val uiState: StateFlow<TimezoneUiState> = combine(
        prefsRepo.userPreferences,
        searchQuery,
        minuteTicker
    ) { prefs, query, nowMillis ->
        val allZones = timezoneRepo.buildTimezoneList(nowMillis)
        val filtered = timezoneRepo.searchTimezones(query, allZones)
        val effectiveId = prefs.effectiveTimeZoneId
        val effectiveEntry = timezoneRepo.entryFor(effectiveId, nowMillis)
        TimezoneUiState(
            timezoneMode = prefs.timezoneMode,
            selectedTimezoneId = effectiveId,
            effectiveEntry = effectiveEntry,
            searchQuery = query,
            filteredTimezones = filtered,
            deviceTimezoneId = java.util.TimeZone.getDefault().id
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimezoneUiState()
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectManualTimezone(timezoneId: String) {
        viewModelScope.launch {
            timezoneRepo.selectManualTimezone(timezoneId)
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
        }
    }

    fun setAutomaticTimezone() {
        viewModelScope.launch {
            timezoneRepo.setAutomaticDeviceTimezone()
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
        }
    }

    fun resetToBangladeshDefault() {
        viewModelScope.launch {
            timezoneRepo.resetToBangladeshDefault()
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
        }
    }
}
