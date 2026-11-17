package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.i18n.LocalAppStrings
import com.example.domain.calculator.HijriCalendarCalculator
import com.example.ui.components.DeenCard
import com.example.ui.viewmodel.MainUiState
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun CalendarScreen(
    state: MainUiState,
    onChangeHijriOffset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tz = TimeZone.getTimeZone(state.prefs.effectiveTimeZoneId)
    val todayCal = remember(state.prefs.effectiveTimeZoneId) { Calendar.getInstance(tz) }
    var displayedYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH) + 1) }

    val monthCal = Calendar.getInstance(tz).apply {
        set(Calendar.YEAR, displayedYear)
        set(Calendar.MONTH, displayedMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeekOffset = monthCal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0

    val monthLabel = "${DateFormatSymbols(Locale.ENGLISH).months[displayedMonth - 1]} $displayedYear"
    val hijriMonthName = strings.hijriMonths.getOrElse(state.hijriToday.month - 1) { "" }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = strings.calendarTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                DeenCard(highlighted = true) {
                    Text(
                        text = strings.hijriTodayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.hijriToday.day} $hijriMonthName ${state.hijriToday.year} AH",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.predictedMoonNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.hijriOffsetLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onChangeHijriOffset(state.prefs.hijriOffsetDays - 1) },
                                enabled = state.prefs.hijriOffsetDays > -2,
                                modifier = Modifier.testTag("hijri_offset_minus")
                            ) {
                                Icon(Icons.Outlined.Remove, contentDescription = null)
                            }
                            Text(
                                text = "${if (state.prefs.hijriOffsetDays > 0) "+" else ""}${state.prefs.hijriOffsetDays} ${strings.hijriDaysUnit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { onChangeHijriOffset(state.prefs.hijriOffsetDays + 1) },
                                enabled = state.prefs.hijriOffsetDays < 2,
                                modifier = Modifier.testTag("hijri_offset_plus")
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                            }
                        }
                    }
                }
            }

            item {
                DeenCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (displayedMonth == 1) {
                                displayedMonth = 12
                                displayedYear -= 1
                            } else {
                                displayedMonth -= 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = strings.back)
                        }
                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            if (displayedMonth == 12) {
                                displayedMonth = 1
                                displayedYear += 1
                            } else {
                                displayedMonth += 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { dow ->
                            Text(
                                text = dow,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val totalCells = firstDayOfWeekOffset + daysInMonth
                    val rows = (totalCells + 6) / 7
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until 7) {
                                    val cellIdx = r * 7 + c
                                    val dayNum = cellIdx - firstDayOfWeekOffset + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val isToday = displayedYear == todayCal.get(Calendar.YEAR) &&
                                            displayedMonth == todayCal.get(Calendar.MONTH) + 1 &&
                                            dayNum == todayCal.get(Calendar.DAY_OF_MONTH)
                                        val hijriCell = HijriCalendarCalculator.gregorianToHijri(
                                            displayedYear,
                                            displayedMonth,
                                            dayNum,
                                            state.prefs.hijriOffsetDays
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isToday) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                                )
                                                .border(
                                                    width = if (hijriCell.day == 1 && !isToday) 1.dp else 0.dp,
                                                    color = if (hijriCell.day == 1 && !isToday) {
                                                        MaterialTheme.colorScheme.secondary
                                                    } else {
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$dayNum",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${hijriCell.day}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                    else MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = strings.importantDatesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.importantEvents, key = { it.id }) { event ->
                val title = when (event.id) {
                    "new_year" -> strings.eventNewYear
                    "ashura" -> strings.eventAshura
                    "ramadan_start" -> strings.eventRamadanStart
                    "laylatul_qadr" -> strings.eventLaylatulQadr
                    "eid_fitr" -> strings.eventEidFitr
                    "arafah" -> strings.eventArafah
                    "eid_adha" -> strings.eventEidAdha
                    else -> event.id
                }
                val hMonthName = strings.hijriMonths.getOrElse(event.hijriMonth - 1) { "" }
                DeenCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${event.hijriDay} $hMonthName ${state.hijriToday.year} AH",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = event.gregorianLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
