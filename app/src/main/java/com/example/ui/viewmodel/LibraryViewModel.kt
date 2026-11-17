package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ContentRepository
import com.example.domain.model.DuaCategory
import com.example.domain.model.DuaItem
import com.example.domain.model.HadithItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    HADITH,
    DUA,
    BOOKMARKS
}

data class LibraryUiState(
    val activeTab: LibraryTab = LibraryTab.HADITH,
    val searchQuery: String = "",
    val selectedDuaCategory: DuaCategory? = null,
    val hadiths: List<HadithItem> = emptyList(),
    val duas: List<DuaItem> = emptyList(),
    val bookmarkedHadiths: List<HadithItem> = emptyList(),
    val bookmarkedDuas: List<DuaItem> = emptyList()
)

class LibraryViewModel(
    private val contentRepo: ContentRepository
) : ViewModel() {

    private val activeTab = MutableStateFlow(LibraryTab.HADITH)
    private val searchQuery = MutableStateFlow("")
    private val selectedDuaCategory = MutableStateFlow<DuaCategory?>(null)

    init {
        viewModelScope.launch {
            contentRepo.ensureSeeded()
        }
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        activeTab,
        searchQuery,
        selectedDuaCategory,
        contentRepo.hadiths,
        contentRepo.duas
    ) { tab, query, category, allHadiths, allDuas ->
        val q = query.trim().lowercase()

        val filteredHadiths = allHadiths.filter { h ->
            q.isEmpty() ||
                h.arabicText.contains(q) ||
                h.englishMeaning.lowercase().contains(q) ||
                h.bengaliMeaning.lowercase().contains(q) ||
                h.reference.lowercase().contains(q) ||
                h.narratorEn.lowercase().contains(q) ||
                h.categoryEn.lowercase().contains(q) ||
                h.categoryBn.lowercase().contains(q)
        }

        val filteredDuas = allDuas.filter { d ->
            val matchesCat = category == null || d.category == category
            val matchesQ = q.isEmpty() ||
                d.arabicText.contains(q) ||
                d.titleEn.lowercase().contains(q) ||
                d.titleBn.lowercase().contains(q) ||
                d.bengaliPronunciation.lowercase().contains(q) ||
                d.bengaliMeaning.lowercase().contains(q) ||
                d.englishMeaning.lowercase().contains(q) ||
                d.reference.lowercase().contains(q)
            matchesCat && matchesQ
        }

        LibraryUiState(
            activeTab = tab,
            searchQuery = query,
            selectedDuaCategory = category,
            hadiths = filteredHadiths,
            duas = filteredDuas,
            bookmarkedHadiths = allHadiths.filter { it.isBookmarked },
            bookmarkedDuas = allDuas.filter { it.isBookmarked }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun setTab(tab: LibraryTab) {
        activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDuaCategory(category: DuaCategory?) {
        selectedDuaCategory.value = category
    }

    fun toggleHadithBookmark(item: HadithItem) {
        viewModelScope.launch {
            contentRepo.toggleHadithBookmark(item.id, item.isBookmarked)
        }
    }

    fun toggleDuaBookmark(item: DuaItem) {
        viewModelScope.launch {
            contentRepo.toggleDuaBookmark(item.id, item.isBookmarked)
        }
    }
}
