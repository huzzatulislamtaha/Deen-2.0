package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.data.local.database.DeenDatabase
import com.example.data.local.database.NoteCategoryEntity
import com.example.data.local.database.NoteEntity
import com.example.domain.model.NoteSortOrder
import com.example.domain.model.NotebookFilter
import com.example.ui.components.DeenCard
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.NotebookUiState
import com.example.ui.viewmodel.NotebookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    state: NotebookUiState,
    language: AppLanguage,
    storedPinHash: String,
    viewModel: NotebookViewModel,
    onBack: () -> Unit
) {
    if (state.editorDraft != null) {
        NoteEditorScreen(
            state = state,
            language = language,
            viewModel = viewModel
        )
        return
    }

    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (state.isPinLocked && storedPinHash.isNotBlank()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.notebookTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DeenCard(modifier = Modifier.widthIn(max = 420.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(strings.notebookPinPromptTitle, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        strings.notebookPinPromptBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all(Char::isDigit)) {
                                pinInput = it
                                pinError = false
                            }
                        },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notebook_pin_unlock_input")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.notebookWrongPin,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!viewModel.attemptUnlockPin(pinInput, storedPinHash)) {
                                pinError = true
                            } else {
                                pinInput = ""
                            }
                        },
                        enabled = pinInput.length == 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notebook_pin_unlock_button")
                    ) {
                        Text(strings.notebookUnlockAction)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.notebookTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("notebook_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCategoryManager = true },
                        modifier = Modifier.testTag("notebook_manage_categories_button")
                    ) {
                        Icon(Icons.Outlined.Category, contentDescription = strings.notebookManageCategories)
                    }
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("notebook_sort_button")
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            val options = listOf(
                                NoteSortOrder.UPDATED_DESC to strings.notebookSortUpdatedDesc,
                                NoteSortOrder.UPDATED_ASC to strings.notebookSortUpdatedAsc,
                                NoteSortOrder.CREATED_DESC to strings.notebookSortCreatedDesc,
                                NoteSortOrder.TITLE_ASC to strings.notebookSortTitleAsc
                            )
                            options.forEach { (order, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewNote() },
                modifier = Modifier.testTag("notebook_add_note_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.notebookAddNote)
            }
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
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.recoverableDraft != null) {
                    item {
                        val recoverable = state.recoverableDraft
                        DeenCard(highlighted = true) {
                            Text(strings.notebookRecoverDraftTitle, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(strings.notebookRecoverDraftBody, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.recoverUnsavedDraft(recoverable) }) {
                                    Text(strings.notebookRecoverAction)
                                }
                                OutlinedButton(onClick = { viewModel.discardRecoverableDraft() }) {
                                    Text(strings.notebookDiscardAction)
                                }
                            }
                        }
                    }
                }

                // Dashboard Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatChipCard(
                            label = strings.notebookTotalNotes,
                            value = state.allActiveNotesCount,
                            selected = state.selectedFilter == NotebookFilter.ALL && state.selectedCategoryId == null,
                            onClick = {
                                viewModel.setCategoryFilter(null)
                                viewModel.setFilter(NotebookFilter.ALL)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        StatChipCard(
                            label = strings.notebookPinned,
                            value = state.pinnedCount,
                            selected = state.selectedFilter == NotebookFilter.PINNED,
                            onClick = { viewModel.setFilter(NotebookFilter.PINNED) },
                            modifier = Modifier.weight(1f)
                        )
                        StatChipCard(
                            label = strings.notebookFavorites,
                            value = state.favoriteCount,
                            selected = state.selectedFilter == NotebookFilter.FAVORITES,
                            onClick = { viewModel.setFilter(NotebookFilter.FAVORITES) },
                            modifier = Modifier.weight(1f)
                        )
                        StatChipCard(
                            label = strings.notebookRecycleBin,
                            value = state.recycleBinCount,
                            selected = state.selectedFilter == NotebookFilter.RECYCLE_BIN,
                            onClick = { viewModel.setFilter(NotebookFilter.RECYCLE_BIN) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search bar
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text(strings.notebookSearchPlaceholder) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Outlined.Close, contentDescription = strings.clearSearch)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notebook_search_input")
                    )
                }

                // Category chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedCategoryId == null && state.selectedFilter != NotebookFilter.RECYCLE_BIN,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text(strings.allCategories) }
                        )
                        state.categories.forEach { cat ->
                            FilterChip(
                                selected = state.selectedCategoryId == cat.id,
                                onClick = { viewModel.setCategoryFilter(cat.id) },
                                label = {
                                    Text(if (language == AppLanguage.BENGALI) cat.nameBn else cat.nameEn)
                                },
                                modifier = Modifier.testTag("notebook_cat_chip_${cat.id}")
                            )
                        }
                    }
                }

                if (state.selectedFilter == NotebookFilter.RECYCLE_BIN && state.deletedNotes.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.emptyRecycleBin() },
                                modifier = Modifier.testTag("notebook_empty_bin_button")
                            ) {
                                Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.notebookEmptyBin)
                            }
                        }
                    }
                }

                if (state.notes.isEmpty()) {
                    item {
                        val isBin = state.selectedFilter == NotebookFilter.RECYCLE_BIN
                        EmptyStateView(
                            icon = Icons.Outlined.EditNote,
                            title = if (isBin) strings.notebookRecycleEmptyTitle else strings.notebookEmptyTitle,
                            subtitle = if (isBin) strings.notebookRecycleEmptySubtitle else strings.notebookEmptySubtitle
                        )
                    }
                } else {
                    items(state.notes, key = { it.id }) { note ->
                        val cat = state.categories.firstOrNull { it.id == note.categoryId }
                        val catName = if (cat != null) {
                            if (language == AppLanguage.BENGALI) cat.nameBn else cat.nameEn
                        } else ""
                        NoteListCard(
                            note = note,
                            categoryName = catName,
                            timeZoneId = state.effectiveTimeZoneId,
                            isRecycleBin = state.selectedFilter == NotebookFilter.RECYCLE_BIN,
                            onOpen = {
                                if (state.selectedFilter != NotebookFilter.RECYCLE_BIN) {
                                    viewModel.openExistingNote(note)
                                }
                            },
                            onTogglePin = { viewModel.togglePinned(note) },
                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                            onSoftDelete = { viewModel.softDeleteNote(note.id) },
                            onRestore = { viewModel.restoreNote(note.id) },
                            onPermanentDelete = { viewModel.permanentlyDeleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCategoryManager) {
        CategoryManagerDialog(
            categories = state.categories,
            language = language,
            onCreateCategory = viewModel::createCustomCategory,
            onRenameCategory = viewModel::renameCustomCategory,
            onDeleteCategory = viewModel::deleteCustomCategory,
            onDismiss = { showCategoryManager = false }
        )
    }
}

@Composable
private fun StatChipCard(
    label: String,
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NoteListCard(
    note: NoteEntity,
    categoryName: String,
    timeZoneId: String,
    isRecycleBin: Boolean,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSoftDelete: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    DeenCard(
        highlighted = note.isPinned && !isRecycleBin,
        modifier = Modifier
            .clickable(onClick = onOpen)
            .testTag("note_card_${note.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (categoryName.isNotBlank()) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = note.title.ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isRecycleBin) {
                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.testTag("note_pin_${note.id}")) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = strings.notebookPinned,
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("note_fav_${note.id}")) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = strings.notebookFavorites,
                            tint = if (note.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onSoftDelete, modifier = Modifier.testTag("note_delete_${note.id}")) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = strings.delete,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        if (note.description.isNotBlank()) {
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (note.content.isNotBlank()) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.notebookUpdatedLabel}: ${NotebookViewModel.formatTimestamp(note.updatedAt, timeZoneId)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note.tags.isNotBlank()) {
                Text(
                    text = note.tags,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isRecycleBin) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("note_restore_${note.id}")
                ) {
                    Icon(Icons.Outlined.RestoreFromTrash, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.notebookRestore)
                }
                OutlinedButton(
                    onClick = onPermanentDelete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("note_hard_delete_${note.id}")
                ) {
                    Text(strings.notebookDeleteForever, color = MaterialTheme.colorScheme.error, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CategoryManagerDialog(
    categories: List<NoteCategoryEntity>,
    language: AppLanguage,
    onCreateCategory: (String, String) -> Unit,
    onRenameCategory: (NoteCategoryEntity, String, String) -> Unit,
    onDeleteCategory: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var newNameEn by remember { mutableStateOf("") }
    var newNameBn by remember { mutableStateOf("") }
    var renamingCategory by remember { mutableStateOf<NoteCategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<NoteCategoryEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notebookManageCategories) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newNameEn,
                    onValueChange = { newNameEn = it },
                    label = { Text(strings.notebookCategoryNameEn) },
                    singleLine = true,
                    modifier = Modifier.testTag("new_category_en_input")
                )
                OutlinedTextField(
                    value = newNameBn,
                    onValueChange = { newNameBn = it },
                    label = { Text(strings.notebookCategoryNameBn) },
                    singleLine = true
                )
                Button(
                    onClick = {
                        onCreateCategory(newNameEn, newNameBn)
                        newNameEn = ""
                        newNameBn = ""
                    },
                    enabled = newNameEn.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_category_submit")
                ) {
                    Text(strings.notebookCreateCategory)
                }
                Spacer(modifier = Modifier.height(6.dp))
                categories.filter { it.isCustom }.forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) cat.nameBn else cat.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { renamingCategory = cat }) {
                            Icon(Icons.Outlined.Edit, contentDescription = strings.edit)
                        }
                        IconButton(
                            onClick = { deletingCategory = cat },
                            modifier = Modifier.testTag("delete_category_${cat.id}")
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = strings.delete)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )

    renamingCategory?.let { cat ->
        var editEn by remember(cat.id) { mutableStateOf(cat.nameEn) }
        var editBn by remember(cat.id) { mutableStateOf(cat.nameBn) }
        AlertDialog(
            onDismissRequest = { renamingCategory = null },
            title = { Text(strings.notebookRenameCategory) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editEn, onValueChange = { editEn = it }, label = { Text(strings.notebookCategoryNameEn) })
                    OutlinedTextField(value = editBn, onValueChange = { editBn = it }, label = { Text(strings.notebookCategoryNameBn) })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameCategory(cat, editEn, editBn)
                        renamingCategory = null
                    },
                    enabled = editEn.isNotBlank()
                ) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { renamingCategory = null }) { Text(strings.cancel) }
            }
        )
    }

    deletingCategory?.let { cat ->
        var targetId by remember(cat.id) { mutableStateOf(DeenDatabase.GENERAL_CATEGORY_ID) }
        val targetChoices = categories.filter { it.id != cat.id }
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text(strings.notebookDeleteCategoryTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.notebookDeleteCategoryBody, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    targetChoices.take(6).forEach { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetId = target.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = targetId == target.id, onClick = { targetId = target.id })
                            Text(
                                text = if (language == AppLanguage.BENGALI) target.nameBn else target.nameEn,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(cat.id, targetId)
                        deletingCategory = null
                    },
                    modifier = Modifier.testTag("confirm_delete_category_btn")
                ) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text(strings.cancel) }
            }
        )
    }
}
