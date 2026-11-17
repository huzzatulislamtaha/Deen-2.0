package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.TasbihPresetEntity
import com.example.data.local.database.TasbihSessionEntity
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.WorshipRepository
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class TrackerPeriodStats(
    val completedCount: Int = 0,
    val missedCount: Int = 0,
    val totalPossible: Int = 35
)

data class WorshipUiState(
    val presets: List<TasbihPresetEntity> = emptyList(),
    val activePreset: TasbihPresetEntity? = null,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val hapticEnabled: Boolean = true,
    val todayTasbihTotal: Int = 0,
    val recentSessions: List<TasbihSessionEntity> = emptyList(),
    val selectedDateKey: String = WorshipViewModel.todayDateKey("Asia/Dhaka"),
    val todayDateKey: String = WorshipViewModel.todayDateKey("Asia/Dhaka"),
    val effectiveTimeZoneId: String = "Asia/Dhaka",
    val dayPrayerLogs: Map<PrayerName, PrayerStatus> = emptyMap(),
    val weeklyStats: TrackerPeriodStats = TrackerPeriodStats(totalPossible = 35),
    val monthlyStats: TrackerPeriodStats = TrackerPeriodStats(totalPossible = 150),
    val todayCompletedCount: Int = 0,
    val selectedSection: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorshipViewModel(
    private val worshipRepo: WorshipRepository,
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    private val selectedDateOverride = MutableStateFlow<String?>(null)
    private val selectedSectionFlow = MutableStateFlow(0)

    private val effectiveTodayFlow = prefsRepo.userPreferences
        .map { it.effectiveTimeZoneId to todayDateKey(it.effectiveTimeZoneId) }

    private val activeDateKeyFlow = combine(
        effectiveTodayFlow,
        selectedDateOverride
    ) { (_, todayKey), overrideKey ->
        overrideKey ?: todayKey
    }

    private val dayLogsFlow = activeDateKeyFlow.flatMapLatest { key ->
        worshipRepo.observePrayerLogsForDate(key)
    }

    private val monthLogsFlow = effectiveTodayFlow.flatMapLatest { (_, todayKey) ->
        val monthStartKey = offsetDateKey(todayKey, -29)
        worshipRepo.observePrayerLogsRange(monthStartKey, todayKey)
    }

    private val dailyTasbihFlow = effectiveTodayFlow.flatMapLatest { (_, todayKey) ->
        worshipRepo.observeDailyTasbihTotal(todayKey)
    }

    private val tasbihPartFlow = combine(
        worshipRepo.presets,
        prefsRepo.userPreferences,
        dailyTasbihFlow,
        worshipRepo.recentSessions
    ) { presets, prefs, dailyTotal, sessions ->
        val active = presets.firstOrNull { it.id == prefs.tasbihPresetId } ?: presets.firstOrNull()
        TasbihSnapshot(
            presets = presets,
            activePreset = active,
            count = prefs.tasbihCurrentCount,
            target = prefs.tasbihTarget,
            hapticEnabled = prefs.tasbihHapticEnabled,
            dailyTotal = dailyTotal,
            sessions = sessions
        )
    }

    private val statsSectionFlow = combine(monthLogsFlow, selectedSectionFlow) { rows, sec ->
        rows to sec
    }

    val uiState: StateFlow<WorshipUiState> = combine(
        tasbihPartFlow,
        effectiveTodayFlow,
        activeDateKeyFlow,
        dayLogsFlow,
        statsSectionFlow
    ) { tasbih, (tzId, todayKey), selectedKey, dayLogs, (monthRows, sec) ->
        val weekStartKey = offsetDateKey(todayKey, -6)
        val weekRows = monthRows.filter { it.dateKey >= weekStartKey }
        val weeklyStats = TrackerPeriodStats(
            completedCount = weekRows.count { it.statusKey == PrayerStatus.COMPLETED.key },
            missedCount = weekRows.count { it.statusKey == PrayerStatus.MISSED.key },
            totalPossible = 35
        )
        val monthlyStats = TrackerPeriodStats(
            completedCount = monthRows.count { it.statusKey == PrayerStatus.COMPLETED.key },
            missedCount = monthRows.count { it.statusKey == PrayerStatus.MISSED.key },
            totalPossible = 150
        )

        val todayCompleted = monthRows.count {
            it.dateKey == todayKey && it.statusKey == PrayerStatus.COMPLETED.key
        }

        WorshipUiState(
            presets = tasbih.presets,
            activePreset = tasbih.activePreset,
            currentCount = tasbih.count,
            targetCount = tasbih.target,
            hapticEnabled = tasbih.hapticEnabled,
            todayTasbihTotal = tasbih.dailyTotal,
            recentSessions = tasbih.sessions,
            selectedDateKey = selectedKey,
            todayDateKey = todayKey,
            effectiveTimeZoneId = tzId,
            dayPrayerLogs = dayLogs,
            weeklyStats = weeklyStats,
            monthlyStats = monthlyStats,
            todayCompletedCount = todayCompleted,
            selectedSection = sec
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorshipUiState()
    )

    fun setSection(section: Int) {
        selectedSectionFlow.value = section.coerceIn(0, 1)
    }

    fun incrementTasbih() {
        val state = uiState.value
        val preset = state.activePreset ?: return
        val next = state.currentCount + 1
        viewModelScope.launch {
            val tzId = prefsRepo.userPreferences.first().effectiveTimeZoneId
            val dateKey = todayDateKey(tzId)
            if (next >= state.targetCount) {
                worshipRepo.recordTasbihSession(
                    preset = preset,
                    count = next,
                    target = state.targetCount,
                    dateKey = dateKey
                )
                prefsRepo.setTasbihState(preset.id, state.targetCount, 0)
            } else {
                prefsRepo.setTasbihState(preset.id, state.targetCount, next)
            }
        }
    }

    fun resetTasbih(saveProgressIfPositive: Boolean = true) {
        val state = uiState.value
        val preset = state.activePreset ?: return
        viewModelScope.launch {
            val tzId = prefsRepo.userPreferences.first().effectiveTimeZoneId
            val dateKey = todayDateKey(tzId)
            if (saveProgressIfPositive && state.currentCount > 0) {
                worshipRepo.recordTasbihSession(
                    preset = preset,
                    count = state.currentCount,
                    target = state.targetCount,
                    dateKey = dateKey
                )
            }
            prefsRepo.setTasbihState(preset.id, state.targetCount, 0)
        }
    }

    fun selectPreset(preset: TasbihPresetEntity) {
        viewModelScope.launch {
            prefsRepo.setTasbihState(preset.id, preset.defaultTarget, 0)
        }
    }

    fun setTarget(target: Int) {
        val state = uiState.value
        val presetId = state.activePreset?.id ?: "preset_subhanallah"
        viewModelScope.launch {
            prefsRepo.setTasbihState(presetId, target.coerceAtLeast(1), state.currentCount)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setTasbihHapticEnabled(enabled) }
    }

    fun addCustomPreset(titleEn: String, titleBn: String, arabicText: String, target: Int) {
        if (titleEn.isBlank() || arabicText.isBlank()) return
        viewModelScope.launch {
            val created = worshipRepo.addCustomPreset(titleEn, titleBn, arabicText, target)
            prefsRepo.setTasbihState(created.id, created.defaultTarget, 0)
        }
    }

    fun deleteCustomPreset(id: String) {
        viewModelScope.launch {
            worshipRepo.deleteCustomPreset(id)
            if (uiState.value.activePreset?.id == id) {
                prefsRepo.setTasbihState("preset_subhanallah", 33, 0)
            }
        }
    }

    fun shiftTrackerDate(deltaDays: Int) {
        val today = uiState.value.todayDateKey
        val current = uiState.value.selectedDateKey
        val next = offsetDateKey(current, deltaDays)
        if (next <= today) {
            selectedDateOverride.value = if (next == today) null else next
        }
    }

    fun setPrayerStatus(prayer: PrayerName, status: PrayerStatus) {
        val dateKey = uiState.value.selectedDateKey
        viewModelScope.launch {
            worshipRepo.setPrayerStatus(dateKey, prayer, status)
        }
    }

    private data class TasbihSnapshot(
        val presets: List<TasbihPresetEntity>,
        val activePreset: TasbihPresetEntity?,
        val count: Int,
        val target: Int,
        val hapticEnabled: Boolean,
        val dailyTotal: Int,
        val sessions: List<TasbihSessionEntity>
    )

    companion object {
        fun todayDateKey(timeZoneId: String = TimeZone.getDefault().id): String {
            val tz = TimeZone.getTimeZone(timeZoneId)
            val cal = Calendar.getInstance(tz)
            return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun offsetDateKey(baseDateKey: String, deltaDays: Int): String {
            val parts = baseDateKey.split("-")
            if (parts.size != 3) return baseDateKey
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2026)
                set(Calendar.MONTH, (parts[1].toIntOrNull() ?: 1) - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: 1)
                add(Calendar.DAY_OF_MONTH, deltaDays)
            }
            return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
