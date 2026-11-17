package com.example

import com.example.data.repository.CityCatalog
import com.example.domain.calculator.HijriCalendarCalculator
import com.example.domain.calculator.PrayerTimesCalculator
import com.example.domain.calculator.QiblaCalculator
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DeenCalculatorsTest {

    @Test
    fun `dhaka daily prayer times preserve chronological order and hanafi asr is later`() {
        val dhaka = CityCatalog.defaultCity
        val standard = PrayerTimesCalculator.calculateDaySchedule(
            2026, 11, 17, dhaka, CalculationMethod.KARACHI, Madhab.STANDARD
        )
        val hanafi = PrayerTimesCalculator.calculateDaySchedule(
            2026, 11, 17, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI
        )

        val stdTimes = standard.entries.associateBy { it.prayer }
        val hanTimes = hanafi.entries.associateBy { it.prayer }

        assertTrue(stdTimes[PrayerName.FAJR]!!.timeMillis < stdTimes[PrayerName.SUNRISE]!!.timeMillis)
        assertTrue(stdTimes[PrayerName.SUNRISE]!!.timeMillis < stdTimes[PrayerName.DHUHR]!!.timeMillis)
        assertTrue(stdTimes[PrayerName.DHUHR]!!.timeMillis < stdTimes[PrayerName.ASR]!!.timeMillis)
        assertTrue(stdTimes[PrayerName.ASR]!!.timeMillis < hanTimes[PrayerName.ASR]!!.timeMillis)
        assertTrue(hanTimes[PrayerName.ASR]!!.timeMillis < hanTimes[PrayerName.MAGHRIB]!!.timeMillis)
        assertTrue(hanTimes[PrayerName.MAGHRIB]!!.timeMillis < hanTimes[PrayerName.ISHA]!!.timeMillis)
    }

    @Test
    fun `umm al qura isha is 90 minutes after maghrib`() {
        val makkah = CityCatalog.cities.first { it.id == "makkah" }
        val schedule = PrayerTimesCalculator.calculateDaySchedule(
            2026, 6, 15, makkah, CalculationMethod.UMM_AL_QURA, Madhab.STANDARD
        )
        val maghrib = schedule.entries.first { it.prayer == PrayerName.MAGHRIB }.timeMillis
        val isha = schedule.entries.first { it.prayer == PrayerName.ISHA }.timeMillis
        val diffMinutes = (isha - maghrib) / 60_000L
        assertEquals(90L, diffMinutes)
    }

    @Test
    fun `findNextPrayer wraps around after isha to tomorrow fajr`() {
        val dhaka = CityCatalog.defaultCity
        val tz = TimeZone.getTimeZone(dhaka.timeZoneId)
        val lateNightCal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 17, 23, 50, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val next = PrayerTimesCalculator.findNextPrayer(
            lateNightCal.timeInMillis, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI
        )
        assertEquals(PrayerName.FAJR, next.prayer)
        assertTrue(next.isTomorrow)
        assertTrue(next.remainingMillis > 0L)
    }

    @Test
    fun `monthly schedule generates exact number of days`() {
        val dhaka = CityCatalog.defaultCity
        val feb = PrayerTimesCalculator.calculateMonthlySchedule(
            2026, 2, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI
        )
        val nov = PrayerTimesCalculator.calculateMonthlySchedule(
            2026, 11, dhaka, CalculationMethod.KARACHI, Madhab.HANAFI
        )
        assertEquals(28, feb.size)
        assertEquals(30, nov.size)
    }

    @Test
    fun `dst changes dhuhr local hour in london between january and july`() {
        val london = CityCatalog.cities.first { it.id == "london" }
        val winter = PrayerTimesCalculator.calculateDaySchedule(
            2026, 1, 15, london, CalculationMethod.MWL, Madhab.STANDARD, use24Hour = true
        )
        val summer = PrayerTimesCalculator.calculateDaySchedule(
            2026, 7, 15, london, CalculationMethod.MWL, Madhab.STANDARD, use24Hour = true
        )
        val winterDhuhrHour = winter.entries.first { it.prayer == PrayerName.DHUHR }
            .formattedTime.substringBefore(":").toInt()
        val summerDhuhrHour = summer.entries.first { it.prayer == PrayerName.DHUHR }
            .formattedTime.substringBefore(":").toInt()
        assertEquals(12, winterDhuhrHour)
        assertEquals(13, summerDhuhrHour)
    }

    @Test
    fun `qibla bearing and distance for dhaka london and new york are accurate`() {
        val dhakaBearing = QiblaCalculator.calculateBearing(23.8103, 90.4125)
        val dhakaDist = QiblaCalculator.calculateDistanceKm(23.8103, 90.4125)
        assertEquals(277.6, dhakaBearing, 0.5)
        assertEquals(5175.0, dhakaDist, 30.0)

        val londonBearing = QiblaCalculator.calculateBearing(51.5072, -0.1276)
        assertEquals(119.0, londonBearing, 0.5)

        val nyBearing = QiblaCalculator.calculateBearing(40.7128, -74.0060)
        assertEquals(58.5, nyBearing, 0.5)
    }

    @Test
    fun `qibla shortest angular delta handles 360 degree wraparound`() {
        val delta = QiblaCalculator.shortestAngularDelta(350f, 10.0)
        assertEquals(20f, delta, 0.01f)
        val leftDelta = QiblaCalculator.shortestAngularDelta(10f, 350.0)
        assertEquals(-20f, leftDelta, 0.01f)
    }

    @Test
    fun `hijri calendar conversion and offset work accurately`() {
        val base = HijriCalendarCalculator.gregorianToHijri(2026, 11, 17, offsetDays = 0)
        val plusOne = HijriCalendarCalculator.gregorianToHijri(2026, 11, 17, offsetDays = 1)
        assertTrue(base.isPredicted)
        assertTrue(base.year in 1447..1449)
        assertTrue(base.month in 1..12)
        assertTrue(base.day in 1..30)
        assertFalse(base == plusOne)

        val events = HijriCalendarCalculator.importantEventsForHijriYear(base.year)
        assertEquals(7, events.size)
    }
}
