package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.repository.UserPreferences
import com.example.domain.calculator.PrayerTimesCalculator
import com.example.domain.model.AzanSoundOption
import com.example.domain.model.PrayerName
import java.util.Calendar
import java.util.TimeZone

class PrayerAlarmScheduler(private val context: Context) {

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: defaultSoundUri
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val defaultChannel = NotificationChannel(
            CHANNEL_DEFAULT,
            "Prayer Times (Standard)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for daily obligatory prayers"
            setSound(defaultSoundUri, audioAttrs)
            enableVibration(true)
        }

        val chimeChannel = NotificationChannel(
            CHANNEL_CHIME,
            "Prayer Times (Chime)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Chime alerts for daily obligatory prayers"
            setSound(alarmSoundUri, audioAttrs)
            enableVibration(true)
        }

        val silentChannel = NotificationChannel(
            CHANNEL_SILENT,
            "Prayer Times (Silent)",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Silent visual reminders for daily prayers"
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannels(listOf(defaultChannel, chimeChannel, silentChannel))
    }

    fun canScheduleExact(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun rescheduleAll(prefs: UserPreferences, nowMillis: Long = System.currentTimeMillis()) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val effectiveLocation = prefs.effectiveLocation
        val tz = TimeZone.getTimeZone(effectiveLocation.timeZoneId)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }

        val todaySchedule = PrayerTimesCalculator.calculateDaySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            effectiveLocation,
            prefs.calculationMethod,
            prefs.madhab,
            prefs.highLatitudeRule
        )

        cal.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrowSchedule = PrayerTimesCalculator.calculateDaySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            effectiveLocation,
            prefs.calculationMethod,
            prefs.madhab,
            prefs.highLatitudeRule
        )

        PrayerName.obligatoryPrayers.forEachIndexed { index, prayer ->
            val requestCode = BASE_REQUEST_CODE + index
            cancelAlarm(alarmManager, requestCode, prayer)

            if (!prefs.notificationPrefs.isEnabled(prayer)) return@forEachIndexed

            val todayEntry = todaySchedule.entries.firstOrNull { it.prayer == prayer }
            val tomorrowEntry = tomorrowSchedule.entries.firstOrNull { it.prayer == prayer }
            val triggerMillis = when {
                todayEntry != null && todayEntry.timeMillis > nowMillis + 5_000L -> todayEntry.timeMillis
                tomorrowEntry != null -> tomorrowEntry.timeMillis
                else -> return@forEachIndexed
            }

            val pendingIntent = buildPendingIntent(
                requestCode = requestCode,
                prayer = prayer,
                cityNameEn = effectiveLocation.nameEn,
                cityNameBn = effectiveLocation.nameBn,
                soundOption = prefs.notificationPrefs.soundOption
            )

            scheduleSafely(alarmManager, triggerMillis, pendingIntent)
        }
    }

    private fun scheduleSafely(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(alarmManager: AlarmManager, requestCode: Int, prayer: PrayerName) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.ACTION_PRAYER_ALARM_${prayer.key}"
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun buildPendingIntent(
        requestCode: Int,
        prayer: PrayerName,
        cityNameEn: String,
        cityNameBn: String,
        soundOption: AzanSoundOption
    ): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.ACTION_PRAYER_ALARM_${prayer.key}"
            putExtra(EXTRA_PRAYER_KEY, prayer.key)
            putExtra(EXTRA_CITY_EN, cityNameEn)
            putExtra(EXTRA_CITY_BN, cityNameBn)
            putExtra(EXTRA_SOUND_KEY, soundOption.key)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_DEFAULT = "deen_prayer_default"
        const val CHANNEL_CHIME = "deen_prayer_chime"
        const val CHANNEL_SILENT = "deen_prayer_silent"
        const val EXTRA_PRAYER_KEY = "extra_prayer_key"
        const val EXTRA_CITY_EN = "extra_city_en"
        const val EXTRA_CITY_BN = "extra_city_bn"
        const val EXTRA_SOUND_KEY = "extra_sound_key"
        private const val BASE_REQUEST_CODE = 7100

        fun channelFor(option: AzanSoundOption): String = when (option) {
            AzanSoundOption.DEFAULT_NOTIFICATION -> CHANNEL_DEFAULT
            AzanSoundOption.GENTLE_CHIME -> CHANNEL_CHIME
            AzanSoundOption.SILENT -> CHANNEL_SILENT
        }
    }
}
