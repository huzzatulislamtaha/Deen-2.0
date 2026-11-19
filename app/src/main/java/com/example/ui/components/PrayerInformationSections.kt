package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppStrings
import com.example.core.i18n.LocalAppStrings
import com.example.domain.calculator.ForbiddenPeriodInfo
import com.example.domain.calculator.ForbiddenPeriodType
import com.example.domain.calculator.PrayerDurationInfo
import com.example.domain.calculator.PrayerInformationState
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Reusable progress bar + remaining-time countdown for the currently active prayer
 * inside Today's Schedule.
 */
@Composable
fun ActivePrayerDurationProgress(
    duration: PrayerDurationInfo,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(
                    strings.prayerDurationElapsedFormat,
                    (duration.progress * 100f).roundToInt().coerceIn(0, 100)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${strings.prayerDurationRemainingLabel}: ${formatDurationHms(duration.remainingMillis)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("current_prayer_duration_remaining")
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { duration.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("current_prayer_duration_progress"),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Compact collapsible scholarly boundary note displayed beneath Today's Schedule.
 */
@Composable
fun PrayerBoundaryInfoNote(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    DeenCard(modifier = modifier.testTag("prayer_boundary_info_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .testTag("toggle_prayer_boundary_note_button"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = strings.prayerDurationTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = strings.prayerDurationTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) strings.hideAllFivePrayers else strings.viewAllFivePrayers,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = strings.prayerDurationGeneralNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.prayerDurationAsrIshaNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PrayerInformationSections(
    prayerInfo: PrayerInformationState?,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = strings.prayerInfoSectionTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("prayer_info_section_heading")
        )

        if (prayerInfo == null) {
            DeenCard(modifier = Modifier.testTag("prayer_info_unavailable_card")) {
                Text(
                    text = strings.prayerInfoUnavailable,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        ForbiddenPrayerTimesCard(
            prayerInfo = prayerInfo,
            strings = strings
        )
    }
}

@Composable
private fun ForbiddenPrayerTimesCard(
    prayerInfo: PrayerInformationState,
    strings: AppStrings
) {
    val active = prayerInfo.activeForbiddenPeriod
    val next = prayerInfo.nextForbiddenPeriod

    DeenCard(
        highlighted = active != null,
        modifier = Modifier.testTag("forbidden_times_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = strings.forbiddenTimesTitle,
                    tint = if (active != null) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = strings.forbiddenTimesTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.forbiddenTimesSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val statusLabel = when {
            active != null -> String.format(
                strings.forbiddenStatusActiveFormat,
                forbiddenPeriodLabel(active.type, strings)
            )
            next != null -> String.format(
                strings.forbiddenStatusNextFormat,
                forbiddenPeriodLabel(next.type, strings)
            )
            else -> ""
        }
        val countdownPrefix = if (active != null) {
            strings.forbiddenEndsInLabel
        } else {
            strings.forbiddenNextInLabel
        }

        if (statusLabel.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (active != null) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forbidden_status_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active != null) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$countdownPrefix ${formatDurationHms(prayerInfo.forbiddenCountdownMillis)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (active != null) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.testTag("forbidden_countdown_text")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prayerInfo.forbiddenPeriods.forEach { period ->
                ForbiddenPeriodRow(period = period, strings = strings)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(14.dp)
            )
            Text(
                text = strings.forbiddenPrecautionNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ForbiddenPeriodRow(
    period: ForbiddenPeriodInfo,
    strings: AppStrings
) {
    val icon: ImageVector = when (period.type) {
        ForbiddenPeriodType.SUNRISE -> Icons.Outlined.WbTwilight
        ForbiddenPeriodType.ZENITH -> Icons.Outlined.WbSunny
        ForbiddenPeriodType.SUNSET -> Icons.Outlined.NightsStay
    }
    val title = forbiddenPeriodLabel(period.type, strings)
    val desc = when (period.type) {
        ForbiddenPeriodType.SUNRISE -> strings.forbiddenSunriseDesc
        ForbiddenPeriodType.ZENITH -> strings.forbiddenZenithDesc
        ForbiddenPeriodType.SUNSET -> strings.forbiddenSunsetDesc
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (period.isActive) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forbidden_period_${period.type.key}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (period.isActive) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${period.formattedStart}\n${period.formattedEnd}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (period.isActive) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

private fun forbiddenPeriodLabel(type: ForbiddenPeriodType, strings: AppStrings): String =
    when (type) {
        ForbiddenPeriodType.SUNRISE -> strings.forbiddenSunriseTitle
        ForbiddenPeriodType.ZENITH -> strings.forbiddenZenithTitle
        ForbiddenPeriodType.SUNSET -> strings.forbiddenSunsetTitle
    }

private fun formatDurationHms(millis: Long): String {
    val totalSec = (millis / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
