package com.example.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.stringsFor
import com.example.data.repository.PreferencesRepository
import com.example.domain.model.AzanSoundOption
import com.example.domain.model.PrayerName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerKey = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_KEY) ?: return
        val prayer = PrayerName.fromKey(prayerKey) ?: return
        val soundOption = AzanSoundOption.fromKey(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SOUND_KEY))
        val cityEn = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_EN) ?: "Your City"
        val cityBn = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_BN) ?: "আপনার শহর"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = PreferencesRepository(context.applicationContext)
                val prefs = prefsRepo.userPreferences.first()
                val strings = stringsFor(prefs.language)
                val prayerLabel = when (prayer) {
                    PrayerName.FAJR -> strings.fajr
                    PrayerName.SUNRISE -> strings.sunrise
                    PrayerName.DHUHR -> strings.dhuhr
                    PrayerName.ASR -> strings.asr
                    PrayerName.MAGHRIB -> strings.maghrib
                    PrayerName.ISHA -> strings.isha
                }
                val cityLabel = if (prefs.language == AppLanguage.BENGALI) cityBn else cityEn

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val contentPi = PendingIntent.getActivity(
                        context,
                        prayer.ordinal + 200,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val channelId = PrayerAlarmScheduler.channelFor(soundOption)
                    val notification = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("${strings.appTitle} • $prayerLabel")
                        .setContentText(String.format(strings.notificationBodyFormat, prayerLabel, cityLabel))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentPi)
                        .build()

                    NotificationManagerCompat.from(context)
                        .notify(4200 + prayer.ordinal, notification)
                }

                // Schedule next occurrence (e.g., tomorrow's same prayer)
                PrayerAlarmScheduler(context.applicationContext).rescheduleAll(prefs)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
