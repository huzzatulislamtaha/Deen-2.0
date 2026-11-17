package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.NoteCategoryDao
import com.example.data.local.database.NoteCategoryEntity
import com.example.data.local.database.NoteDao
import com.example.data.local.database.NoteEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

@JsonClass(generateAdapter = true)
data class UnsavedNoteDraft(
    val id: String,
    val title: String,
    val content: String,
    val description: String,
    val categoryId: String,
    val tags: String,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val hadithCollection: String,
    val hadithNumber: String,
    val duaSource: String,
    val islamicBookName: String,
    val pageNumber: String,
    val websiteUrl: String,
    val createdAt: Long,
    val updatedAt: Long
)

class NotebookRepository(
    private val database: DeenDatabase,
    private val noteDao: NoteDao,
    private val categoryDao: NoteCategoryDao,
    private val prefsRepo: PreferencesRepository
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val draftAdapter = moshi.adapter(UnsavedNoteDraft::class.java)

    val activeNotes: Flow<List<NoteEntity>> = noteDao.observeActiveNotes()
    val deletedNotes: Flow<List<NoteEntity>> = noteDao.observeDeletedNotes()
    val activeNoteCount: Flow<Int> = noteDao.observeActiveCount()
    val categories: Flow<List<NoteCategoryEntity>> = categoryDao.observeAll()

    suspend fun ensureDefaultCategories() {
        if (categoryDao.count() == 0) {
            categoryDao.insertMissing(DeenDatabase.defaultNoteCategories)
        }
    }

    suspend fun saveNote(note: NoteEntity): Result<Unit> = runCatching {
        database.withTransaction {
            ensureDefaultCategories()
            noteDao.upsert(note)
        }
        prefsRepo.saveUnsavedNoteDraft("")
    }

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getById(id)

    suspend fun softDeleteNote(id: String): Result<Unit> = runCatching {
        noteDao.softDelete(id, System.currentTimeMillis())
    }

    suspend fun restoreNote(id: String): Result<Unit> = runCatching {
        noteDao.restore(id, System.currentTimeMillis())
    }

    suspend fun permanentlyDeleteNote(id: String): Result<Unit> = runCatching {
        noteDao.hardDelete(id)
    }

    suspend fun emptyRecycleBin(): Result<Unit> = runCatching {
        noteDao.emptyRecycleBin()
    }

    suspend fun toggleFavorite(note: NoteEntity): Result<Unit> = runCatching {
        noteDao.upsert(
            note.copy(
                isFavorite = !note.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun togglePinned(note: NoteEntity): Result<Unit> = runCatching {
        noteDao.upsert(
            note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createCustomCategory(nameEn: String, nameBn: String): Result<NoteCategoryEntity> = runCatching {
        val cleanEn = nameEn.trim()
        require(cleanEn.isNotEmpty()) { "Category name cannot be empty" }
        val cleanBn = nameBn.trim().ifEmpty { cleanEn }
        val category = NoteCategoryEntity(
            id = "cat_custom_${UUID.randomUUID()}",
            nameEn = cleanEn,
            nameBn = cleanBn,
            isCustom = true,
            sortOrder = 100,
            createdAt = System.currentTimeMillis()
        )
        categoryDao.upsert(category)
        category
    }

    suspend fun renameCustomCategory(
        category: NoteCategoryEntity,
        newNameEn: String,
        newNameBn: String
    ): Result<Unit> = runCatching {
        if (!category.isCustom) return@runCatching
        val cleanEn = newNameEn.trim()
        require(cleanEn.isNotEmpty()) { "Category name cannot be empty" }
        val cleanBn = newNameBn.trim().ifEmpty { cleanEn }
        categoryDao.update(category.copy(nameEn = cleanEn, nameBn = cleanBn))
    }

    suspend fun deleteCustomCategorySafely(
        categoryId: String,
        targetReassignCategoryId: String = DeenDatabase.GENERAL_CATEGORY_ID
    ): Result<Unit> = runCatching {
        if (categoryId == DeenDatabase.GENERAL_CATEGORY_ID) return@runCatching
        database.withTransaction {
            ensureDefaultCategories()
            val safeTarget = if (targetReassignCategoryId == categoryId) {
                DeenDatabase.GENERAL_CATEGORY_ID
            } else {
                targetReassignCategoryId
            }
            noteDao.reassignCategory(categoryId, safeTarget)
            categoryDao.deleteCustomById(categoryId)
        }
    }

    suspend fun persistUnsavedDraftSnapshot(draft: UnsavedNoteDraft) {
        runCatching {
            prefsRepo.saveUnsavedNoteDraft(draftAdapter.toJson(draft))
        }
    }

    fun parseUnsavedDraftSnapshot(json: String): UnsavedNoteDraft? {
        if (json.isBlank()) return null
        return runCatching { draftAdapter.fromJson(json) }.getOrNull()
    }

    suspend fun clearUnsavedDraftSnapshot() {
        runCatching { prefsRepo.saveUnsavedNoteDraft("") }
    }

    companion object {
        private const val PIN_SALT = "deen_notebook_local_pin_v1"

        fun hashPin(pin: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$PIN_SALT:$pin".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun verifyPin(pin: String, storedHash: String): Boolean {
            if (storedHash.isBlank()) return true
            return hashPin(pin) == storedHash
        }
    }
}
