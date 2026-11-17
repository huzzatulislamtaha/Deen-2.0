package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.i18n.LocalAppStrings
import com.example.domain.model.FontScalePreset
import com.example.domain.model.LatinFontChoice
import com.example.ui.components.DeenCard
import com.example.ui.theme.scaledArabicDisplayStyle
import com.example.ui.theme.scaledBengaliBodyStyle
import com.example.ui.theme.scaledEnglishBodyStyle
import com.example.ui.viewmodel.FontSettingsUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    state: FontSettingsUiState,
    onSelectPreset: (FontScalePreset) -> Unit,
    onArabicScaleChange: (Float) -> Unit,
    onBengaliScaleChange: (Float) -> Unit,
    onEnglishScaleChange: (Float) -> Unit,
    onLatinFontChange: (LatinFontChoice) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val config = state.fontConfig

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.fontSettingsTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("font_settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("font_reset_icon_button")
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = strings.resetFontDefaults)
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
                    .testTag("font_lazy_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Required live preview card
                item {
                    DeenCard(highlighted = true) {
                        Text(
                            text = strings.fontPreviewSection,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = scaledArabicDisplayStyle(config),
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("font_preview_arabic")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "আপনার দৈনন্দিন ইসলামিক সঙ্গী",
                            style = scaledBengaliBodyStyle(config),
                            modifier = Modifier.testTag("font_preview_bengali")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Islamic Daily Companion",
                            style = scaledEnglishBodyStyle(config),
                            modifier = Modifier.testTag("font_preview_english")
                        )
                    }
                }

                // App Font Scale Preset (Small / Medium / Large / Extra Large)
                item {
                    DeenCard {
                        Text(
                            text = strings.fontAppSizeLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val presets = listOf(
                            FontScalePreset.SMALL to strings.fontSizeSmall,
                            FontScalePreset.MEDIUM to strings.fontSizeMedium,
                            FontScalePreset.LARGE to strings.fontSizeLarge,
                            FontScalePreset.EXTRA_LARGE to strings.fontSizeExtraLarge
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            presets.forEachIndexed { idx, (preset, label) ->
                                SegmentedButton(
                                    selected = config.appScalePreset == preset,
                                    onClick = { onSelectPreset(preset) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, presets.size),
                                    modifier = Modifier.testTag("font_preset_${preset.key}")
                                ) {
                                    Text(label, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.fontLatinFamilyLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            LatinFontChoice.entries.forEachIndexed { idx, choice ->
                                SegmentedButton(
                                    selected = config.latinFontChoice == choice,
                                    onClick = { onLatinFontChange(choice) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, LatinFontChoice.entries.size),
                                    modifier = Modifier.testTag("latin_font_${choice.key}")
                                ) {
                                    Text(choice.displayName, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Independent Arabic, Bengali, English scaling sliders
                item {
                    DeenCard {
                        ScaleSliderRow(
                            label = strings.fontArabicSizeLabel,
                            scale = config.arabicFontScale,
                            range = 0.85f..1.50f,
                            onChange = onArabicScaleChange,
                            testTag = "arabic_font_slider"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ScaleSliderRow(
                            label = strings.fontBengaliSizeLabel,
                            scale = config.bengaliFontScale,
                            range = 0.85f..1.45f,
                            onChange = onBengaliScaleChange,
                            testTag = "bengali_font_slider"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ScaleSliderRow(
                            label = strings.fontEnglishSizeLabel,
                            scale = config.englishFontScale,
                            range = 0.85f..1.45f,
                            onChange = onEnglishScaleChange,
                            testTag = "english_font_slider"
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_reset_default_button")
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.resetFontDefaults)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaleSliderRow(
    label: String,
    scale: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${(scale * 100f).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = scale,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.testTag(testTag)
        )
    }
}
