package com.example.data.local.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey val id: String,
    val categoryEn: String,
    val categoryBn: String,
    val arabicText: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val sourceBook: String,
    val reference: String,
    val narratorEn: String,
    val narratorBn: String,
    val chapterEn: String,
    val chapterBn: String,
    val authenticityGrade: String,
    val gradingScholar: String,
    val sourceUrl: String,
    val verificationStatus: String,
    val lastVerifiedDate: String,
    val verificationNotes: String
)

@Entity(tableName = "duas")
data class DuaEntity(
    @PrimaryKey val id: String,
    val categoryKey: String,
    val titleEn: String,
    val titleBn: String,
    val arabicText: String,
    val bengaliPronunciation: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val sourceBook: String,
    val reference: String,
    val authenticityGrade: String,
    val repetitionCount: Int,
    val contextEn: String,
    val contextBn: String,
    val sourceUrl: String,
    val verificationStatus: String,
    val lastVerifiedDate: String,
    val verificationNotes: String
)

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["itemId", "itemType"]
)
data class BookmarkEntity(
    val itemId: String,
    val itemType: String, // "HADITH" or "DUA"
    val createdAt: Long
)

@Entity(tableName = "tasbih_presets")
data class TasbihPresetEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleBn: String,
    val arabicText: String,
    val defaultTarget: Int,
    val isCustom: Boolean
)

@Entity(
    tableName = "tasbih_sessions",
    indices = [Index(value = ["dateKey"])]
)
data class TasbihSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: String,
    val dhikrTitleEn: String,
    val dhikrTitleBn: String,
    val dhikrArabic: String,
    val count: Int,
    val target: Int,
    val dateKey: String,
    val completedAt: Long
)

@Entity(
    tableName = "prayer_tracker_logs",
    primaryKeys = ["dateKey", "prayerKey"],
    indices = [Index(value = ["dateKey"])]
)
data class PrayerTrackerEntity(
    val dateKey: String,
    val prayerKey: String,
    val statusKey: String,
    val updatedAt: Long
)

@Entity(tableName = "note_categories")
data class NoteCategoryEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameBn: String,
    val isCustom: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isDeleted"])
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val description: String = "",
    val categoryId: String = "cat_general",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val hadithCollection: String = "",
    val hadithNumber: String = "",
    val duaSource: String = "",
    val islamicBookName: String = "",
    val pageNumber: String = "",
    val websiteUrl: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
