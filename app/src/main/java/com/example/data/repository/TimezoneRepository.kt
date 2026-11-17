package com.example.data.repository

import com.example.domain.model.TimezoneEntry
import com.example.domain.model.TimezoneMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class TimezoneRepository(
    private val prefsRepo: PreferencesRepository
) {
    private val curatedIds: List<String> = listOf(
        "Asia/Dhaka",
        "Asia/Riyadh",
        "Asia/Dubai",
        "Asia/Kolkata",
        "Asia/Karachi",
        "Asia/Kuala_Lumpur",
        "Asia/Jakarta",
        "Europe/London",
        "Europe/Istanbul",
        "America/New_York",
        "America/Los_Angeles",
        "America/Toronto",
        "Africa/Cairo",
        "UTC"
    )

    private val specialDisplayNames: Map<String, Pair<String, String>> = mapOf(
        "Asia/Dhaka" to ("Bangladesh Standard Time" to "Bangladesh (All 64 Districts)"),
        "Asia/Riyadh" to ("Arabian Standard Time (Makkah / Madinah)" to "Saudi Arabia"),
        "Asia/Kolkata" to ("India Standard Time" to "India"),
        "Asia/Karachi" to ("Pakistan Standard Time" to "Pakistan"),
        "Asia/Dubai" to ("Gulf Standard Time" to "UAE"),
        "Asia/Kuala_Lumpur" to ("Malaysia Time" to "Malaysia"),
        "Asia/Jakarta" to ("Western Indonesia Time" to "Indonesia"),
        "Europe/London" to ("Greenwich / British Time" to "United Kingdom"),
        "Europe/Istanbul" to ("Turkey Time" to "Turkey"),
        "America/New_York" to ("Eastern Time" to "United States"),
        "America/Los_Angeles" to ("Pacific Time" to "United States"),
        "America/Toronto" to ("Eastern Time" to "Canada"),
        "Africa/Cairo" to ("Eastern European Time" to "Egypt"),
        "UTC" to ("Coordinated Universal Time" to "Universal")
    )

    fun buildTimezoneList(nowMillis: Long = System.currentTimeMillis()): List<TimezoneEntry> {
        val available = TimeZone.getAvailableIDs().toSet()
        val orderedIds = buildList {
            curatedIds.forEach { if (it in available) add(it) }
            available.sorted().forEach { id ->
                if (id !in curatedIds && !id.startsWith("Etc/") && id.contains("/")) {
                    add(id)
                }
            }
        }
        return orderedIds.map { id -> entryFor(id, nowMillis) }
    }

    fun entryFor(timezoneId: String, nowMillis: Long = System.currentTimeMillis()): TimezoneEntry {
        val tz = TimeZone.getTimeZone(timezoneId)
        val offsetMs = tz.getOffset(nowMillis)
        val offsetLabel = formatUtcOffset(offsetMs)

        val fmt = SimpleDateFormat("hh:mm a • dd MMM", Locale.US).apply { timeZone = tz }
        val formatted = fmt.format(Date(nowMillis))

        val special = specialDisplayNames[timezoneId]
        val displayName = special?.first ?: tz.getDisplayName(tz.inDaylightTime(Date(nowMillis)), TimeZone.LONG, Locale.ENGLISH)
        val regionTag = special?.second ?: ""

        return TimezoneEntry(
            id = tz.id,
            displayName = displayName,
            offsetLabel = offsetLabel,
            offsetMillis = offsetMs,
            currentTimeFormatted = formatted,
            usesDst = tz.useDaylightTime(),
            regionTag = regionTag
        )
    }

    fun searchTimezones(
        query: String,
        allEntries: List<TimezoneEntry>
    ): List<TimezoneEntry> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return allEntries
        val qLower = trimmed.lowercase(Locale.US)
        val aliasMatchesMakkah = qLower.contains("makkah") || qLower.contains("mecca") || qLower.contains("madinah")
        return allEntries.filter { entry ->
            entry.id.lowercase(Locale.US).contains(qLower) ||
                entry.displayName.lowercase(Locale.US).contains(qLower) ||
                entry.offsetLabel.lowercase(Locale.US).contains(qLower) ||
                entry.regionTag.lowercase(Locale.US).contains(qLower) ||
                (aliasMatchesMakkah && entry.id == "Asia/Riyadh")
        }
    }

    suspend fun selectManualTimezone(timezoneId: String) {
        val valid = TimeZone.getTimeZone(timezoneId).id
        prefsRepo.setTimezoneSettings(TimezoneMode.MANUAL, valid)
    }

    suspend fun setAutomaticDeviceTimezone() {
        prefsRepo.setTimezoneSettings(TimezoneMode.AUTOMATIC, TimeZone.getDefault().id)
    }

    suspend fun resetToBangladeshDefault() {
        prefsRepo.setTimezoneSettings(TimezoneMode.MANUAL, BangladeshLocationCatalog.BANGLADESH_TIMEZONE_ID)
    }

    companion object {
        fun formatUtcOffset(offsetMs: Int): String {
            val totalMinutes = offsetMs / 60_000
            val sign = if (totalMinutes >= 0) "+" else "-"
            val absMinutes = abs(totalMinutes)
            val hours = absMinutes / 60
            val mins = absMinutes % 60
            return String.format(Locale.US, "UTC%s%02d:%02d", sign, hours, mins)
        }
    }
}
