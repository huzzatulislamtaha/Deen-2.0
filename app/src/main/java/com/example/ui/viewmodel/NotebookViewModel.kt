package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.NoteCategoryEntity
import com.example.data.local.database.NoteEntity
import com.example.data.repository.NotebookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.UnsavedNoteDraft
import com.example.domain.model.NoteSaveStatus
import com.example.domain.model.NoteSortOrder
import com.example.domain.model.NotebookFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class NoteEditorDraft(
    val id: String = "",
    val isNew: Boolean = true,
    val title: String = "",
    val content: String = "",
    val description: String = "",
    val categoryId: String = DeenDatabase.GENERAL_CATEGORY_ID,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val hadithCollection: String = "",
    val hadithNumber: String = "",
    val duaSource: String = "",
    val islamicBookName: String = "",
    val pageNumber: String = "",
    val websiteUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun hasMeaningfulContent(): Boolean =
        title.isNotBlank() ||
            content.isNotBlank() ||
            description.isNotBlank() ||
            tags.isNotBlank() ||
            hadithCollection.isNotBlank() ||
            islamicBookName.isNotBlank()

    fun toEntity(now: Long): NoteEntity = NoteEntity(
        id = id,
        title = title.trim(),
        content = content,
        description = description.trim(),
        categoryId = categoryId.ifBlank { DeenDatabase.GENERAL_CATEGORY_ID },
        tags = tags.trim(),
        isFavorite = isFavorite,
        isPinned = isPinned,
        hadithCollection = hadithCollection.trim(),
        hadithNumber = hadithNumber.trim(),
        duaSource = duaSource.trim(),
        islamicBookName = islamicBookName.trim(),
        pageNumber = pageNumber.trim(),
        websiteUrl = websiteUrl.trim(),
        isDeleted = false,
        deletedAt = null,
        createdAt = createdAt,
        updatedAt = now
    )

    fun toUnsavedSnapshot(): UnsavedNoteDraft = UnsavedNoteDraft(
        id = id,
        title = title,
        content = content,
        description = description,
        categoryId = categoryId,
        tags = tags,
        isFavorite = isFavorite,
        isPinned = isPinned,
        hadithCollection = hadithCollection,
        hadithNumber = hadithNumber,
        duaSource = duaSource,
        islamicBookName = islamicBookName,
        pageNumber = pageNumber,
        websiteUrl = websiteUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class NotebookUiState(
    val notes: List<NoteEntity> = emptyList(),
    val allActiveNotesCount: Int = 0,
    val favoriteCount: Int = 0,
    val pinnedCount: Int = 0,
    val recycleBinCount: Int = 0,
    val deletedNotes: List<NoteEntity> = emptyList(),
    val recentlyUpdated: List<NoteEntity> = emptyList(),
    val categories: List<NoteCategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: NotebookFilter = NotebookFilter.ALL,
    val selectedCategoryId: String? = null,
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC,
    val editorDraft: NoteEditorDraft? = null,
    val saveStatus: NoteSaveStatus = NoteSaveStatus.IDLE,
    val lastSavedAtMillis: Long? = null,
    val errorMessage: String? = null,
    val recoverableDraft: UnsavedNoteDraft? = null,
    val autosaveEnabled: Boolean = true,
    val defaultCategoryId: String = DeenDatabase.GENERAL_CATEGORY_ID,
    val effectiveTimeZoneId: String = "Asia/Dhaka",
    val isPinLocked: Boolean = false,
    val hasPinConfigured: Boolean = false
)

class NotebookViewModel(
    private val notebookRepo: NotebookRepository,
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(NotebookFilter.ALL)
    private val selectedCategoryId = MutableStateFlow<String?>(null)
    private val sortOrder = MutableStateFlow(NoteSortOrder.UPDATED_DESC)
    private val editorDraft = MutableStateFlow<NoteEditorDraft?>(null)
    private val saveStatus = MutableStateFlow(NoteSaveStatus.IDLE)
    private val lastSavedAtMillis = MutableStateFlow<Long?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val pinUnlockedForSession = MutableStateFlow(false)

    private var autosaveJob: Job? = null

    private val queryFilterFlow = combine(
        searchQuery,
        selectedFilter,
        selectedCategoryId,
        sortOrder
    ) { q, filter, catId, sort ->
        QueryConfig(q, filter, catId, sort)
    }

    private val editorMetaFlow = combine(
        editorDraft,
        saveStatus,
        lastSavedAtMillis,
        errorMessage,
        pinUnlockedForSession
    ) { draft, status, savedAt, err, unlocked ->
        EditorMeta(draft, status, savedAt, err, unlocked)
    }

    val uiState: StateFlow<NotebookUiState> = combine(
        notebookRepo.activeNotes,
        notebookRepo.deletedNotes,
        notebookRepo.categories,
        prefsRepo.userPreferences,
        queryFilterFlow,
        editorMetaFlow
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val activeNotes = args[0] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val deletedNotes = args[1] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = args[2] as List<NoteCategoryEntity>
        val prefs = args[3] as com.example.data.repository.UserPreferences
        val queryConfig = args[4] as QueryConfig
        val editorMeta = args[5] as EditorMeta

        val q = queryConfig.query.trim()
        val filtered = activeNotes
            .filter { note ->
                when (queryConfig.filter) {
                    NotebookFilter.ALL -> true
                    NotebookFilter.PINNED -> note.isPinned
                    NotebookFilter.FAVORITES -> note.isFavorite
                    NotebookFilter.RECYCLE_BIN -> false
                }
            }
            .filter { note ->
                queryConfig.categoryId == null || note.categoryId == queryConfig.categoryId
            }
            .filter { note ->
                if (q.isEmpty()) true else {
                    note.title.contains(q, ignoreCase = true) ||
                        note.content.contains(q, ignoreCase = true) ||
                        note.description.contains(q, ignoreCase = true) ||
                        note.tags.contains(q, ignoreCase = true) ||
                        note.hadithCollection.contains(q, ignoreCase = true) ||
                        note.hadithNumber.contains(q, ignoreCase = true) ||
                        note.islamicBookName.contains(q, ignoreCase = true)
                }
            }
            .let { list ->
                when (queryConfig.sortOrder) {
                    NoteSortOrder.UPDATED_DESC -> list.sortedWith(
                        compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.updatedAt }
                    )
                    NoteSortOrder.UPDATED_ASC -> list.sortedWith(
                        compareByDescending<NoteEntity> { it.isPinned }.thenBy { it.updatedAt }
                    )
                    NoteSortOrder.CREATED_DESC -> list.sortedWith(
                        compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.createdAt }
                    )
                    NoteSortOrder.TITLE_ASC -> list.sortedWith(
                        compareByDescending<NoteEntity> { it.isPinned }
                            .thenBy { it.title.lowercase(Locale.getDefault()) }
                    )
                }
            }

        val filteredDeleted = if (q.isEmpty()) deletedNotes else {
            deletedNotes.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.content.contains(q, ignoreCase = true) ||
                    it.tags.contains(q, ignoreCase = true)
            }
        }

        val recoverable = if (editorMeta.draft == null) {
            notebookRepo.parseUnsavedDraftSnapshot(prefs.unsavedNoteDraftJson)
        } else null

        val hasPin = prefs.isNotebookLockEnabled
        NotebookUiState(
            notes = if (queryConfig.filter == NotebookFilter.RECYCLE_BIN) filteredDeleted else filtered,
            allActiveNotesCount = activeNotes.size,
            favoriteCount = activeNotes.count { it.isFavorite },
            pinnedCount = activeNotes.count { it.isPinned },
            recycleBinCount = deletedNotes.size,
            deletedNotes = filteredDeleted,
            recentlyUpdated = activeNotes.sortedByDescending { it.updatedAt }.take(4),
            categories = categories,
            searchQuery = queryConfig.query,
            selectedFilter = queryConfig.filter,
            selectedCategoryId = queryConfig.categoryId,
            sortOrder = queryConfig.sortOrder,
            editorDraft = editorMeta.draft,
            saveStatus = editorMeta.status,
            lastSavedAtMillis = editorMeta.lastSavedAtMillis,
            errorMessage = editorMeta.error,
            recoverableDraft = recoverable,
            autosaveEnabled = prefs.notebookAutosaveEnabled,
            defaultCategoryId = prefs.notebookDefaultCategoryId,
            effectiveTimeZoneId = prefs.effectiveTimeZoneId,
            isPinLocked = hasPin && !editorMeta.unlocked,
            hasPinConfigured = hasPin
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotebookUiState()
    )

    fun attemptUnlockPin(pin: String, storedHash: String): Boolean {
        val ok = NotebookRepository.verifyPin(pin, storedHash)
        if (ok) {
            pinUnlockedForSession.value = true
            errorMessage.value = null
        }
        return ok
    }

    fun lockNotebookNow() {
        pinUnlockedForSession.value = false
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: NotebookFilter) {
        selectedFilter.value = filter
        if (filter == NotebookFilter.RECYCLE_BIN) {
            selectedCategoryId.value = null
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        selectedCategoryId.value = categoryId
        if (selectedFilter.value == NotebookFilter.RECYCLE_BIN) {
            selectedFilter.value = NotebookFilter.ALL
        }
    }

    fun setSortOrder(order: NoteSortOrder) {
        sortOrder.value = order
    }

    fun openNewNote(
        defaultCategoryId: String = uiState.value.defaultCategoryId,
        prefillTitle: String = "",
        prefillContent: String = "",
        prefillBook: String = ""
    ) {
        val now = System.currentTimeMillis()
        autosaveJob?.cancel()
        saveStatus.value = NoteSaveStatus.IDLE
        errorMessage.value = null
        editorDraft.value = NoteEditorDraft(
            id = "note_${UUID.randomUUID()}",
            isNew = true,
            title = prefillTitle,
            content = prefillContent,
            categoryId = defaultCategoryId.ifBlank { DeenDatabase.GENERAL_CATEGORY_ID },
            islamicBookName = prefillBook,
            createdAt = now,
            updatedAt = now
        )
    }

    fun openExistingNote(note: NoteEntity) {
        autosaveJob?.cancel()
        saveStatus.value = NoteSaveStatus.IDLE
        errorMessage.value = null
        lastSavedAtMillis.value = note.updatedAt
        editorDraft.value = NoteEditorDraft(
            id = note.id,
            isNew = false,
            title = note.title,
            content = note.content,
            description = note.description,
            categoryId = note.categoryId,
            tags = note.tags,
            isFavorite = note.isFavorite,
            isPinned = note.isPinned,
            hadithCollection = note.hadithCollection,
            hadithNumber = note.hadithNumber,
            duaSource = note.duaSource,
            islamicBookName = note.islamicBookName,
            pageNumber = note.pageNumber,
            websiteUrl = note.websiteUrl,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }

    fun recoverUnsavedDraft(draft: UnsavedNoteDraft) {
        autosaveJob?.cancel()
        saveStatus.value = NoteSaveStatus.IDLE
        editorDraft.value = NoteEditorDraft(
            id = draft.id,
            isNew = false,
            title = draft.title,
            content = draft.content,
            description = draft.description,
            categoryId = draft.categoryId,
            tags = draft.tags,
            isFavorite = draft.isFavorite,
            isPinned = draft.isPinned,
            hadithCollection = draft.hadithCollection,
            hadithNumber = draft.hadithNumber,
            duaSource = draft.duaSource,
            islamicBookName = draft.islamicBookName,
            pageNumber = draft.pageNumber,
            websiteUrl = draft.websiteUrl,
            createdAt = draft.createdAt,
            updatedAt = draft.updatedAt
        )
    }

    fun discardRecoverableDraft() {
        viewModelScope.launch { notebookRepo.clearUnsavedDraftSnapshot() }
    }

    fun updateDraft(transform: (NoteEditorDraft) -> NoteEditorDraft) {
        val current = editorDraft.value ?: return
        val updated = transform(current).copy(updatedAt = System.currentTimeMillis())
        editorDraft.value = updated
        viewModelScope.launch {
            notebookRepo.persistUnsavedDraftSnapshot(updated.toUnsavedSnapshot())
        }
        if (uiState.value.autosaveEnabled) {
            scheduleDebouncedAutosave()
        }
    }

    private fun scheduleDebouncedAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(550L)
            persistCurrentDraftInternal()
        }
    }

    fun saveAndCloseEditor() {
        autosaveJob?.cancel()
        viewModelScope.launch {
            persistCurrentDraftInternal()
            editorDraft.value = null
            saveStatus.value = NoteSaveStatus.IDLE
        }
    }

    fun flushSaveIfEditing() {
        val draft = editorDraft.value ?: return
        if (!draft.hasMeaningfulContent()) return
        autosaveJob?.cancel()
        viewModelScope.launch {
            persistCurrentDraftInternal()
        }
    }

    fun closeEditorWithoutSavingIfEmpty() {
        autosaveJob?.cancel()
        val draft = editorDraft.value
        viewModelScope.launch {
            if (draft != null && draft.hasMeaningfulContent()) {
                persistCurrentDraftInternal()
            } else {
                notebookRepo.clearUnsavedDraftSnapshot()
            }
            editorDraft.value = null
            saveStatus.value = NoteSaveStatus.IDLE
        }
    }

    private suspend fun persistCurrentDraftInternal() {
        val draft = editorDraft.value ?: return
        if (!draft.hasMeaningfulContent()) return
        saveStatus.value = NoteSaveStatus.SAVING
        val now = System.currentTimeMillis()
        val entity = draft.toEntity(now)
        val result = notebookRepo.saveNote(entity)
        result.onSuccess {
            editorDraft.value = draft.copy(isNew = false, updatedAt = now)
            lastSavedAtMillis.value = now
            saveStatus.value = NoteSaveStatus.SAVED
            errorMessage.value = null
        }.onFailure { err ->
            saveStatus.value = NoteSaveStatus.ERROR
            errorMessage.value = err.message ?: "Failed to save note locally"
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            notebookRepo.toggleFavorite(note).onFailure { errorMessage.value = it.message }
        }
    }

    fun togglePinned(note: NoteEntity) {
        viewModelScope.launch {
            notebookRepo.togglePinned(note).onFailure { errorMessage.value = it.message }
        }
    }

    fun softDeleteNote(noteId: String) {
        viewModelScope.launch {
            notebookRepo.softDeleteNote(noteId).onFailure { errorMessage.value = it.message }
            if (editorDraft.value?.id == noteId) {
                editorDraft.value = null
            }
        }
    }

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            notebookRepo.restoreNote(noteId).onFailure { errorMessage.value = it.message }
        }
    }

    fun permanentlyDeleteNote(noteId: String) {
        viewModelScope.launch {
            notebookRepo.permanentlyDeleteNote(noteId).onFailure { errorMessage.value = it.message }
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            notebookRepo.emptyRecycleBin().onFailure { errorMessage.value = it.message }
        }
    }

    fun createCustomCategory(nameEn: String, nameBn: String) {
        viewModelScope.launch {
            notebookRepo.createCustomCategory(nameEn, nameBn)
                .onFailure { errorMessage.value = it.message }
        }
    }

    fun renameCustomCategory(category: NoteCategoryEntity, nameEn: String, nameBn: String) {
        viewModelScope.launch {
            notebookRepo.renameCustomCategory(category, nameEn, nameBn)
                .onFailure { errorMessage.value = it.message }
        }
    }

    fun deleteCustomCategory(categoryId: String, targetCategoryId: String = DeenDatabase.GENERAL_CATEGORY_ID) {
        viewModelScope.launch {
            notebookRepo.deleteCustomCategorySafely(categoryId, targetCategoryId)
                .onFailure { errorMessage.value = it.message }
            if (selectedCategoryId.value == categoryId) {
                selectedCategoryId.value = null
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    private data class QueryConfig(
        val query: String,
        val filter: NotebookFilter,
        val categoryId: String?,
        val sortOrder: NoteSortOrder
    )

    private data class EditorMeta(
        val draft: NoteEditorDraft?,
        val status: NoteSaveStatus,
        val lastSavedAtMillis: Long?,
        val error: String?,
        val unlocked: Boolean
    )

    companion object {
        fun formatTimestamp(epochMillis: Long, timeZoneId: String): String {
            val tz = TimeZone.getTimeZone(timeZoneId)
            val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).apply {
                timeZone = tz
            }
            return fmt.format(Date(epochMillis))
        }

        fun formatShortTime(epochMillis: Long, timeZoneId: String): String {
            val tz = TimeZone.getTimeZone(timeZoneId)
            val fmt = SimpleDateFormat("hh:mm a", Locale.US).apply {
                timeZone = tz
            }
            return fmt.format(Date(epochMillis))
        }
    }
}
