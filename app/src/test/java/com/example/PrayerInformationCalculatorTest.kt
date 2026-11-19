package com.example

import com.example.data.repository.CityCatalog
import com.example.domain.calculator.ForbiddenPeriodType
import com.example.domain.calculator.ForbiddenTimesConfig
import com.example.domain.calculator.PrayerInformationCalculator
import com.example.domain.calculator.PrayerTimesCalculator
import com.example.domain.model.CalculationMethod
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerInformationCalculatorTest {

    private val dhaka = CityCatalog.defaultCity

    @Test
    fun `forbidden times compute 15m sunrise 10m zenith and 15m sunset accurately`() {
        val yesterday = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 18, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)
        val today = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 19, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)
        val tomorrow = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 20, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)

        val todayMap = today.entries.associateBy { it.prayer }
        val sunrise = todayMap[PrayerName.SUNRISE]!!.timeMillis
        val dhuhr = todayMap[PrayerName.DHUHR]!!.timeMillis
        val maghrib = todayMap[PrayerName.MAGHRIB]!!.timeMillis

        // 5 minutes after sunrise -> Sunrise forbidden period must be active
        val duringSunrise = PrayerInformationCalculator.evaluate(
            nowMillis = sunrise + 5 * 60_000L,
            todaySchedule = today,
            yesterdaySchedule = yesterday,
            tomorrowSchedule = tomorrow,
            timeZoneId = dhaka.timeZoneId,
            config = ForbiddenTimesConfig(15, 10, 15)
        )!!

        assertEquals(3, duringSunrise.forbiddenPeriods.size)
        assertEquals(ForbiddenPeriodType.SUNRISE, duringSunrise.activeForbiddenPeriod?.type)
        assertNull(duringSunrise.nextForbiddenPeriod)
        assertEquals(10 * 60_000L, duringSunrise.forbiddenCountdownMillis)

        // Verify exact configured boundaries
        val sunrisePeriod = duringSunrise.forbiddenPeriods.first { it.type == ForbiddenPeriodType.SUNRISE }
        val zenithPeriod = duringSunrise.forbiddenPeriods.first { it.type == ForbiddenPeriodType.ZENITH }
        val sunsetPeriod = duringSunrise.forbiddenPeriods.first { it.type == ForbiddenPeriodType.SUNSET }
        assertEquals(15 * 60_000L, sunrisePeriod.endMillis - sunrisePeriod.startMillis)
        assertEquals(10 * 60_000L, zenithPeriod.endMillis - zenithPeriod.startMillis)
        assertEquals(dhuhr, zenithPeriod.endMillis)
        assertEquals(15 * 60_000L, sunsetPeriod.endMillis - sunsetPeriod.startMillis)
        assertEquals(maghrib, sunsetPeriod.endMillis)
    }

    @Test
    fun `prayer duration tracks current prayer progress and handles post-midnight isha wraparound`() {
        val yesterday = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 18, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)
        val today = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 19, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)
        val tomorrow = PrayerTimesCalculator.calculateDaySchedule(2026, 11, 20, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI)

        val todayMap = today.entries.associateBy { it.prayer }
        val fajr = todayMap[PrayerName.FAJR]!!.timeMillis
        val dhuhr = todayMap[PrayerName.DHUHR]!!.timeMillis
        val asr = todayMap[PrayerName.ASR]!!.timeMillis

        // Midpoint of Dhuhr -> Asr
        val midDhuhr = dhuhr + (asr - dhuhr) / 2L
        val midState = PrayerInformationCalculator.evaluate(
            nowMillis = midDhuhr,
            todaySchedule = today,
            yesterdaySchedule = yesterday,
            tomorrowSchedule = tomorrow,
            timeZoneId = dhaka.timeZoneId
        )!!

        assertEquals(5, midState.allPrayerDurations.size)
        assertEquals(PrayerName.DHUHR, midState.currentPrayerDuration?.prayer)
        assertTrue(midState.currentPrayerDuration!!.progress in 0.48f..0.52f)
        assertEquals(ForbiddenPeriodType.SUNSET, midState.nextForbiddenPeriod?.type)

        // Post-midnight / pre-Fajr (30 minutes before today's Fajr) -> Isha active using yesterday's Isha -> today's Fajr
        val preFajrState = PrayerInformationCalculator.evaluate(
            nowMillis = fajr - 30 * 60_000L,
            todaySchedule = today,
            yesterdaySchedule = yesterday,
            tomorrowSchedule = tomorrow,
            timeZoneId = dhaka.timeZoneId
        )!!

        assertNotNull(preFajrState.currentPrayerDuration)
        assertEquals(PrayerName.ISHA, preFajrState.currentPrayerDuration?.prayer)
        assertEquals(30 * 60_000L, preFajrState.currentPrayerDuration?.remainingMillis)
        assertEquals(ForbiddenPeriodType.SUNRISE, preFajrState.nextForbiddenPeriod?.type)
    }
}
