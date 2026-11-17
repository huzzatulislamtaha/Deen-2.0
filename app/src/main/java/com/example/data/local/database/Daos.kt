package com.example.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths ORDER BY id ASC")
    fun observeAll(): Flow<List<HadithEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HadithEntity>)

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun count(): Int
}

@Dao
interface DuaDao {
    @Query("SELECT * FROM duas ORDER BY id ASC")
    fun observeAll(): Flow<List<DuaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DuaEntity>)

    @Query("SELECT COUNT(*) FROM duas")
    suspend fun count(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE itemId = :itemId AND itemType = :itemType")
    suspend fun delete(itemId: String, itemType: String)
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_presets ORDER BY isCustom ASC, id ASC")
    fun observePresets(): Flow<List<TasbihPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPresets(presets: List<TasbihPresetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TasbihPresetEntity)

    @Query("DELETE FROM tasbih_presets WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomPreset(id: String)

    @Query("SELECT * FROM tasbih_sessions ORDER BY completedAt DESC LIMIT 50")
    fun observeRecentSessions(): Flow<List<TasbihSessionEntity>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbih_sessions WHERE dateKey = :dateKey")
    fun observeDailyTotal(dateKey: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TasbihSessionEntity)
}

@Dao
interface PrayerTrackerDao {
    @Query("SELECT * FROM prayer_tracker_logs WHERE dateKey = :dateKey")
    fun observeByDate(dateKey: String): Flow<List<PrayerTrackerEntity>>

    @Query("SELECT * FROM prayer_tracker_logs WHERE dateKey >= :startDateKey AND dateKey <= :endDateKey ORDER BY dateKey DESC")
    fun observeRange(startDateKey: String, endDateKey: String): Flow<List<PrayerTrackerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(entry: PrayerTrackerEntity)

    @Query("DELETE FROM prayer_tracker_logs WHERE dateKey = :dateKey AND prayerKey = :prayerKey")
    suspend fun deleteLog(dateKey: String, prayerKey: String)
}

@Dao
interface NoteCategoryDao {
    @Query("SELECT * FROM note_categories ORDER BY isCustom ASC, sortOrder ASC, nameEn ASC")
    fun observeAll(): Flow<List<NoteCategoryEntity>>

    @Query("SELECT COUNT(*) FROM note_categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(categories: List<NoteCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: NoteCategoryEntity)

    @Update
    suspend fun update(category: NoteCategoryEntity)

    @Query("DELETE FROM note_categories WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomById(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC, updatedAt DESC")
    fun observeDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt, isPinned = 0, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL, updatedAt = :restoredAt WHERE id = :id")
    suspend fun restore(id: String, restoredAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyRecycleBin()

    @Query("UPDATE notes SET categoryId = :targetCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: String, targetCategoryId: String)
}
