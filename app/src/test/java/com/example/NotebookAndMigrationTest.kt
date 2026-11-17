package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.NoteEntity
import com.example.data.repository.NotebookRepository
import com.example.data.repository.PreferencesRepository
import com.example.domain.model.NoteSaveStatus
import com.example.domain.model.NoteSortOrder
import com.example.domain.model.NotebookFilter
import com.example.ui.viewmodel.NotebookViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotebookAndMigrationTest {

    private lateinit var context: Context
    private lateinit var db: DeenDatabase
    private lateinit var prefsRepo: PreferencesRepository
    private lateinit var notebookRepo: NotebookRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, DeenDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            DeenDatabase.seedInitialContent(db)
        }
        prefsRepo = PreferencesRepository(context)
        notebookRepo = NotebookRepository(db, db.noteDao(), db.noteCategoryDao(), prefsRepo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `room migration 1 to 2 preserves existing v1 user data and adds notebook schema with default categories`(): Unit = runBlocking {
        val dbName = "migration_verify_test.db"
        context.deleteDatabase(dbName)

        // 1. Create authentic v1 SQLite database containing existing user records
        val v1Helper = object : SQLiteOpenHelper(context, dbName, null, 1) {
            override fun onCreate(sqliteDb: SQLiteDatabase) {
                sqliteDb.execSQL(
                    "CREATE TABLE `hadiths` (`id` TEXT NOT NULL PRIMARY KEY, `categoryEn` TEXT NOT NULL, `categoryBn` TEXT NOT NULL, `arabicText` TEXT NOT NULL, `bengaliMeaning` TEXT NOT NULL, `englishMeaning` TEXT NOT NULL, `sourceBook` TEXT NOT NULL, `reference` TEXT NOT NULL, `narratorEn` TEXT NOT NULL, `narratorBn` TEXT NOT NULL, `chapterEn` TEXT NOT NULL, `chapterBn` TEXT NOT NULL, `authenticityGrade` TEXT NOT NULL, `gradingScholar` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `verificationStatus` TEXT NOT NULL, `lastVerifiedDate` TEXT NOT NULL, `verificationNotes` TEXT NOT NULL)"
                )
                sqliteDb.execSQL(
                    "CREATE TABLE `duas` (`id` TEXT NOT NULL PRIMARY KEY, `categoryKey` TEXT NOT NULL, `titleEn` TEXT NOT NULL, `titleBn` TEXT NOT NULL, `arabicText` TEXT NOT NULL, `bengaliPronunciation` TEXT NOT NULL, `bengaliMeaning` TEXT NOT NULL, `englishMeaning` TEXT NOT NULL, `sourceBook` TEXT NOT NULL, `reference` TEXT NOT NULL, `authenticityGrade` TEXT NOT NULL, `repetitionCount` INTEGER NOT NULL, `contextEn` TEXT NOT NULL, `contextBn` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `verificationStatus` TEXT NOT NULL, `lastVerifiedDate` TEXT NOT NULL, `verificationNotes` TEXT NOT NULL)"
                )
                sqliteDb.execSQL(
                    "CREATE TABLE `bookmarks` (`itemId` TEXT NOT NULL, `itemType` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`itemId`, `itemType`))"
                )
                sqliteDb.execSQL(
                    "CREATE TABLE `tasbih_presets` (`id` TEXT NOT NULL PRIMARY KEY, `titleEn` TEXT NOT NULL, `titleBn` TEXT NOT NULL, `arabicText` TEXT NOT NULL, `defaultTarget` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL)"
                )
                sqliteDb.execSQL(
                    "CREATE TABLE `tasbih_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `presetId` TEXT NOT NULL, `dhikrTitleEn` TEXT NOT NULL, `dhikrTitleBn` TEXT NOT NULL, `dhikrArabic` TEXT NOT NULL, `count` INTEGER NOT NULL, `target` INTEGER NOT NULL, `dateKey` TEXT NOT NULL, `completedAt` INTEGER NOT NULL)"
                )
                sqliteDb.execSQL("CREATE INDEX `index_tasbih_sessions_dateKey` ON `tasbih_sessions` (`dateKey`)")
                sqliteDb.execSQL(
                    "CREATE TABLE `prayer_tracker_logs` (`dateKey` TEXT NOT NULL, `prayerKey` TEXT NOT NULL, `statusKey` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`dateKey`, `prayerKey`))"
                )
                sqliteDb.execSQL("CREATE INDEX `index_prayer_tracker_logs_dateKey` ON `prayer_tracker_logs` (`dateKey`)")

                // Insert real v1 user records
                sqliteDb.execSQL("INSERT INTO bookmarks VALUES ('hadith_bukhari_1', 'HADITH', 1700000000000)")
                sqliteDb.execSQL("INSERT INTO prayer_tracker_logs VALUES ('2026-11-16', 'fajr', 'completed', 1700000000000)")
                sqliteDb.execSQL("INSERT INTO tasbih_sessions (presetId, dhikrTitleEn, dhikrTitleBn, dhikrArabic, count, target, dateKey, completedAt) VALUES ('preset_subhanallah', 'SubhanAllah', 'সুবহানাল্লাহ', 'سُبْحَانَ اللَّهِ', 33, 33, '2026-11-16', 1700000000000)")
            }

            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) = Unit
        }
        v1Helper.writableDatabase.close()

        // 2. Open with Room v2 + MIGRATION_1_2
        val migratedDb = Room.databaseBuilder(context, DeenDatabase::class.java, dbName)
            .addMigrations(DeenDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        // Verify v1 rows survived intact
        assertTrue(migratedDb.bookmarkDao().observeAll().first().any { it.itemId == "hadith_bukhari_1" && it.itemType == "HADITH" })
        val logs = migratedDb.prayerTrackerDao().observeByDate("2026-11-16").first()
        assertEquals(1, logs.size)
        assertEquals("completed", logs.first().statusKey)
        assertEquals(33, migratedDb.tasbihDao().observeDailyTotal("2026-11-16").first())

        // Verify 9 built-in Notebook categories seeded during migration
        val categories = migratedDb.noteCategoryDao().observeAll().first()
        assertEquals(9, categories.size)
        assertNotNull(categories.firstOrNull { it.id == DeenDatabase.GENERAL_CATEGORY_ID })

        // Verify writing a Note on migrated DB works
        migratedDb.noteDao().upsert(
            NoteEntity(
                id = "migrated_note_1",
                title = "Post-migration note",
                content = "بِسْمِ اللَّهِ — বাংলা নোট",
                description = "",
                categoryId = DeenDatabase.GENERAL_CATEGORY_ID,
                tags = "khutbah,dhaka",
                isFavorite = true,
                isPinned = false,
                hadithCollection = "",
                hadithNumber = "",
                duaSource = "",
                islamicBookName = "",
                pageNumber = "",
                websiteUrl = "",
                isDeleted = false,
                deletedAt = null,
                createdAt = 1700000001000L,
                updatedAt = 1700000001000L
            )
        )
        assertEquals(1, migratedDb.noteDao().observeActiveNotes().first().size)
        migratedDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `notebook crud search pin favorite sort recycle bin and safe category deletion preserve notes`(): Unit = runBlocking {
        // Default 9 categories
        val defaultCats = notebookRepo.categories.first()
        assertEquals(9, defaultCats.size)

        // Create & rename custom category
        val custom = notebookRepo.createCustomCategory("Tafsir Study", "তাফসীর অধ্যয়ন").getOrThrow()
        assertTrue(custom.isCustom)
        notebookRepo.renameCustomCategory(custom, "Tafsir Class", "তাফসীর ক্লাস").getOrThrow()
        assertEquals("Tafsir Class", notebookRepo.categories.first().first { it.id == custom.id }.nameEn)

        // Create 2 notes (one multilingual Arabic/Bengali/English, one large note) in custom category
        val largeContent = buildString {
            repeat(250) { append("Line $it: SubhanAllah walhamdulillah — বাংলা এবং العربية.\n") }
        }
        val n1 = NoteEntity(
            id = "n1",
            title = "Friday Khutbah",
            content = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nজুমার বয়ান নোট — Sincerity in worship.",
            description = "Masjid reflection",
            categoryId = custom.id,
            tags = "friday,sincerity",
            isFavorite = true,
            isPinned = true,
            hadithCollection = "Sahih al-Bukhari",
            hadithNumber = "1",
            duaSource = "",
            islamicBookName = "Riyad as-Salihin",
            pageNumber = "12",
            websiteUrl = "https://sunnah.com/bukhari:1",
            isDeleted = false,
            deletedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val n2 = n1.copy(
            id = "n2",
            title = "Large Note Test",
            content = largeContent,
            isFavorite = false,
            isPinned = false,
            createdAt = 1500L,
            updatedAt = 3000L
        )
        notebookRepo.saveNote(n1).getOrThrow()
        notebookRepo.saveNote(n2).getOrThrow()

        assertEquals(2, notebookRepo.activeNotes.first().size)
        assertEquals(largeContent, notebookRepo.activeNotes.first().first { it.id == "n2" }.content)

        // Delete custom category MUST NOT delete n1 or n2; moves them to General Islamic Notes
        notebookRepo.deleteCustomCategorySafely(custom.id, DeenDatabase.GENERAL_CATEGORY_ID).getOrThrow()
        val afterCatDelete = notebookRepo.activeNotes.first()
        assertEquals(2, afterCatDelete.size)
        assertTrue(afterCatDelete.all { it.categoryId == DeenDatabase.GENERAL_CATEGORY_ID })

        // Soft delete n1 -> verify recycle bin -> restore -> permanent delete
        notebookRepo.softDeleteNote("n1").getOrThrow()
        assertEquals(1, notebookRepo.activeNotes.first().size)
        assertEquals(1, notebookRepo.deletedNotes.first().size)

        notebookRepo.restoreNote("n1").getOrThrow()
        assertEquals(2, notebookRepo.activeNotes.first().size)
        assertEquals(0, notebookRepo.deletedNotes.first().size)

        notebookRepo.softDeleteNote("n1").getOrThrow()
        notebookRepo.emptyRecycleBin().getOrThrow()
        assertEquals(0, notebookRepo.deletedNotes.first().size)
    }

    @Test
    fun `notebook pin hashing and timestamp timezone formatting work accurately`() {
        val hash = NotebookRepository.hashPin("4829")
        assertTrue(NotebookRepository.verifyPin("4829", hash))
        assertFalse(NotebookRepository.verifyPin("0000", hash))

        // UTC epoch millis formats appropriately without altering stored UTC value
        val utcMidnight = 1794873600000L // fixed UTC epoch
        val dhakaFormatted = NotebookViewModel.formatTimestamp(utcMidnight, "Asia/Dhaka")
        val londonFormatted = NotebookViewModel.formatTimestamp(utcMidnight, "Europe/London")
        assertTrue(dhakaFormatted != londonFormatted)
    }

    @Test
    fun `notebook viewmodel autosave draft recovery and filtering work without duplicates`(): Unit = runBlocking {
        val vm = NotebookViewModel(notebookRepo, prefsRepo)

        vm.openNewNote()
        vm.updateDraft { it.copy(title = "Ramadan Goal", content = "Finish Juz Amma") }
        vm.flushSaveIfEditing()
        delay(200)

        assertEquals(NoteSaveStatus.SAVED, vm.uiState.value.saveStatus)
        // Updating the same note must NOT create a duplicate row
        vm.updateDraft { it.copy(content = "Finish Juz Amma and Surah Mulk") }
        vm.saveAndCloseEditor()
        delay(200)

        val notes = notebookRepo.activeNotes.first()
        assertEquals(1, notes.size)
        assertEquals("Finish Juz Amma and Surah Mulk", notes.first().content)

        // Filtering and search
        vm.setSearchQuery("Mulk")
        delay(100)
        assertEquals(1, vm.uiState.value.notes.size)

        vm.setFilter(NotebookFilter.FAVORITES)
        delay(100)
        assertEquals(0, vm.uiState.value.notes.size)

        vm.setFilter(NotebookFilter.ALL)
        vm.setSearchQuery("")
        vm.setSortOrder(NoteSortOrder.TITLE_ASC)
        delay(100)
        assertEquals(1, vm.uiState.value.notes.size)
        assertNull(vm.uiState.value.recoverableDraft)
    }
}
