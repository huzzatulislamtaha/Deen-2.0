package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.data.local.database.NoteCategoryEntity
import com.example.data.repository.TimezoneRepository
import com.example.domain.model.AzanSoundOption
import com.example.domain.model.CalculationMethod
import com.example.domain.model.FontScalePreset
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.Madhab
import com.example.domain.model.ThemeMode
import com.example.domain.model.TimezoneMode
import com.example.ui.components.DeenCard
import com.example.ui.viewmodel.MainUiState
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: MainUiState,
    notebookCategories: List<NoteCategoryEntity>,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onOpenLocationSelection: () -> Unit,
    onOpenTimezoneSelection: () -> Unit,
    onOpenFontSettings: () -> Unit,
    onSelectMethod: (CalculationMethod) -> Unit,
    onSelectMadhab: (Madhab) -> Unit,
    onSelectHighLat: (HighLatitudeRule) -> Unit,
    onSelectSound: (AzanSoundOption) -> Unit,
    onSelectNotebookDefaultCategory: (String) -> Unit,
    onToggleNotebookAutosave: (Boolean) -> Unit,
    onSetNotebookPin: (String) -> Unit,
    onClearNotebookPin: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val strings = LocalAppStrings.current
    val lang = state.prefs.language
    var showMethodPicker by remember { mutableStateOf(false) }
    var showHighLatPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    val effectiveTzId = state.prefs.effectiveTimeZoneId
    val tzOffset = remember(effectiveTzId) {
        val tz = TimeZone.getTimeZone(effectiveTzId)
        TimezoneRepository.formatUtcOffset(tz.getOffset(System.currentTimeMillis()))
    }

    val loc = state.prefs.location
    val locationDisplay = if (lang == AppLanguage.BENGALI) {
        buildString {
            if (loc.upazilaNameBn.isNotBlank()) {
                append(loc.upazilaNameBn)
                append(", ")
            }
            append(loc.nameBn)
            if (loc.divisionNameBn.isNotBlank()) {
                append(", ")
                append(loc.divisionNameBn)
            } else {
                append(", ")
                append(loc.countryBn)
            }
        }
    } else {
        buildString {
            if (loc.upazilaNameEn.isNotBlank()) {
                append(loc.upazilaNameEn)
                append(", ")
            }
            append(loc.nameEn)
            if (loc.divisionNameEn.isNotBlank()) {
                append(", ")
                append(loc.divisionNameEn)
            } else {
                append(", ")
                append(loc.countryEn)
            }
        }
    }

    val fontPresetLabel = when (state.prefs.fontConfig.appScalePreset) {
        FontScalePreset.SMALL -> strings.fontSizeSmall
        FontScalePreset.MEDIUM -> strings.fontSizeMedium
        FontScalePreset.LARGE -> strings.fontSizeLarge
        FontScalePreset.EXTRA_LARGE -> strings.fontSizeExtraLarge
    }

    val activeDefaultCategory = notebookCategories.firstOrNull {
        it.id == state.prefs.notebookDefaultCategoryId
    }
    val defaultCatLabel = if (activeDefaultCategory != null) {
        if (lang == AppLanguage.BENGALI) activeDefaultCategory.nameBn else activeDefaultCategory.nameEn
    } else {
        state.prefs.notebookDefaultCategoryId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = strings.back
                        )
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
                    .testTag("settings_lazy_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Location & Time
                item {
                    SectionHeading(strings.locationTimeSection)
                }

                item {
                    DeenCard {
                        SettingRow(
                            title = strings.locationLabel,
                            value = locationDisplay,
                            onClick = onOpenLocationSelection,
                            testTag = "settings_location_row",
                            showChevron = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val tzModeSuffix = if (state.prefs.timezoneMode == TimezoneMode.AUTOMATIC) {
                            strings.timezoneModeAuto
                        } else {
                            strings.timezoneModeManual
                        }
                        SettingRow(
                            title = strings.timezoneLabel,
                            value = "$effectiveTzId ($tzOffset) • $tzModeSuffix",
                            onClick = onOpenTimezoneSelection,
                            testTag = "settings_timezone_row",
                            showChevron = true
                        )
                    }
                }

                // 2. Appearance & Typography
                item {
                    SectionHeading(strings.appearanceSection)
                }

                item {
                    DeenCard {
                        Text(
                            text = strings.languageLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            AppLanguage.entries.forEachIndexed { idx, appLang ->
                                SegmentedButton(
                                    selected = lang == appLang,
                                    onClick = { onLanguageChange(appLang) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = idx,
                                        count = AppLanguage.entries.size
                                    ),
                                    modifier = Modifier.testTag("lang_option_${appLang.code}")
                                ) {
                                    Text(appLang.nativeLabel)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = strings.themeLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val themeOptions = listOf(
                            ThemeMode.SYSTEM to strings.themeSystem,
                            ThemeMode.LIGHT to strings.themeLight,
                            ThemeMode.DARK to strings.themeDark
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            themeOptions.forEachIndexed { idx, (mode, label) ->
                                SegmentedButton(
                                    selected = state.prefs.themeMode == mode,
                                    onClick = { onThemeChange(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = idx,
                                        count = themeOptions.size
                                    ),
                                    modifier = Modifier.testTag("theme_option_${mode.key}")
                                ) {
                                    Text(label)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingRow(
                            title = strings.fontSettingsLabel,
                            value = "$fontPresetLabel • ${state.prefs.fontConfig.latinFontChoice.displayName}",
                            onClick = onOpenFontSettings,
                            testTag = "settings_font_row",
                            showChevron = true
                        )
                    }
                }

                // 3. Personalization (Notebook & Worship)
                item {
                    SectionHeading(strings.personalizationSection)
                }

                item {
                    DeenCard {
                        SettingRow(
                            title = strings.notebookDefaultCatLabel,
                            value = defaultCatLabel,
                            onClick = { showCategoryPicker = true },
                            testTag = "settings_notebook_category_row",
                            showChevron = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.notebookAutosavePrefLabel,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = state.prefs.notebookAutosaveEnabled,
                                onCheckedChange = onToggleNotebookAutosave,
                                modifier = Modifier.testTag("settings_notebook_autosave_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.notebookPinPrefLabel,
                            value = if (state.prefs.isNotebookLockEnabled) {
                                strings.notebookPinSet
                            } else {
                                strings.notebookPinDisabled
                            },
                            onClick = { showPinDialog = true },
                            testTag = "settings_notebook_pin_row",
                            showChevron = true
                        )
                    }
                }

                // 4. Prayer & Calendar Calculation
                item {
                    SectionHeading(strings.prayerSettingsSection)
                }

                item {
                    DeenCard {
                        SettingRow(
                            title = strings.calculationMethodLabel,
                            value = if (lang == AppLanguage.BENGALI) {
                                state.prefs.calculationMethod.titleBn
                            } else {
                                state.prefs.calculationMethod.titleEn
                            },
                            onClick = { showMethodPicker = true },
                            testTag = "settings_method_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.madhabLabel,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val madhabs = listOf(
                                Madhab.STANDARD to strings.madhabStandard,
                                Madhab.HANAFI to strings.madhabHanafi
                            )
                            madhabs.forEachIndexed { idx, (m, label) ->
                                SegmentedButton(
                                    selected = state.prefs.madhab == m,
                                    onClick = { onSelectMadhab(m) },
                                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                                    modifier = Modifier.testTag("madhab_${m.key}")
                                ) {
                                    Text(label, maxLines = 1)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.highLatitudeLabel,
                            value = if (lang == AppLanguage.BENGALI) {
                                state.prefs.highLatitudeRule.titleBn
                            } else {
                                state.prefs.highLatitudeRule.titleEn
                            },
                            onClick = { showHighLatPicker = true },
                            testTag = "settings_high_lat_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.azanSoundLabel,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val sounds = listOf(
                            AzanSoundOption.DEFAULT_NOTIFICATION to strings.soundDefault,
                            AzanSoundOption.GENTLE_CHIME to strings.soundChime,
                            AzanSoundOption.SILENT to strings.soundSilent
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            sounds.forEachIndexed { idx, (opt, label) ->
                                SegmentedButton(
                                    selected = state.prefs.notificationPrefs.soundOption == opt,
                                    onClick = { onSelectSound(opt) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = idx,
                                        count = sounds.size
                                    )
                                ) {
                                    Text(label, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // 5. Privacy & Trust
                item {
                    SectionHeading(strings.privacySection)
                }

                item {
                    DeenCard {
                        SettingRow(
                            title = strings.notebookPrivacyInfoTitle,
                            value = strings.localStorageInfoTitle,
                            onClick = {
                                infoDialogContent = strings.notebookPrivacyInfoTitle to
                                    strings.notebookPrivacyInfoBody
                            },
                            testTag = "settings_notebook_privacy_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.localStorageInfoTitle,
                            value = strings.privacyPolicyTitle,
                            onClick = {
                                infoDialogContent = strings.localStorageInfoTitle to
                                    strings.localStorageInfoBody
                            },
                            testTag = "settings_local_storage_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.contentVerificationPolicyTitle,
                            value = strings.verificationBadgeVerified,
                            onClick = {
                                infoDialogContent = strings.contentVerificationPolicyTitle to
                                    strings.contentVerificationPolicyBody
                            },
                            testTag = "settings_verification_policy_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.privacyPolicyTitle,
                            value = strings.aboutDeenTitle,
                            onClick = {
                                infoDialogContent = strings.privacyPolicyTitle to strings.privacyPolicyBody
                            },
                            testTag = "settings_privacy_policy_row"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingRow(
                            title = strings.aboutDeenTitle,
                            value = "${strings.appVersionLabel} ${BuildConfig.VERSION_NAME}",
                            onClick = {
                                infoDialogContent = strings.aboutDeenTitle to strings.aboutDeenBody
                            },
                            testTag = "settings_about_row"
                        )
                    }
                }
            }
        }
    }

    if (showMethodPicker) {
        AlertDialog(
            onDismissRequest = { showMethodPicker = false },
            title = { Text(strings.calculationMethodLabel) },
            text = {
                Column {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectMethod(method)
                                    showMethodPicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.prefs.calculationMethod == method,
                                onClick = {
                                    onSelectMethod(method)
                                    showMethodPicker = false
                                }
                            )
                            Text(
                                text = if (lang == AppLanguage.BENGALI) method.titleBn else method.titleEn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodPicker = false }) { Text(strings.close) }
            }
        )
    }

    if (showHighLatPicker) {
        AlertDialog(
            onDismissRequest = { showHighLatPicker = false },
            title = { Text(strings.highLatitudeLabel) },
            text = {
                Column {
                    HighLatitudeRule.entries.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectHighLat(rule)
                                    showHighLatPicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.prefs.highLatitudeRule == rule,
                                onClick = {
                                    onSelectHighLat(rule)
                                    showHighLatPicker = false
                                }
                            )
                            Text(
                                text = if (lang == AppLanguage.BENGALI) rule.titleBn else rule.titleEn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHighLatPicker = false }) { Text(strings.close) }
            }
        )
    }

    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text(strings.notebookDefaultCatLabel) },
            text = {
                Column {
                    notebookCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectNotebookDefaultCategory(category.id)
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.prefs.notebookDefaultCategoryId == category.id,
                                onClick = {
                                    onSelectNotebookDefaultCategory(category.id)
                                    showCategoryPicker = false
                                }
                            )
                            Text(
                                text = if (lang == AppLanguage.BENGALI) category.nameBn else category.nameEn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) { Text(strings.close) }
            }
        )
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(strings.notebookPinPrefLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.notebookPrivacyInfoBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { ch -> newPin = ch.filter { it.isDigit() }.take(4) },
                        label = { Text(strings.notebookPinEnterNew) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetNotebookPin(newPin)
                        showPinDialog = false
                    },
                    enabled = newPin.length == 4,
                    modifier = Modifier.testTag("settings_pin_save_button")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (state.prefs.isNotebookLockEnabled) {
                        TextButton(
                            onClick = {
                                onClearNotebookPin()
                                showPinDialog = false
                            },
                            modifier = Modifier.testTag("settings_pin_remove_button")
                        ) {
                            Text(strings.notebookPinRemove, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showPinDialog = false }) {
                        Text(strings.cancel)
                    }
                }
            }
        )
    }

    infoDialogContent?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { infoDialogContent = null },
            title = { Text(title) },
            text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { infoDialogContent = null }) { Text(strings.close) }
            }
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    testTag: String,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
