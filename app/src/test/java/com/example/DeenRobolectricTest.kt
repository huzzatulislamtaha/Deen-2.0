package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.TasbihSessionEntity
import com.example.data.repository.ContentRepository
import com.example.data.repository.WorshipRepository
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeenRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var inMemoryDb: DeenDatabase

    @Before
    fun setUpDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, DeenDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDownDb() {
        inMemoryDb.close()
    }

    @Test
    fun `database seeding bookmarks tasbih and manual prayer tracker work end to end`() = runBlocking {
        DeenDatabase.seedInitialContent(inMemoryDb)

        val contentRepo = ContentRepository(
            inMemoryDb,
            inMemoryDb.hadithDao(),
            inMemoryDb.duaDao(),
            inMemoryDb.bookmarkDao()
        )
        val worshipRepo = WorshipRepository(
            inMemoryDb.tasbihDao(),
            inMemoryDb.prayerTrackerDao()
        )

        val hadiths = contentRepo.hadiths.first()
        val duas = contentRepo.duas.first()
        assertEquals(10, hadiths.size)
        assertEquals(16, duas.size)

        // Bookmarks toggle
        val firstHadith = hadiths.first()
        contentRepo.toggleHadithBookmark(firstHadith.id, currentlyBookmarked = false)
        assertTrue(contentRepo.hadiths.first().first { it.id == firstHadith.id }.isBookmarked)

        // Tasbih session & daily total
        val preset = worshipRepo.presets.first().first()
        worshipRepo.recordTasbihSession(preset, count = 33, target = 33, dateKey = "2026-11-17")
        assertEquals(33, worshipRepo.observeDailyTasbihTotal("2026-11-17").first())

        // Prayer tracker never auto-marks and persists manual completion/missed
        val emptyLogs = worshipRepo.observePrayerLogsForDate("2026-11-17").first()
        assertTrue(emptyLogs.isEmpty())

        worshipRepo.setPrayerStatus("2026-11-17", PrayerName.FAJR, PrayerStatus.COMPLETED)
        worshipRepo.setPrayerStatus("2026-11-17", PrayerName.DHUHR, PrayerStatus.MISSED)
        val updatedLogs = worshipRepo.observePrayerLogsForDate("2026-11-17").first()
        assertEquals(PrayerStatus.COMPLETED, updatedLogs[PrayerName.FAJR])
        assertEquals(PrayerStatus.MISSED, updatedLogs[PrayerName.DHUHR])
    }

    @Test
    fun `navigation across tabs and instant bengali english switch works without crash`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("next_prayer_countdown").assertIsDisplayed()

        // Navigate to Qibla
        composeRule.onNodeWithTag("nav_qibla").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("qibla_compass_dial").assertIsDisplayed()

        // Navigate to Hadith & Dua
        composeRule.onNodeWithTag("nav_library").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("library_search_input").assertIsDisplayed()

        // Navigate to Worship -> Tasbih increment
        composeRule.onNodeWithTag("nav_worship").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbih_tap_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbih_count_text", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("worship_tab_tracker").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tracker_complete_fajr").assertIsDisplayed()

        // Navigate to Calendar
        composeRule.onNodeWithTag("nav_calendar").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hijri_offset_plus").assertIsDisplayed()

        // Return to Prayer and open Settings -> switch to Bengali -> verify instant UI update
        composeRule.onNodeWithTag("nav_prayer").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("open_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lang_option_bn").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("সেটিংস").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("সেটিংস").assertIsDisplayed()
    }
}
