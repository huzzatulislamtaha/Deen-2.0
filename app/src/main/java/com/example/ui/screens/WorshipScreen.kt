package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.data.local.database.TasbihPresetEntity
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerStatus
import com.example.ui.components.DeenCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.prayerDisplayName
import com.example.ui.theme.scaledArabicDisplayStyle
import com.example.ui.viewmodel.WorshipUiState
import com.example.ui.viewmodel.WorshipViewModel

@Composable
fun WorshipScreen(
    state: WorshipUiState,
    language: AppLanguage,
    onSelectSection: (Int) -> Unit,
    onIncrementTasbih: () -> Unit,
    onResetTasbih: () -> Unit,
    onSelectPreset: (TasbihPresetEntity) -> Unit,
    onSetTarget: (Int) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onAddCustomPreset: (String, String, String, Int) -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onSetPrayerStatus: (PrayerName, PrayerStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val selectedSection = state.selectedSection

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
                text = strings.worshipTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedSection == 0,
                    onClick = { onSelectSection(0) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("worship_tab_tasbih")
                ) {
                    Text(strings.tabTasbih)
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { onSelectSection(1) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("worship_tab_tracker")
                ) {
                    Text(strings.tabPrayerTracker)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSection == 0) {
                TasbihContent(
                    state = state,
                    language = language,
                    onIncrement = onIncrementTasbih,
                    onReset = onResetTasbih,
                    onSelectPreset = onSelectPreset,
                    onSetTarget = onSetTarget,
                    onToggleHaptic = onToggleHaptic,
                    onAddCustomPreset = onAddCustomPreset,
                    onDeleteCustomPreset = onDeleteCustomPreset
                )
            } else {
                PrayerTrackerContent(
                    state = state,
                    onShiftDate = onShiftDate,
                    onSetPrayerStatus = onSetPrayerStatus
                )
            }
        }
    }
}

@Composable
private fun TasbihContent(
    state: WorshipUiState,
    language: AppLanguage,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSelectPreset: (TasbihPresetEntity) -> Unit,
    onSetTarget: (Int) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onAddCustomPreset: (String, String, String, Int) -> Unit,
    onDeleteCustomPreset: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    var showTargetDialog by remember { mutableStateOf(false) }
    var showNewDhikrDialog by remember { mutableStateOf(false) }

    val progress = if (state.targetCount > 0) {
        (state.currentCount.toFloat() / state.targetCount.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "tasbih_progress")
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.presets.forEach { preset ->
                    val label = if (language == AppLanguage.BENGALI) preset.titleBn else preset.titleEn
                    FilterChip(
                        selected = preset.id == state.activePreset?.id,
                        onClick = { onSelectPreset(preset) },
                        label = { Text(label) },
                        trailingIcon = if (preset.isCustom) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = strings.delete,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onDeleteCustomPreset(preset.id) }
                                )
                            }
                        } else null
                    )
                }
                OutlinedButton(
                    onClick = { showNewDhikrDialog = true },
                    modifier = Modifier.testTag("tasbih_add_custom_button")
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addCustomDhikr)
                }
            }
        }

        item {
            DeenCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.activePreset?.let { preset ->
                        Text(
                            text = preset.arabicText,
                            style = scaledArabicDisplayStyle(),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (language == AppLanguage.BENGALI) preset.titleBn else preset.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (state.hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onIncrement()
                            }
                            .testTag("tasbih_tap_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.currentCount}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("tasbih_count_text")
                            )
                            Text(
                                text = "/ ${state.targetCount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.tapCounterHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(33, 99, 100).forEach { targetOpt ->
                            FilterChip(
                                selected = state.targetCount == targetOpt,
                                onClick = { onSetTarget(targetOpt) },
                                label = { Text("$targetOpt") },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        FilterChip(
                            selected = state.targetCount !in listOf(33, 99, 100),
                            onClick = { showTargetDialog = true },
                            label = { Text(strings.customTarget) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .testTag("tasbih_custom_target_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier.testTag("tasbih_reset_button")
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.reset)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.hapticFeedbackLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = state.hapticEnabled,
                                onCheckedChange = onToggleHaptic
                            )
                        }
                    }
                }
            }
        }

        item {
            DeenCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.dailyTotalLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${state.todayTasbihTotal}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text(
                text = strings.sessionHistoryTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.recentSessions.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.History,
                    title = strings.sessionHistoryTitle,
                    subtitle = strings.noSessionsYet
                )
            }
        } else {
            items(state.recentSessions, key = { it.id }) { session ->
                DeenCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) session.dhikrTitleBn else session.dhikrTitleEn,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = session.dateKey,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${session.count} / ${session.target}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showTargetDialog) {
        var targetInput by remember { mutableStateOf(state.targetCount.toString()) }
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text(strings.enterCustomTarget) },
            text = {
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val parsed = targetInput.toIntOrNull()?.coerceIn(1, 100000) ?: 33
                    onSetTarget(parsed)
                    showTargetDialog = false
                }) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showNewDhikrDialog) {
        var titleEn by remember { mutableStateOf("") }
        var titleBn by remember { mutableStateOf("") }
        var arabic by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("33") }
        AlertDialog(
            onDismissRequest = { showNewDhikrDialog = false },
            title = { Text(strings.addCustomDhikr) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text(strings.dhikrTitleEn) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = titleBn,
                        onValueChange = { titleBn = it },
                        label = { Text(strings.dhikrTitleBn) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = arabic,
                        onValueChange = { arabic = it },
                        label = { Text(strings.dhikrArabic) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.targetLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCustomPreset(
                            titleEn,
                            titleBn,
                            arabic,
                            targetText.toIntOrNull()?.coerceIn(1, 100000) ?: 33
                        )
                        showNewDhikrDialog = false
                    },
                    enabled = titleEn.isNotBlank() && arabic.isNotBlank()
                ) {
                    Text(strings.add)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDhikrDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun PrayerTrackerContent(
    state: WorshipUiState,
    onShiftDate: (Int) -> Unit,
    onSetPrayerStatus: (PrayerName, PrayerStatus) -> Unit
) {
    val strings = LocalAppStrings.current
    val isToday = state.selectedDateKey == WorshipViewModel.todayDateKey(state.effectiveTimeZoneId)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.manualTrackerNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        item {
            DeenCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShiftDate(-1) },
                        modifier = Modifier.testTag("tracker_prev_day_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = strings.back)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.trackerTodayTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.selectedDateKey,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { onShiftDate(1) },
                        enabled = !isToday,
                        modifier = Modifier.testTag("tracker_next_day_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }

        items(PrayerName.obligatoryPrayers, key = { it.key }) { prayer ->
            val currentStatus = state.dayPrayerLogs[prayer] ?: PrayerStatus.UNMARKED
            DeenCard(highlighted = currentStatus == PrayerStatus.COMPLETED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prayerDisplayName(prayer),
                            style = MaterialTheme.typography.titleMedium
                        )
                        val statusText = when (currentStatus) {
                            PrayerStatus.COMPLETED -> strings.trackerStatusCompleted
                            PrayerStatus.MISSED -> strings.trackerStatusMissed
                            PrayerStatus.UNMARKED -> strings.trackerStatusUnmarked
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (currentStatus) {
                                PrayerStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                PrayerStatus.MISSED -> MaterialTheme.colorScheme.error
                                PrayerStatus.UNMARKED -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currentStatus == PrayerStatus.COMPLETED,
                            onClick = {
                                val next = if (currentStatus == PrayerStatus.COMPLETED) {
                                    PrayerStatus.UNMARKED
                                } else {
                                    PrayerStatus.COMPLETED
                                }
                                onSetPrayerStatus(prayer, next)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(strings.markCompleted) },
                            modifier = Modifier.testTag("tracker_complete_${prayer.key}")
                        )
                        FilterChip(
                            selected = currentStatus == PrayerStatus.MISSED,
                            onClick = {
                                val next = if (currentStatus == PrayerStatus.MISSED) {
                                    PrayerStatus.UNMARKED
                                } else {
                                    PrayerStatus.MISSED
                                }
                                onSetPrayerStatus(prayer, next)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(strings.markMissed) },
                            modifier = Modifier.testTag("tracker_missed_${prayer.key}")
                        )
                    }
                }
            }
        }

        item {
            StatsCard(
                title = strings.weeklyStatsTitle,
                completed = state.weeklyStats.completedCount,
                missed = state.weeklyStats.missedCount,
                totalPossible = state.weeklyStats.totalPossible
            )
        }

        item {
            StatsCard(
                title = strings.monthlyStatsTitle,
                completed = state.monthlyStats.completedCount,
                missed = state.monthlyStats.missedCount,
                totalPossible = state.monthlyStats.totalPossible
            )
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    completed: Int,
    missed: Int,
    totalPossible: Int
) {
    val strings = LocalAppStrings.current
    val ratio = (completed.toFloat() / totalPossible.toFloat()).coerceIn(0f, 1f)
    DeenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$completed / $totalPossible",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(strings.completedCountFormat, completed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format(strings.missedCountFormat, missed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
