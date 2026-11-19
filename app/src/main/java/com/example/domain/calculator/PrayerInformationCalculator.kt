package com.example.domain.calculator

import com.example.domain.model.DailyPrayerSchedule
import com.example.domain.model.PrayerName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Configurable precautionary intervals (in minutes) for the three commonly recognized
 * prohibited times for voluntary (Nafl) prayers.
 * These are practical precautionary buffers rather than universally fixed Islamic rulings.
 */
data class ForbiddenTimesConfig(
    val sunrisePrecautionMinutes: Int = 15,
    val zenithPrecautionMinutes: Int = 10,
    val sunsetPrecautionMinutes: Int = 15
)

enum class ForbiddenPeriodType(val key: String) {
    SUNRISE("sunrise"),
    ZENITH("zenith"),
    SUNSET("sunset")
}

data class ForbiddenPeriodInfo(
    val type: ForbiddenPeriodType,
    val startMillis: Long,
    val endMillis: Long,
    val formattedStart: String,
    val formattedEnd: String,
    val isActive: Boolean
)

data class PrayerDurationInfo(
    val prayer: PrayerName,
    val startMillis: Long,
    val endMillis: Long,
    val formattedStart: String,
    val formattedEnd: String,
    val isCurrent: Boolean,
    val progress: Float,
    val remainingMillis: Long
)

data class PrayerInformationState(
    val forbiddenPeriods: List<ForbiddenPeriodInfo>,
    val activeForbiddenPeriod: ForbiddenPeriodInfo?,
    val nextForbiddenPeriod: ForbiddenPeriodInfo?,
    val forbiddenCountdownMillis: Long,
    val currentPrayerDuration: PrayerDurationInfo?,
    val allPrayerDurations: List<PrayerDurationInfo>,
    val config: ForbiddenTimesConfig = ForbiddenTimesConfig()
)

/**
 * Pure offline derivative evaluator that computes Forbidden Prayer Times and
 * Prayer Time Durations directly from existing [DailyPrayerSchedule] outputs
 * produced by [PrayerTimesCalculator] without modifying the underlying calculation engine.
 */
object PrayerInformationCalculator {

    private const val MINUTE_MILLIS = 60_000L

    fun evaluate(
        nowMillis: Long,
        todaySchedule: DailyPrayerSchedule,
        yesterdaySchedule: DailyPrayerSchedule,
        tomorrowSchedule: DailyPrayerSchedule,
        timeZoneId: String,
        config: ForbiddenTimesConfig = ForbiddenTimesConfig()
    ): PrayerInformationState? {
        val todayMap = todaySchedule.entries.associateBy { it.prayer }
        val yesterdayMap = yesterdaySchedule.entries.associateBy { it.prayer }
        val tomorrowMap = tomorrowSchedule.entries.associateBy { it.prayer }

        val fajr = todayMap[PrayerName.FAJR]?.timeMillis ?: return null
        val sunrise = todayMap[PrayerName.SUNRISE]?.timeMillis ?: return null
        val dhuhr = todayMap[PrayerName.DHUHR]?.timeMillis ?: return null
        val asr = todayMap[PrayerName.ASR]?.timeMillis ?: return null
        val maghrib = todayMap[PrayerName.MAGHRIB]?.timeMillis ?: return null
        val isha = todayMap[PrayerName.ISHA]?.timeMillis ?: return null

        val yesterdayIsha = yesterdayMap[PrayerName.ISHA]?.timeMillis ?: (isha - 24 * 3_600_000L)
        val tomorrowFajr = tomorrowMap[PrayerName.FAJR]?.timeMillis ?: (fajr + 24 * 3_600_000L)
        val tomorrowSunrise = tomorrowMap[PrayerName.SUNRISE]?.timeMillis ?: (sunrise + 24 * 3_600_000L)

        val tz = TimeZone.getTimeZone(timeZoneId)

        // 1. Forbidden Prayer Times (Voluntary / Nafl prayers)
        val sunriseStart = sunrise
        val sunriseEnd = sunrise + config.sunrisePrecautionMinutes.coerceAtLeast(1) * MINUTE_MILLIS

        val zenithEnd = dhuhr
        val zenithStart = dhuhr - config.zenithPrecautionMinutes.coerceAtLeast(1) * MINUTE_MILLIS

        val sunsetEnd = maghrib
        val sunsetStart = maghrib - config.sunsetPrecautionMinutes.coerceAtLeast(1) * MINUTE_MILLIS

        val forbiddenPeriods = listOf(
            buildForbiddenPeriod(ForbiddenPeriodType.SUNRISE, sunriseStart, sunriseEnd, nowMillis, tz),
            buildForbiddenPeriod(ForbiddenPeriodType.ZENITH, zenithStart, zenithEnd, nowMillis, tz),
            buildForbiddenPeriod(ForbiddenPeriodType.SUNSET, sunsetStart, sunsetEnd, nowMillis, tz)
        )

        val activeForbidden = forbiddenPeriods.firstOrNull { it.isActive }
        val nextForbidden: ForbiddenPeriodInfo?
        val forbiddenCountdown: Long

        if (activeForbidden != null) {
            nextForbidden = null
            forbiddenCountdown = (activeForbidden.endMillis - nowMillis).coerceAtLeast(0L)
        } else {
            val upcomingToday = forbiddenPeriods.firstOrNull { it.startMillis > nowMillis }
            if (upcomingToday != null) {
                nextForbidden = upcomingToday
                forbiddenCountdown = (upcomingToday.startMillis - nowMillis).coerceAtLeast(0L)
            } else {
                // After today's sunset completes, roll over cleanly to tomorrow's sunrise
                val nextSunriseEnd = tomorrowSunrise + config.sunrisePrecautionMinutes.coerceAtLeast(1) * MINUTE_MILLIS
                nextForbidden = buildForbiddenPeriod(
                    ForbiddenPeriodType.SUNRISE,
                    tomorrowSunrise,
                    nextSunriseEnd,
                    nowMillis,
                    tz
                )
                forbiddenCountdown = (tomorrowSunrise - nowMillis).coerceAtLeast(0L)
            }
        }

        // 2. Five Daily Prayer Durations (General Interval Boundaries)
        // Handle post-midnight / pre-Fajr continuity by showing yesterday's Isha -> today's Fajr as active
        val isPreFajrIshaWindow = nowMillis < fajr && nowMillis >= yesterdayIsha
        val ishaIntervalStart = if (isPreFajrIshaWindow) yesterdayIsha else isha
        val ishaIntervalEnd = if (isPreFajrIshaWindow) fajr else tomorrowFajr

        val allDurations = listOf(
            buildPrayerDuration(PrayerName.FAJR, fajr, sunrise, nowMillis, tz),
            buildPrayerDuration(PrayerName.DHUHR, dhuhr, asr, nowMillis, tz),
            buildPrayerDuration(PrayerName.ASR, asr, maghrib, nowMillis, tz),
            buildPrayerDuration(PrayerName.MAGHRIB, maghrib, isha, nowMillis, tz),
            buildPrayerDuration(PrayerName.ISHA, ishaIntervalStart, ishaIntervalEnd, nowMillis, tz)
        )

        val currentDuration = allDurations.firstOrNull { it.isCurrent }

        return PrayerInformationState(
            forbiddenPeriods = forbiddenPeriods,
            activeForbiddenPeriod = activeForbidden,
            nextForbiddenPeriod = nextForbidden,
            forbiddenCountdownMillis = forbiddenCountdown,
            currentPrayerDuration = currentDuration,
            allPrayerDurations = allDurations,
            config = config
        )
    }

    private fun buildForbiddenPeriod(
        type: ForbiddenPeriodType,
        startMillis: Long,
        endMillis: Long,
        nowMillis: Long,
        tz: TimeZone
    ): ForbiddenPeriodInfo {
        val active = nowMillis in startMillis until endMillis
        return ForbiddenPeriodInfo(
            type = type,
            startMillis = startMillis,
            endMillis = endMillis,
            formattedStart = formatMillis(startMillis, tz),
            formattedEnd = formatMillis(endMillis, tz),
            isActive = active
        )
    }

    private fun buildPrayerDuration(
        prayer: PrayerName,
        startMillis: Long,
        endMillis: Long,
        nowMillis: Long,
        tz: TimeZone
    ): PrayerDurationInfo {
        val isCurrent = nowMillis in startMillis until endMillis
        val total = (endMillis - startMillis).coerceAtLeast(1L)
        val elapsed = (nowMillis - startMillis).coerceIn(0L, total)
        val progress = if (isCurrent) (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        val remaining = if (isCurrent) (endMillis - nowMillis).coerceAtLeast(0L) else 0L
        return PrayerDurationInfo(
            prayer = prayer,
            startMillis = startMillis,
            endMillis = endMillis,
            formattedStart = formatMillis(startMillis, tz),
            formattedEnd = formatMillis(endMillis, tz),
            isCurrent = isCurrent,
            progress = progress,
            remainingMillis = remaining
        )
    }

    private fun formatMillis(epochMillis: Long, tz: TimeZone): String {
        val fmt = SimpleDateFormat("hh:mm a", Locale.US).apply {
            timeZone = tz
        }
        return fmt.format(Date(epochMillis))
    }
}
