package com.example.data.repository

import com.example.data.local.database.BookmarkDao
import com.example.data.local.database.BookmarkEntity
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.DuaDao
import com.example.data.local.database.HadithDao
import com.example.domain.model.DuaCategory
import com.example.domain.model.DuaItem
import com.example.domain.model.HadithItem
import com.example.domain.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ContentRepository(
    private val database: DeenDatabase,
    private val hadithDao: HadithDao,
    private val duaDao: DuaDao,
    private val bookmarkDao: BookmarkDao
) {
    suspend fun ensureSeeded() {
        DeenDatabase.seedInitialContentIfEmpty(database)
    }

    val hadiths: Flow<List<HadithItem>> = combine(
        hadithDao.observeAll(),
        bookmarkDao.observeAll()
    ) { entities, bookmarks ->
        val bookmarkedIds = bookmarks
            .filter { it.itemType == TYPE_HADITH }
            .map { it.itemId }
            .toSet()

        entities.map { e ->
            HadithItem(
                id = e.id,
                categoryEn = e.categoryEn,
                categoryBn = e.categoryBn,
                arabicText = e.arabicText,
                bengaliMeaning = e.bengaliMeaning,
                englishMeaning = e.englishMeaning,
                sourceBook = e.sourceBook,
                reference = e.reference,
                narratorEn = e.narratorEn,
                narratorBn = e.narratorBn,
                chapterEn = e.chapterEn,
                chapterBn = e.chapterBn,
                authenticityGrade = e.authenticityGrade,
                gradingScholar = e.gradingScholar,
                sourceUrl = e.sourceUrl,
                verificationStatus = VerificationStatus.fromKey(e.verificationStatus),
                lastVerifiedDate = e.lastVerifiedDate,
                verificationNotes = e.verificationNotes,
                isBookmarked = e.id in bookmarkedIds
            )
        }
    }

    val duas: Flow<List<DuaItem>> = combine(
        duaDao.observeAll(),
        bookmarkDao.observeAll()
    ) { entities, bookmarks ->
        val bookmarkedIds = bookmarks
            .filter { it.itemType == TYPE_DUA }
            .map { it.itemId }
            .toSet()

        entities.map { e ->
            DuaItem(
                id = e.id,
                category = DuaCategory.fromKey(e.categoryKey),
                titleEn = e.titleEn,
                titleBn = e.titleBn,
                arabicText = e.arabicText,
                bengaliPronunciation = e.bengaliPronunciation,
                bengaliMeaning = e.bengaliMeaning,
                englishMeaning = e.englishMeaning,
                sourceBook = e.sourceBook,
                reference = e.reference,
                authenticityGrade = e.authenticityGrade,
                repetitionCount = e.repetitionCount,
                contextEn = e.contextEn,
                contextBn = e.contextBn,
                sourceUrl = e.sourceUrl,
                verificationStatus = VerificationStatus.fromKey(e.verificationStatus),
                lastVerifiedDate = e.lastVerifiedDate,
                verificationNotes = e.verificationNotes,
                isBookmarked = e.id in bookmarkedIds
            )
        }
    }

    suspend fun toggleHadithBookmark(id: String, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            bookmarkDao.delete(id, TYPE_HADITH)
        } else {
            bookmarkDao.insert(BookmarkEntity(id, TYPE_HADITH, System.currentTimeMillis()))
        }
    }

    suspend fun toggleDuaBookmark(id: String, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            bookmarkDao.delete(id, TYPE_DUA)
        } else {
            bookmarkDao.insert(BookmarkEntity(id, TYPE_DUA, System.currentTimeMillis()))
        }
    }

    companion object {
        const val TYPE_HADITH = "HADITH"
        const val TYPE_DUA = "DUA"
    }
}
