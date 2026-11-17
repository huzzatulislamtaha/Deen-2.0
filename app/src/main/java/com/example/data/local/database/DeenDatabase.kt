package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.content.VerifiedIslamicContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HadithEntity::class,
        DuaEntity::class,
        BookmarkEntity::class,
        TasbihPresetEntity::class,
        TasbihSessionEntity::class,
        PrayerTrackerEntity::class,
        NoteCategoryEntity::class,
        NoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DeenDatabase : RoomDatabase() {
    abstract fun hadithDao(): HadithDao
    abstract fun duaDao(): DuaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun prayerTrackerDao(): PrayerTrackerDao
    abstract fun noteCategoryDao(): NoteCategoryDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val GENERAL_CATEGORY_ID = "cat_general"

        @Volatile
        private var INSTANCE: DeenDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `note_categories` (
                        `id` TEXT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameBn` TEXT NOT NULL,
                        `isCustom` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `isPinned` INTEGER NOT NULL,
                        `hadithCollection` TEXT NOT NULL,
                        `hadithNumber` TEXT NOT NULL,
                        `duaSource` TEXT NOT NULL,
                        `islamicBookName` TEXT NOT NULL,
                        `pageNumber` TEXT NOT NULL,
                        `websiteUrl` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_categoryId` ON `notes` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_updatedAt` ON `notes` (`updatedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isDeleted` ON `notes` (`isDeleted`)")

                defaultNoteCategories.forEach { cat ->
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO `note_categories` (`id`, `nameEn`, `nameBn`, `isCustom`, `sortOrder`, `createdAt`)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        arrayOf<Any>(
                            cat.id,
                            cat.nameEn,
                            cat.nameBn,
                            if (cat.isCustom) 1 else 0,
                            cat.sortOrder,
                            cat.createdAt
                        )
                    )
                }
            }
        }

        fun getInstance(context: Context): DeenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): DeenDatabase {
            lateinit var db: DeenDatabase
            db = Room.databaseBuilder(
                appContext,
                DeenDatabase::class.java,
                "deen_database.db"
            )
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(sqliteDb: SupportSQLiteDatabase) {
                        super.onCreate(sqliteDb)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialContent(db)
                        }
                    }
                })
                .build()
            return db
        }

        suspend fun seedInitialContentIfEmpty(db: DeenDatabase) {
            if (db.hadithDao().count() == 0 || db.duaDao().count() == 0) {
                seedInitialContent(db)
            } else if (db.noteCategoryDao().count() == 0) {
                db.noteCategoryDao().insertMissing(defaultNoteCategories)
            }
        }

        suspend fun seedInitialContent(db: DeenDatabase) {
            db.hadithDao().upsertAll(
                VerifiedIslamicContent.hadiths.map { h ->
                    HadithEntity(
                        id = h.id,
                        categoryEn = h.categoryEn,
                        categoryBn = h.categoryBn,
                        arabicText = h.arabicText,
                        bengaliMeaning = h.bengaliMeaning,
                        englishMeaning = h.englishMeaning,
                        sourceBook = h.sourceBook,
                        reference = h.reference,
                        narratorEn = h.narratorEn,
                        narratorBn = h.narratorBn,
                        chapterEn = h.chapterEn,
                        chapterBn = h.chapterBn,
                        authenticityGrade = h.authenticityGrade,
                        gradingScholar = h.gradingScholar,
                        sourceUrl = h.sourceUrl,
                        verificationStatus = h.verificationStatus.key,
                        lastVerifiedDate = h.lastVerifiedDate,
                        verificationNotes = h.verificationNotes
                    )
                }
            )
            db.duaDao().upsertAll(
                VerifiedIslamicContent.duas.map { d ->
                    DuaEntity(
                        id = d.id,
                        categoryKey = d.category.key,
                        titleEn = d.titleEn,
                        titleBn = d.titleBn,
                        arabicText = d.arabicText,
                        bengaliPronunciation = d.bengaliPronunciation,
                        bengaliMeaning = d.bengaliMeaning,
                        englishMeaning = d.englishMeaning,
                        sourceBook = d.sourceBook,
                        reference = d.reference,
                        authenticityGrade = d.authenticityGrade,
                        repetitionCount = d.repetitionCount,
                        contextEn = d.contextEn,
                        contextBn = d.contextBn,
                        sourceUrl = d.sourceUrl,
                        verificationStatus = d.verificationStatus.key,
                        lastVerifiedDate = d.lastVerifiedDate,
                        verificationNotes = d.verificationNotes
                    )
                }
            )
            db.tasbihDao().upsertPresets(defaultTasbihPresets)
            db.noteCategoryDao().insertMissing(defaultNoteCategories)
        }

        val defaultNoteCategories: List<NoteCategoryEntity> = listOf(
            NoteCategoryEntity(GENERAL_CATEGORY_ID, "General Islamic Notes", "সাধারণ ইসলামি নোট", false, 1, 0L),
            NoteCategoryEntity("cat_quran", "Quran Reflections", "কুরআন তাদাব্বুর", false, 2, 0L),
            NoteCategoryEntity("cat_hadith", "Hadith Notes", "হাদিস নোট", false, 3, 0L),
            NoteCategoryEntity("cat_dua", "Dua Collection", "দোয়া সংকলন", false, 4, 0L),
            NoteCategoryEntity("cat_reminders", "Daily Islamic Reminders", "দৈনন্দিন ইসলামি অনুস্মারক", false, 5, 0L),
            NoteCategoryEntity("cat_friday", "Friday Notes", "জুমার নোট", false, 6, 0L),
            NoteCategoryEntity("cat_ramadan", "Ramadan Journal", "রমজান জার্নাল", false, 7, 0L),
            NoteCategoryEntity("cat_goals", "Personal Islamic Goals", "ব্যক্তিগত ইসলামি লক্ষ্য", false, 8, 0L),
            NoteCategoryEntity("cat_study", "Study Notes", "অধ্যয়ন নোট", false, 9, 0L)
        )

        val defaultTasbihPresets = listOf(
            TasbihPresetEntity(
                id = "preset_subhanallah",
                titleEn = "SubhanAllah",
                titleBn = "সুবহানাল্লাহ",
                arabicText = "سُبْحَانَ اللَّهِ",
                defaultTarget = 33,
                isCustom = false
            ),
            TasbihPresetEntity(
                id = "preset_alhamdulillah",
                titleEn = "Alhamdulillah",
                titleBn = "আলহামদুলিল্লাহ",
                arabicText = "الْحَمْدُ لِلَّهِ",
                defaultTarget = 33,
                isCustom = false
            ),
            TasbihPresetEntity(
                id = "preset_allahuakbar",
                titleEn = "Allahu Akbar",
                titleBn = "আল্লাহু আকবার",
                arabicText = "اللَّهُ أَكْبَرُ",
                defaultTarget = 34,
                isCustom = false
            ),
            TasbihPresetEntity(
                id = "preset_astaghfirullah",
                titleEn = "Astaghfirullah",
                titleBn = "আস্তাগফিরুল্লাহ",
                arabicText = "أَسْتَغْفِرُ اللَّهَ",
                defaultTarget = 100,
                isCustom = false
            ),
            TasbihPresetEntity(
                id = "preset_subhanallah_bihamdihi",
                titleEn = "SubhanAllahi wa bihamdihi",
                titleBn = "সুবহানাল্লাহি ওয়া বিহামদিহি",
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                defaultTarget = 100,
                isCustom = false
            ),
            TasbihPresetEntity(
                id = "preset_tahlil",
                titleEn = "La ilaha illAllah",
                titleBn = "লা ইলাহা ইল্লাল্লাহ",
                arabicText = "لَا إِلَهَ إِلَّا اللَّهُ",
                defaultTarget = 100,
                isCustom = false
            )
        )
    }
}
