package com.example.domain.calculator

import com.example.domain.model.HijriDate
import com.example.domain.model.IslamicEvent
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

/**
 * Kuwaiti/Tabular arithmetic Hijri calendar conversion with local moon-sighting offset (-2..+2 days).
 * Clearly marks all calculated dates as astronomically predicted rather than locally confirmed.
 */
object HijriCalendarCalculator {

    fun gregorianToHijri(
        year: Int,
        month: Int, // 1..12
        day: Int,
        offsetDays: Int = 0
    ): HijriDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (offsetDays != 0) add(Calendar.DAY_OF_MONTH, offsetDays)
        }
        val adjYear = cal.get(Calendar.YEAR)
        val adjMonth = cal.get(Calendar.MONTH) + 1
        val adjDay = cal.get(Calendar.DAY_OF_MONTH)

        val jd = gregorianToJulianDay(adjYear, adjMonth, adjDay)
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631)
        val l1 = l - 10631 * n + 354
        val j = ((10985 - l1) / 5316) * ((50 * l1) / 17719) + (l1 / 5670) * ((43 * l1) / 15238)
        val l2 = l1 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hijriMonth = (24 * l2) / 709
        val hijriDay = l2 - (709 * hijriMonth) / 24
        val hijriYear = 30 * n + j - 30

        return HijriDate(
            year = hijriYear,
            month = hijriMonth.coerceIn(1, 12),
            day = hijriDay.coerceIn(1, 30),
            isPredicted = true
        )
    }

    fun hijriToGregorian(
        hijriYear: Int,
        hijriMonth: Int,
        hijriDay: Int,
        offsetDays: Int = 0
    ): Triple<Int, Int, Int> {
        val jd = ((11 * hijriYear + 3) / 30) + 354 * hijriYear + 30 * hijriMonth -
            ((hijriMonth - 1) / 2) + hijriDay + 1948440 - 385
        val (gy, gm, gd) = julianDayToGregorian(jd)
        if (offsetDays == 0) return Triple(gy, gm, gd)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            add(Calendar.DAY_OF_MONTH, -offsetDays)
        }
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun importantEventsForHijriYear(
        hijriYear: Int,
        offsetDays: Int = 0
    ): List<IslamicEvent> {
        val specs = listOf(
            Triple("new_year", 1, 1),
            Triple("ashura", 1, 10),
            Triple("ramadan_start", 9, 1),
            Triple("laylatul_qadr", 9, 27),
            Triple("eid_fitr", 10, 1),
            Triple("arafah", 12, 9),
            Triple("eid_adha", 12, 10)
        )
        return specs.map { (id, hMonth, hDay) ->
            val (gy, gm, gd) = hijriToGregorian(hijriYear, hMonth, hDay, offsetDays)
            IslamicEvent(
                id = id,
                hijriMonth = hMonth,
                hijriDay = hDay,
                gregorianDateKey = String.format(Locale.US, "%04d-%02d-%02d", gy, gm, gd),
                gregorianLabel = String.format(Locale.US, "%02d/%02d/%04d", gd, gm, gy)
            )
        }
    }

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun julianDayToGregorian(jd: Int): Triple<Int, Int, Int> {
        val a = jd + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * b + d - 4800 + (m / 10)
        return Triple(year, month, day)
    }
}
