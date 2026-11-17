package com.example.domain.calculator

import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.DailyPrayerSchedule
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.Madhab
import com.example.domain.model.NextPrayerInfo
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerTimeEntry
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical solar prayer time calculator based on Jean Meeus / PrayTimes equations.
 * Uses local calendar date, exact timezone offset (DST-aware for the given date),
 * location coordinates, calculation method angles, Asr shadow factor (Madhab),
 * and high-latitude adjustment rules.
 */
object PrayerTimesCalculator {

    fun calculateDaySchedule(
        year: Int,
        month: Int, // 1..12
        day: Int,
        location: CityLocation,
        method: CalculationMethod,
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
        use24Hour: Boolean = false
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone(location.timeZoneId)
        val dayStartCal = Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tzOffsetHours = tz.getOffset(dayStartCal.timeInMillis) / 3_600_000.0
        val jd = julianDay(year, month, day) - location.longitude / (15.0 * 24.0)

        val (declinationDeg, eqtHours) = sunPosition(jd)
        val lat = location.latitude
        val lng = location.longitude

        val dhuhrHour = fixHour(12.0 + tzOffsetHours - lng / 15.0 - eqtHours)
        val sunriseAngle = 0.833 // standard atmospheric refraction + solar radius
        val sunriseHour = dhuhrHour - sunAngleTime(lat, declinationDeg, sunriseAngle)
        val sunsetHour = dhuhrHour + sunAngleTime(lat, declinationDeg, sunriseAngle)

        val fajrRaw = sunAngleTime(lat, declinationDeg, method.fajrAngle)
        var fajrHour = if (fajrRaw.isNaN()) Double.NaN else dhuhrHour - fajrRaw

        val ishaHourRaw = if (method.ishaMinutesAfterMaghrib != null) {
            sunsetHour + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            val t = sunAngleTime(lat, declinationDeg, method.ishaAngle)
            if (t.isNaN()) Double.NaN else dhuhrHour + t
        }
        var ishaHour = ishaHourRaw

        val asrHour = dhuhrHour + asrTimeOffset(lat, declinationDeg, madhab.shadowFactor)

        // High-latitude fallback / adjustment when twilight angle does not occur
        val nightLength = fixHour(sunriseHour + 24.0 - sunsetHour)
        if (highLatitudeRule != HighLatitudeRule.NONE) {
            val fajrDiff = if (fajrHour.isNaN()) Double.POSITIVE_INFINITY else fixHour(sunriseHour - fajrHour)
            val fajrPortion = highLatPortion(highLatitudeRule, nightLength, method.fajrAngle)
            if (fajrHour.isNaN() || fajrDiff > fajrPortion) {
                fajrHour = sunriseHour - fajrPortion
            }

            if (method.ishaMinutesAfterMaghrib == null) {
                val ishaDiff = if (ishaHour.isNaN()) Double.POSITIVE_INFINITY else fixHour(ishaHour - sunsetHour)
                val ishaPortion = highLatPortion(highLatitudeRule, nightLength, method.ishaAngle)
                if (ishaHour.isNaN() || ishaDiff > ishaPortion) {
                    ishaHour = sunsetHour + ishaPortion
                }
            }
        }

        val hoursMap = listOf(
            PrayerName.FAJR to fixHour(fajrHour),
            PrayerName.SUNRISE to fixHour(sunriseHour),
            PrayerName.DHUHR to fixHour(dhuhrHour),
            PrayerName.ASR to fixHour(asrHour),
            PrayerName.MAGHRIB to fixHour(sunsetHour),
            PrayerName.ISHA to fixHour(ishaHour)
        )

        val entries = hoursMap.map { (prayer, decimalHour) ->
            val millis = decimalHourToMillis(year, month, day, decimalHour, tz)
            PrayerTimeEntry(
                prayer = prayer,
                timeMillis = millis,
                formattedTime = formatHour(decimalHour, use24Hour)
            )
        }

        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        return DailyPrayerSchedule(
            dateKey = dateKey,
            year = year,
            month = month,
            day = day,
            entries = entries
        )
    }

    fun calculateMonthlySchedule(
        year: Int,
        month: Int,
        location: CityLocation,
        method: CalculationMethod,
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
        use24Hour: Boolean = false
    ): List<DailyPrayerSchedule> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(location.timeZoneId)).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            calculateDaySchedule(year, month, day, location, method, madhab, highLatitudeRule, use24Hour)
        }
    }

    fun findNextPrayer(
        nowMillis: Long,
        location: CityLocation,
        method: CalculationMethod,
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
        use24Hour: Boolean = false
    ): NextPrayerInfo {
        val tz = TimeZone.getTimeZone(location.timeZoneId)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val todaySchedule = calculateDaySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            location, method, madhab, highLatitudeRule, use24Hour
        )
        val nextToday = todaySchedule.entries.firstOrNull {
            it.prayer.isObligatory && it.timeMillis > nowMillis
        }
        if (nextToday != null) {
            return NextPrayerInfo(
                prayer = nextToday.prayer,
                timeMillis = nextToday.timeMillis,
                formattedTime = nextToday.formattedTime,
                remainingMillis = (nextToday.timeMillis - nowMillis).coerceAtLeast(0L),
                isTomorrow = false
            )
        }

        // Midnight / post-Isha edge case: roll over to tomorrow's Fajr
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrowSchedule = calculateDaySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            location, method, madhab, highLatitudeRule, use24Hour
        )
        val tomorrowFajr = tomorrowSchedule.entries.first { it.prayer == PrayerName.FAJR }
        return NextPrayerInfo(
            prayer = tomorrowFajr.prayer,
            timeMillis = tomorrowFajr.timeMillis,
            formattedTime = tomorrowFajr.formattedTime,
            remainingMillis = (tomorrowFajr.timeMillis - nowMillis).coerceAtLeast(0L),
            isTomorrow = true
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2.0 * g))
        val e = 23.439 - 0.00000036 * d

        val ra = radToDeg(atan2(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = radToDeg(asin(sinDeg(e) * sinDeg(l)))
        return decl to eqt
    }

    private fun sunAngleTime(lat: Double, decl: Double, angleDeg: Double): Double {
        val num = -sinDeg(angleDeg) - sinDeg(lat) * sinDeg(decl)
        val den = cosDeg(lat) * cosDeg(decl)
        val ratio = num / den
        if (ratio < -1.0 || ratio > 1.0) return Double.NaN
        return radToDeg(acos(ratio)) / 15.0
    }

    private fun asrTimeOffset(lat: Double, decl: Double, shadowFactor: Int): Double {
        val angle = -radToDeg(atan(1.0 / (shadowFactor + tanDeg(abs(lat - decl)))))
        return sunAngleTime(lat, decl, angle)
    }

    private fun highLatPortion(rule: HighLatitudeRule, nightLength: Double, angle: Double): Double =
        when (rule) {
            HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> nightLength / 2.0
            HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> nightLength / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> (angle / 60.0) * nightLength
            HighLatitudeRule.NONE -> nightLength
        }

    private fun decimalHourToMillis(
        year: Int,
        month: Int,
        day: Int,
        decimalHour: Double,
        tz: TimeZone
    ): Long {
        val totalMinutes = (decimalHour * 60.0).roundToInt()
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60
        return Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatHour(decimalHour: Double, use24Hour: Boolean = false): String {
        if (decimalHour.isNaN()) return "--:--"
        val fixed = fixHour(decimalHour)
        val totalMinutes = (fixed * 60.0).roundToInt() % (24 * 60)
        val h24 = totalMinutes / 60
        val min = totalMinutes % 60
        return if (use24Hour) {
            String.format(Locale.US, "%02d:%02d", h24, min)
        } else {
            val suffix = if (h24 >= 12) "PM" else "AM"
            val h12 = when (val mod = h24 % 12) {
                0 -> 12
                else -> mod
            }
            String.format(Locale.US, "%d:%02d %s", h12, min, suffix)
        }
    }

    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double): Double = a - 24.0 * floor(a / 24.0)
    private fun degToRad(d: Double): Double = d * Math.PI / 180.0
    private fun radToDeg(r: Double): Double = r * 180.0 / Math.PI
    private fun sinDeg(d: Double): Double = sin(degToRad(d))
    private fun cosDeg(d: Double): Double = cos(degToRad(d))
    private fun tanDeg(d: Double): Double = tan(degToRad(d))
}
