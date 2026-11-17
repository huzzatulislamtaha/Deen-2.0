package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.domain.model.DuaCategory
import com.example.domain.model.DuaItem
import com.example.domain.model.HadithItem
import com.example.ui.components.ArabicTextBlock
import com.example.ui.components.DeenCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.VerificationBadge
import com.example.ui.components.bilingualStyle
import com.example.ui.viewmodel.LibraryTab
import com.example.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    language: AppLanguage,
    onSelectTab: (LibraryTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectDuaCategory: (DuaCategory?) -> Unit,
    onToggleHadithBookmark: (HadithItem) -> Unit,
    onToggleDuaBookmark: (DuaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var selectedHadith by remember { mutableStateOf<HadithItem?>(null) }
    var selectedDua by remember { mutableStateOf<DuaItem?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = strings.libraryTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf(
                    LibraryTab.HADITH to strings.tabHadith,
                    LibraryTab.DUA to strings.tabDuaAzkar,
                    LibraryTab.BOOKMARKS to strings.tabBookmarks
                )
                tabs.forEachIndexed { idx, (tab, label) ->
                    SegmentedButton(
                        selected = state.activeTab == tab,
                        onClick = { onSelectTab(tab) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
                        modifier = Modifier.testTag("library_tab_${tab.name.lowercase()}")
                    ) {
                        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = strings.clearSearch)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )

            if (state.activeTab == LibraryTab.DUA) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedDuaCategory == null,
                        onClick = { onSelectDuaCategory(null) },
                        label = { Text(strings.allCategories) }
                    )
                    DuaCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = state.selectedDuaCategory == cat,
                            onClick = { onSelectDuaCategory(cat) },
                            label = { Text(duaCategoryLabel(cat)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.activeTab) {
                    LibraryTab.HADITH -> {
                        if (state.hadiths.isEmpty()) {
                            item {
                                EmptyStateView(
                                    icon = Icons.Outlined.LibraryBooks,
                                    title = strings.noResultsTitle,
                                    subtitle = strings.noResultsSubtitle
                                )
                            }
                        } else {
                            items(state.hadiths, key = { it.id }) { hadith ->
                                HadithCardItem(
                                    hadith = hadith,
                                    language = language,
                                    onClick = { selectedHadith = hadith },
                                    onToggleBookmark = { onToggleHadithBookmark(hadith) }
                                )
                            }
                        }
                    }

                    LibraryTab.DUA -> {
                        if (state.duas.isEmpty()) {
                            item {
                                EmptyStateView(
                                    icon = Icons.Outlined.LibraryBooks,
                                    title = strings.noResultsTitle,
                                    subtitle = strings.noResultsSubtitle
                                )
                            }
                        } else {
                            items(state.duas, key = { it.id }) { dua ->
                                DuaCardItem(
                                    dua = dua,
                                    language = language,
                                    onClick = { selectedDua = dua },
                                    onToggleBookmark = { onToggleDuaBookmark(dua) }
                                )
                            }
                        }
                    }

                    LibraryTab.BOOKMARKS -> {
                        if (state.bookmarkedHadiths.isEmpty() && state.bookmarkedDuas.isEmpty()) {
                            item {
                                EmptyStateView(
                                    icon = Icons.Outlined.BookmarkBorder,
                                    title = strings.noBookmarksTitle,
                                    subtitle = strings.noBookmarksSubtitle
                                )
                            }
                        } else {
                            items(state.bookmarkedHadiths, key = { "bm_${it.id}" }) { hadith ->
                                HadithCardItem(
                                    hadith = hadith,
                                    language = language,
                                    onClick = { selectedHadith = hadith },
                                    onToggleBookmark = { onToggleHadithBookmark(hadith) }
                                )
                            }
                            items(state.bookmarkedDuas, key = { "bm_${it.id}" }) { dua ->
                                DuaCardItem(
                                    dua = dua,
                                    language = language,
                                    onClick = { selectedDua = dua },
                                    onToggleBookmark = { onToggleDuaBookmark(dua) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedHadith?.let { item ->
        val current = state.hadiths.firstOrNull { it.id == item.id } ?: item
        HadithDetailDialog(
            hadith = current,
            language = language,
            onToggleBookmark = { onToggleHadithBookmark(current) },
            onDismiss = { selectedHadith = null }
        )
    }

    selectedDua?.let { item ->
        val current = state.duas.firstOrNull { it.id == item.id } ?: item
        DuaDetailDialog(
            dua = current,
            language = language,
            onToggleBookmark = { onToggleDuaBookmark(current) },
            onDismiss = { selectedDua = null }
        )
    }
}

@Composable
private fun HadithCardItem(
    hadith: HadithItem,
    language: AppLanguage,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val strings = LocalAppStrings.current
    val category = if (language == AppLanguage.BENGALI) hadith.categoryBn else hadith.categoryEn
    val translation = if (language == AppLanguage.BENGALI) hadith.bengaliMeaning else hadith.englishMeaning

    DeenCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("hadith_card_${hadith.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.testTag("bookmark_hadith_${hadith.id}")
            ) {
                Icon(
                    imageVector = if (hadith.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (hadith.isBookmarked) strings.bookmarkRemove else strings.bookmarkAdd,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ArabicTextBlock(arabic = hadith.arabicText)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = translation,
            style = bilingualStyle(language),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hadith.reference,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            VerificationBadge(
                status = hadith.verificationStatus,
                grade = hadith.authenticityGrade
            )
        }
    }
}

@Composable
private fun DuaCardItem(
    dua: DuaItem,
    language: AppLanguage,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val strings = LocalAppStrings.current
    val title = if (language == AppLanguage.BENGALI) dua.titleBn else dua.titleEn
    val meaning = if (language == AppLanguage.BENGALI) dua.bengaliMeaning else dua.englishMeaning

    DeenCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("dua_card_${dua.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = duaCategoryLabel(dua.category),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.testTag("bookmark_dua_${dua.id}")
            ) {
                Icon(
                    imageVector = if (dua.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (dua.isBookmarked) strings.bookmarkRemove else strings.bookmarkAdd,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ArabicTextBlock(arabic = dua.arabicText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = meaning,
            style = bilingualStyle(language),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${dua.reference} • ${String.format(strings.repeatCountFormat, dua.repetitionCount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            VerificationBadge(
                status = dua.verificationStatus,
                grade = dua.authenticityGrade
            )
        }
    }
}

@Composable
private fun HadithDetailDialog(
    hadith: HadithItem,
    language: AppLanguage,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BENGALI) hadith.chapterBn else hadith.chapterEn,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VerificationBadge(
                    status = hadith.verificationStatus,
                    grade = hadith.authenticityGrade
                )
                ArabicTextBlock(arabic = hadith.arabicText)

                Text(
                    text = strings.narratorLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (language == AppLanguage.BENGALI) hadith.narratorBn else hadith.narratorEn,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = strings.bengaliMeaningLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = hadith.bengaliMeaning, style = bilingualStyle(AppLanguage.BENGALI))

                Text(
                    text = strings.englishMeaningLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = hadith.englishMeaning, style = MaterialTheme.typography.bodyMedium)

                Text(
                    text = "${strings.referenceLabel}: ${hadith.reference}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${strings.gradedByLabel}: ${hadith.gradingScholar}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${strings.lastVerifiedLabel}: ${hadith.lastVerifiedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { uriHandler.openUri(hadith.sourceUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${strings.sourceUrlLabel}: ${hadith.sourceBook}")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (hadith.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (hadith.isBookmarked) strings.bookmarkRemove else strings.bookmarkAdd
                    )
                }
                IconButton(onClick = {
                    val shareBody = buildString {
                        appendLine(hadith.arabicText)
                        appendLine()
                        appendLine(if (language == AppLanguage.BENGALI) hadith.bengaliMeaning else hadith.englishMeaning)
                        appendLine()
                        appendLine("${hadith.reference} (${hadith.authenticityGrade})")
                        append(hadith.sourceUrl)
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(intent, strings.share))
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = strings.share)
                }
                TextButton(onClick = onDismiss) { Text(strings.close) }
            }
        }
    )
}

@Composable
private fun DuaDetailDialog(
    dua: DuaItem,
    language: AppLanguage,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var recitedCount by remember(dua.id) { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BENGALI) dua.titleBn else dua.titleEn,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VerificationBadge(
                    status = dua.verificationStatus,
                    grade = dua.authenticityGrade
                )
                ArabicTextBlock(arabic = dua.arabicText)

                // Interactive repetition counter for Dua/Azkar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (recitedCount < dua.repetitionCount) recitedCount++
                        },
                        modifier = Modifier.testTag("dua_recite_button")
                    ) {
                        val label = if (recitedCount >= dua.repetitionCount) {
                            "${strings.reciteCompleted} ($recitedCount/${dua.repetitionCount})"
                        } else {
                            "${strings.tapToRecite} ($recitedCount/${dua.repetitionCount})"
                        }
                        Text(label)
                    }
                    IconButton(onClick = { recitedCount = 0 }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = strings.reset)
                    }
                }

                Text(
                    text = strings.pronunciationLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = dua.bengaliPronunciation, style = bilingualStyle(AppLanguage.BENGALI))

                Text(
                    text = strings.bengaliMeaningLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = dua.bengaliMeaning, style = bilingualStyle(AppLanguage.BENGALI))

                Text(
                    text = strings.englishMeaningLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = dua.englishMeaning, style = MaterialTheme.typography.bodyMedium)

                Text(
                    text = strings.contextLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (language == AppLanguage.BENGALI) dua.contextBn else dua.contextEn,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${strings.referenceLabel}: ${dua.reference}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = { uriHandler.openUri(dua.sourceUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${strings.sourceUrlLabel}: ${dua.sourceBook}")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (dua.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (dua.isBookmarked) strings.bookmarkRemove else strings.bookmarkAdd
                    )
                }
                IconButton(onClick = {
                    val shareBody = buildString {
                        appendLine(dua.arabicText)
                        appendLine()
                        appendLine(dua.bengaliPronunciation)
                        appendLine()
                        appendLine(if (language == AppLanguage.BENGALI) dua.bengaliMeaning else dua.englishMeaning)
                        appendLine()
                        appendLine("${dua.reference} (${dua.authenticityGrade})")
                        append(dua.sourceUrl)
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(intent, strings.share))
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = strings.share)
                }
                TextButton(onClick = onDismiss) { Text(strings.close) }
            }
        }
    )
}

@Composable
private fun duaCategoryLabel(category: DuaCategory): String {
    val strings = LocalAppStrings.current
    return when (category) {
        DuaCategory.MORNING_AZKAR -> strings.catMorningAzkar
        DuaCategory.EVENING_AZKAR -> strings.catEveningAzkar
        DuaCategory.AFTER_PRAYER -> strings.catAfterPrayer
        DuaCategory.DAILY_DUA -> strings.catDailyDua
        DuaCategory.SLEEP_DUA -> strings.catSleepDua
        DuaCategory.TRAVEL_DUA -> strings.catTravelDua
    }
}
