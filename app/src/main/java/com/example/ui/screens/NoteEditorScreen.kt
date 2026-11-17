package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.domain.model.NoteSaveStatus
import com.example.ui.components.DeenCard
import com.example.ui.theme.LocalDeenFontConfig
import com.example.ui.theme.scaledArabicCompactStyle
import com.example.ui.theme.scaledBengaliBodyStyle
import com.example.ui.theme.scaledEnglishBodyStyle
import com.example.ui.viewmodel.NotebookUiState
import com.example.ui.viewmodel.NotebookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NotebookUiState,
    language: AppLanguage,
    viewModel: NotebookViewModel
) {
    val draft = state.editorDraft ?: return
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val fontConfig = LocalDeenFontConfig.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isRtlArabicInput by remember(draft.id) { mutableStateOf(false) }
    var isPreviewMode by remember(draft.id) { mutableStateOf(false) }
    var showReferencesSection by remember(draft.id) {
        mutableStateOf(
            draft.hadithCollection.isNotBlank() ||
                draft.hadithNumber.isNotBlank() ||
                draft.duaSource.isNotBlank() ||
                draft.islamicBookName.isNotBlank() ||
                draft.websiteUrl.isNotBlank()
        )
    }

    // Preserve content immediately on app backgrounding / pause
    DisposableEffect(lifecycleOwner, draft.id) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.flushSaveIfEditing()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler { viewModel.closeEditorWithoutSavingIfEmpty() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val statusText = when (state.saveStatus) {
                        NoteSaveStatus.SAVING -> strings.notebookAutosaveSaving
                        NoteSaveStatus.SAVED -> state.lastSavedAtMillis?.let {
                            "${strings.notebookAutosaveSaved} • ${NotebookViewModel.formatShortTime(it, state.effectiveTimeZoneId)}"
                        } ?: strings.notebookAutosaveSaved
                        NoteSaveStatus.ERROR -> strings.notebookAutosaveError
                        NoteSaveStatus.IDLE -> strings.notebookTitle
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (state.saveStatus == NoteSaveStatus.ERROR) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.testTag("note_autosave_status")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeEditorWithoutSavingIfEmpty() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateDraft { it.copy(isPinned = !it.isPinned) } },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (draft.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = strings.notebookPinned,
                            tint = if (draft.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.updateDraft { it.copy(isFavorite = !it.isFavorite) } },
                        modifier = Modifier.testTag("editor_fav_button")
                    ) {
                        Icon(
                            imageVector = if (draft.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = strings.notebookFavorites,
                            tint = if (draft.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                if (draft.title.isNotBlank()) appendLine(draft.title)
                                if (draft.description.isNotBlank()) appendLine(draft.description)
                                appendLine()
                                appendLine(draft.content)
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, strings.share))
                        }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = strings.share)
                    }
                    if (!draft.isNew) {
                        IconButton(
                            onClick = { viewModel.softDeleteNote(draft.id) },
                            modifier = Modifier.testTag("editor_delete_button")
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.saveAndCloseEditor() },
                        modifier = Modifier.testTag("editor_save_done_button")
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = strings.save, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .testTag("editor_lazy_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.errorMessage != null) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Category selector chips
                item {
                    Text(
                        text = strings.notebookCategoryLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.categories.forEach { cat ->
                            FilterChip(
                                selected = draft.categoryId == cat.id,
                                onClick = { viewModel.updateDraft { it.copy(categoryId = cat.id) } },
                                label = {
                                    Text(if (language == AppLanguage.BENGALI) cat.nameBn else cat.nameEn)
                                }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { v -> viewModel.updateDraft { it.copy(title = v) } },
                        label = { Text(strings.notebookNoteTitleLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { v -> viewModel.updateDraft { it.copy(description = v) } },
                        label = { Text(strings.notebookNoteDescriptionLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_description_input")
                    )
                }

                // Formatting & RTL Arabic Toolbar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = isRtlArabicInput,
                            onClick = { isRtlArabicInput = !isRtlArabicInput },
                            label = { Text(strings.notebookRtlArabicToggle) },
                            modifier = Modifier.testTag("editor_rtl_toggle")
                        )
                        FilterChip(
                            selected = isPreviewMode,
                            onClick = { isPreviewMode = !isPreviewMode },
                            leadingIcon = { Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = {
                                Text(if (isPreviewMode) strings.notebookEditToggle else strings.notebookPreviewToggle)
                            },
                            modifier = Modifier.testTag("editor_preview_toggle")
                        )
                        OutlinedButton(
                            onClick = { viewModel.updateDraft { it.copy(content = "${it.content}**bold**") } }
                        ) {
                            Icon(Icons.Outlined.FormatBold, contentDescription = "Bold", modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = { viewModel.updateDraft { it.copy(content = "${it.content}*italic*") } }
                        ) {
                            Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = {
                                val prefix = if (draft.content.isEmpty() || draft.content.endsWith("\n")) "• " else "\n• "
                                viewModel.updateDraft { it.copy(content = it.content + prefix) }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Bullet list", modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = {
                                val prefix = if (draft.content.isEmpty() || draft.content.endsWith("\n")) "1. " else "\n1. "
                                viewModel.updateDraft { it.copy(content = it.content + prefix) }
                            }
                        ) {
                            Icon(Icons.Outlined.FormatListNumbered, contentDescription = "Numbered list", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Content Editor or Formatted Rich Preview
                item {
                    val contentTextStyle = when {
                        isRtlArabicInput -> scaledArabicCompactStyle(fontConfig)
                        language == AppLanguage.BENGALI -> scaledBengaliBodyStyle(fontConfig)
                        else -> scaledEnglishBodyStyle(fontConfig)
                    }

                    if (isPreviewMode) {
                        DeenCard {
                            val paragraphs = draft.content.split("\n")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                paragraphs.forEach { line ->
                                    val containsArabic = line.any { ch -> ch in '\u0600'..'\u06FF' }
                                    Text(
                                        text = parseInlineFormatting(line),
                                        style = if (containsArabic || isRtlArabicInput) {
                                            scaledArabicCompactStyle(fontConfig).copy(textDirection = TextDirection.Rtl)
                                        } else {
                                            contentTextStyle
                                        },
                                        textAlign = if (containsArabic || isRtlArabicInput) TextAlign.End else TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = draft.content,
                            onValueChange = { v -> viewModel.updateDraft { it.copy(content = v) } },
                            label = { Text(strings.notebookNoteContentLabel) },
                            textStyle = contentTextStyle,
                            minLines = 8,
                            maxLines = 24,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_content_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = draft.tags,
                        onValueChange = { v -> viewModel.updateDraft { it.copy(tags = v) } },
                        label = { Text(strings.notebookTagsLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_tags_input")
                    )
                }

                // Optional Islamic Reference Fields
                item {
                    DeenCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReferencesSection = !showReferencesSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.notebookReferencesSection,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (showReferencesSection) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null
                            )
                        }
                        if (showReferencesSection) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = strings.notebookReferenceDisclaimer,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = draft.hadithCollection,
                                    onValueChange = { v -> viewModel.updateDraft { it.copy(hadithCollection = v) } },
                                    label = { Text(strings.notebookHadithCollection) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = draft.hadithNumber,
                                    onValueChange = { v -> viewModel.updateDraft { it.copy(hadithNumber = v) } },
                                    label = { Text(strings.notebookHadithNumber) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = draft.duaSource,
                                    onValueChange = { v -> viewModel.updateDraft { it.copy(duaSource = v) } },
                                    label = { Text(strings.notebookDuaSource) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = draft.islamicBookName,
                                    onValueChange = { v -> viewModel.updateDraft { it.copy(islamicBookName = v) } },
                                    label = { Text(strings.notebookIslamicBook) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = draft.pageNumber,
                                        onValueChange = { v -> viewModel.updateDraft { it.copy(pageNumber = v) } },
                                        label = { Text(strings.notebookPageNumber) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = draft.websiteUrl,
                                        onValueChange = { v -> viewModel.updateDraft { it.copy(websiteUrl = v) } },
                                        label = { Text(strings.notebookWebsiteUrl) },
                                        singleLine = true,
                                        modifier = Modifier.weight(2f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Timestamps (displayed in user's selected timezone from UTC epoch millis)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.notebookCreatedLabel}: ${NotebookViewModel.formatTimestamp(draft.createdAt, state.effectiveTimeZoneId)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${strings.notebookUpdatedLabel}: ${NotebookViewModel.formatTimestamp(draft.updatedAt, state.effectiveTimeZoneId)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun parseInlineFormatting(line: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("**", i) -> {
                val end = line.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(line.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(line[i])
                    i++
                }
            }
            line.startsWith("*", i) -> {
                val end = line.indexOf("*", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(line.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(line[i])
                    i++
                }
            }
            else -> {
                append(line[i])
                i++
            }
        }
    }
}
