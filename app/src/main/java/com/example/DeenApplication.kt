package com.example

import android.app.Application
import com.example.data.local.database.DeenDatabase
import com.example.data.repository.ContentRepository
import com.example.data.repository.LocationRepository
import com.example.data.repository.NotebookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.TimezoneRepository
import com.example.data.repository.WorshipRepository
import com.example.location.CompassSensorManager
import com.example.location.LocationHelper
import com.example.notifications.PrayerAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DeenApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: DeenDatabase by lazy { DeenDatabase.getInstance(this) }
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(this) }
    val contentRepository: ContentRepository by lazy {
        ContentRepository(
            database = database,
            hadithDao = database.hadithDao(),
            duaDao = database.duaDao(),
            bookmarkDao = database.bookmarkDao()
        )
    }
    val worshipRepository: WorshipRepository by lazy {
        WorshipRepository(
            tasbihDao = database.tasbihDao(),
            prayerTrackerDao = database.prayerTrackerDao()
        )
    }
    val notebookRepository: NotebookRepository by lazy {
        NotebookRepository(
            database = database,
            noteDao = database.noteDao(),
            categoryDao = database.noteCategoryDao(),
            prefsRepo = preferencesRepository
        )
    }
    val locationRepository: LocationRepository by lazy {
        LocationRepository(preferencesRepository)
    }
    val timezoneRepository: TimezoneRepository by lazy {
        TimezoneRepository(preferencesRepository)
    }
    val prayerAlarmScheduler: PrayerAlarmScheduler by lazy { PrayerAlarmScheduler(this) }
    val locationHelper: LocationHelper by lazy { LocationHelper(this) }
    val compassSensorManager: CompassSensorManager by lazy { CompassSensorManager(this) }

    override fun onCreate() {
        super.onCreate()
        prayerAlarmScheduler.createNotificationChannels()
        appScope.launch {
            contentRepository.ensureSeeded()
            notebookRepository.ensureDefaultCategories()
            val prefs = preferencesRepository.userPreferences.first()
            prayerAlarmScheduler.rescheduleAll(prefs)
        }
    }
}
