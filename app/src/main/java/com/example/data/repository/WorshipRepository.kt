package com.example.data.repository

import com.example.data.local.database.PrayerTrackerDao
import com.example.data.local.database.PrayerTrackerEntity
import com.example.data.local.database.TasbihDao
import com.example.data.local.database.TasbihPresetEntity
import com.example.data.local.database.TasbihSessionEntity
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WorshipRepository(
    private val tasbihDao: TasbihDao,
    private val prayerTrackerDao: PrayerTrackerDao
) {
    val presets: Flow<List<TasbihPresetEntity>> = tasbihDao.observePresets()
    val recentSessions: Flow<List<TasbihSessionEntity>> = tasbihDao.observeRecentSessions()

    fun observeDailyTasbihTotal(dateKey: String): Flow<Int> =
        tasbihDao.observeDailyTotal(dateKey)

    suspend fun addCustomPreset(
        titleEn: String,
        titleBn: String,
        arabicText: String,
        defaultTarget: Int
    ): TasbihPresetEntity {
        val preset = TasbihPresetEntity(
            id = "custom_${UUID.randomUUID()}",
            titleEn = titleEn.trim(),
            titleBn = titleBn.trim().ifEmpty { titleEn.trim() },
            arabicText = arabicText.trim(),
            defaultTarget = defaultTarget.coerceAtLeast(1),
            isCustom = true
        )
        tasbihDao.insertPreset(preset)
        return preset
    }

    suspend fun deleteCustomPreset(id: String) {
        tasbihDao.deleteCustomPreset(id)
    }

    suspend fun recordTasbihSession(
        preset: TasbihPresetEntity,
        count: Int,
        target: Int,
        dateKey: String
    ) {
        if (count <= 0) return
        tasbihDao.insertSession(
            TasbihSessionEntity(
                presetId = preset.id,
                dhikrTitleEn = preset.titleEn,
                dhikrTitleBn = preset.titleBn,
                dhikrArabic = preset.arabicText,
                count = count,
                target = target,
                dateKey = dateKey,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    fun observePrayerLogsForDate(dateKey: String): Flow<Map<PrayerName, PrayerStatus>> =
        prayerTrackerDao.observeByDate(dateKey).map { rows ->
            val map = mutableMapOf<PrayerName, PrayerStatus>()
            rows.forEach { row ->
                PrayerName.fromKey(row.prayerKey)?.let { prayer ->
                    map[prayer] = PrayerStatus.fromKey(row.statusKey)
                }
            }
            map
        }

    fun observePrayerLogsRange(
        startDateKey: String,
        endDateKey: String
    ): Flow<List<PrayerTrackerEntity>> =
        prayerTrackerDao.observeRange(startDateKey, endDateKey)

    /**
     * Manual prayer status recording. Prayers are never auto-marked as completed.
     */
    suspend fun setPrayerStatus(dateKey: String, prayer: PrayerName, status: PrayerStatus) {
        if (!prayer.isObligatory) return
        if (status == PrayerStatus.UNMARKED) {
            prayerTrackerDao.deleteLog(dateKey, prayer.key)
        } else {
            prayerTrackerDao.upsertLog(
                PrayerTrackerEntity(
                    dateKey = dateKey,
                    prayerKey = prayer.key,
                    statusKey = status.key,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
