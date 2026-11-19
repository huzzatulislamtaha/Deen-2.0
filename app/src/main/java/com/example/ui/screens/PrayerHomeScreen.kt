package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.data.repository.TimezoneRepository
import com.example.domain.model.CityLocation
import com.example.domain.model.DailyPrayerSchedule
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerName
import com.example.ui.components.ActivePrayerDurationProgress
import com.example.ui.components.DeenCard
import com.example.ui.components.PrayerBoundaryInfoNote
import com.example.ui.components.PrayerInformationSections
import com.example.ui.components.prayerDisplayName
import com.example.ui.viewmodel.MainUiState
import java.util.Locale
import java.util.TimeZone

private data class HomeFeatureShortcut(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun PrayerHomeScreen(
    state: MainUiState,
    notebookCount: Int,
    prayedTodayCount: Int,
    onDetectGps: () -> Unit,
    onTogglePrayerNotification: (PrayerName, Boolean) -> Unit,
    onOpenLocationSelection: () -> Unit,
    onOpenTimezoneSelection: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenDuaAzkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenPrayerTracker: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val lang = state.prefs.language
    var showMonthlyDialog by remember { mutableStateOf(false) }

    var hasNotifPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            onDetectGps()
        }
    }

    val effectiveTzId = state.prefs.effectiveTimeZoneId
    val tzOffsetText = remember(effectiveTzId) {
        val tz = TimeZone.getTimeZone(effectiveTzId)
        TimezoneRepository.formatUtcOffset(tz.getOffset(System.currentTimeMillis()))
    }

    val loc = state.prefs.location
    val cityDisplay = if (lang == AppLanguage.BENGALI) {
        buildString {
            if (loc.upazilaNameBn.isNotBlank()) {
                append(loc.upazilaNameBn)
                append(", ")
            }
            append(loc.nameBn)
            append(", ")
            append(loc.countryBn)
        }
    } else {
        buildString {
            if (loc.upazilaNameEn.isNotBlank()) {
                append(loc.upazilaNameEn)
                append(", ")
            }
            append(loc.nameEn)
            append(", ")
            append(loc.countryEn)
        }
    }

    val hijriMonthName = strings.hijriMonths.getOrElse(state.hijriToday.month - 1) { "" }

    val featureShortcuts = listOf(
        HomeFeatureShortcut(
            id = "prayer_times",
            title = strings.featurePrayerTimes,
            subtitle = state.nextPrayer?.let { "${prayerDisplayName(it.prayer)} • ${it.formattedTime}" } ?: strings.todaySchedule,
            icon = Icons.Outlined.AccessTime,
            onClick = { showMonthlyDialog = true }
        ),
        HomeFeatureShortcut(
            id = "qibla",
            title = strings.featureQibla,
            subtitle = String.format(Locale.US, "%.1f°", state.qiblaBearing),
            icon = Icons.Outlined.Explore,
            onClick = onOpenQibla
        ),
        HomeFeatureShortcut(
            id = "quran",
            title = strings.featureQuran,
            subtitle = strings.quranTitle,
            icon = Icons.Outlined.AutoStories,
            onClick = onOpenQuran
        ),
        HomeFeatureShortcut(
            id = "hadith",
            title = strings.featureHadith,
            subtitle = strings.tabHadith,
            icon = Icons.Outlined.MenuBook,
            onClick = onOpenHadith
        ),
        HomeFeatureShortcut(
            id = "dua_azkar",
            title = strings.featureDuaAzkar,
            subtitle = strings.tabDuaAzkar,
            icon = Icons.Outlined.VolunteerActivism,
            onClick = onOpenDuaAzkar
        ),
        HomeFeatureShortcut(
            id = "tasbih",
            title = strings.featureTasbih,
            subtitle = strings.tabTasbih,
            icon = Icons.Outlined.RadioButtonChecked,
            onClick = onOpenTasbih
        ),
        HomeFeatureShortcut(
            id = "prayer_tracker",
            title = strings.featurePrayerTracker,
            subtitle = String.format(strings.homePrayedTodayFormat, prayedTodayCount),
            icon = Icons.Outlined.CheckCircle,
            onClick = onOpenPrayerTracker
        ),
        HomeFeatureShortcut(
            id = "calendar",
            title = strings.featureCalendar,
            subtitle = "${state.hijriToday.day} $hijriMonthName",
            icon = Icons.Outlined.CalendarMonth,
            onClick = onOpenCalendar
        ),
        HomeFeatureShortcut(
            id = "notebook",
            title = strings.featureNotebook,
            subtitle = String.format(strings.homeNotesCountFormat, notebookCount),
            icon = Icons.Outlined.EditNote,
            onClick = onOpenNotebook
        ),
        HomeFeatureShortcut(
            id = "location",
            title = strings.featureLocation,
            subtitle = if (lang == AppLanguage.BENGALI) loc.nameBn else loc.nameEn,
            icon = Icons.Outlined.LocationOn,
            onClick = onOpenLocationSelection
        ),
        HomeFeatureShortcut(
            id = "timezone",
            title = strings.featureTimezone,
            subtitle = "$effectiveTzId • $tzOffsetText",
            icon = Icons.Outlined.Public,
            onClick = onOpenTimezoneSelection
        ),
        HomeFeatureShortcut(
            id = "settings",
            title = strings.featureSettings,
            subtitle = strings.settingsTitle,
            icon = Icons.Outlined.Settings,
            onClick = onOpenSettings
        )
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .testTag("home_lazy_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.prayerTimetableTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenLocationSelection)
                                .padding(vertical = 3.dp)
                                .testTag("select_city_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = strings.selectCity,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cityDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenTimezoneSelection)
                                .padding(vertical = 2.dp)
                                .testTag("home_timezone_pill")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = strings.timezoneLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$effectiveTzId ($tzOffsetText)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val coarse = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val fine = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (coarse || fine) {
                                    onDetectGps()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.testTag("detect_gps_button")
                        ) {
                            if (state.isDetectingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.MyLocation,
                                    contentDescription = strings.detectLocation,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = strings.settingsTitle,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            item {
                // Hero Next Prayer Card with Islamic geometric arch artwork
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_deen_hero_banner),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xB8062E22),
                                            Color(0xDC083B2C)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${state.hijriToday.day} $hijriMonthName ${state.hijriToday.year} AH",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFE3BE5A)
                            )
                            Text(
                                text = strings.nextPrayerLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD6EDE3)
                            )
                            val next = state.nextPrayer
                            if (next != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = prayerDisplayName(next.prayer),
                                            style = MaterialTheme.typography.displaySmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = next.formattedTime,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFFE3BE5A)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = strings.startsIn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD6EDE3)
                                        )
                                        Text(
                                            text = formatCountdown(next.remainingMillis),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.testTag("next_prayer_countdown")
                                        )
                                    }
                                }
                            }
                            if (loc.isReferenceCoordinate && !loc.isGpsDetected) {
                                Text(
                                    text = strings.referenceCoordsNotice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD6EDE3).copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    DeenCard(highlighted = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.notificationPermissionNeeded,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier.testTag("grant_notification_permission_button")
                            ) {
                                Text(strings.grantPermission)
                            }
                        }
                    }
                }
            }

            if (state.locationError != null) {
                item {
                    DeenCard {
                        Text(
                            text = state.locationError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.todaySchedule,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    FilledTonalButton(
                        onClick = { showMonthlyDialog = true },
                        modifier = Modifier.testTag("view_monthly_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.viewMonthlySchedule)
                    }
                }
            }

            val entries = state.todaySchedule?.entries ?: emptyList()
            val durationByPrayer = state.prayerInfo?.allPrayerDurations?.associateBy { it.prayer } ?: emptyMap()
            val hasCurrentPrayer = state.prayerInfo?.currentPrayerDuration != null
            items(entries, key = { it.prayer.key }) { entry ->
                val duration = if (entry.prayer.isObligatory) durationByPrayer[entry.prayer] else null
                val isCurrent = duration?.isCurrent == true
                val isNextFallback = !hasCurrentPrayer &&
                    state.nextPrayer?.prayer == entry.prayer &&
                    !state.nextPrayer.isTomorrow
                val isHighlighted = isCurrent || isNextFallback
                val notifEnabled = state.prefs.notificationPrefs.isEnabled(entry.prayer)
                val timeDisplay = if (duration != null) {
                    "${duration.formattedStart} – ${duration.formattedEnd}"
                } else {
                    entry.formattedTime
                }

                DeenCard(highlighted = isHighlighted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = prayerDisplayName(entry.prayer),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = strings.currentPrayerTimeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.testTag("current_prayer_duration_badge")
                                        )
                                    }
                                }
                                Text(
                                    text = timeDisplay,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isHighlighted) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        if (entry.prayer.isObligatory) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (notifEnabled) {
                                        Icons.Outlined.NotificationsActive
                                    } else {
                                        Icons.Outlined.NotificationsOff
                                    },
                                    contentDescription = null,
                                    tint = if (notifEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = notifEnabled,
                                    onCheckedChange = { onTogglePrayerNotification(entry.prayer, it) },
                                    modifier = Modifier.testTag("prayer_notif_switch_${entry.prayer.key}")
                                )
                            }
                        }
                    }

                    if (isCurrent && duration != null) {
                        ActivePrayerDurationProgress(duration = duration)
                    }
                }
            }

            if (state.prayerInfo != null) {
                item {
                    PrayerBoundaryInfoNote()
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                PrayerInformationSections(
                    prayerInfo = state.prayerInfo
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.homeFeaturesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val rows = featureShortcuts.chunked(2)
            items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { shortcut ->
                        DeenCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = shortcut.onClick)
                                .testTag("home_shortcut_${shortcut.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = shortcut.icon,
                                        contentDescription = shortcut.title,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shortcut.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = shortcut.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                val methodLabel = if (lang == AppLanguage.BENGALI) {
                    state.prefs.calculationMethod.titleBn
                } else {
                    state.prefs.calculationMethod.titleEn
                }
                val madhabLabel = if (state.prefs.madhab == Madhab.HANAFI) {
                    strings.madhabHanafi
                } else {
                    strings.madhabStandard
                }
                DeenCard {
                    Text(
                        text = "${strings.calculationMethodLabel}: $methodLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.madhabLabel}: $madhabLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showMonthlyDialog) {
        MonthlyTimetableDialog(
            schedules = state.monthlySchedules,
            onDismiss = { showMonthlyDialog = false }
        )
    }
}

@Composable
fun CityPickerDialog(
    cities: List<CityLocation>,
    currentCityId: String,
    language: AppLanguage,
    onSelect: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.selectCity) },
        text = {
            LazyColumn(modifier = Modifier.height(340.dp)) {
                items(cities, key = { it.id }) { city ->
                    val name = if (language == AppLanguage.BENGALI) {
                        "${city.nameBn}, ${city.countryBn}"
                    } else {
                        "${city.nameEn}, ${city.countryEn}"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(city) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = city.id == currentCityId,
                            onClick = { onSelect(city) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}

@Composable
private fun MonthlyTimetableDialog(
    schedules: List<DailyPrayerSchedule>,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.monthlyTimetable) },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(schedules, key = { it.dateKey }) { day ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = day.dateKey,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            day.entries.filter { it.prayer.isObligatory }.forEach { entry ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = prayerDisplayName(entry.prayer),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.formattedTime.replace(" AM", "").replace(" PM", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}

private fun formatCountdown(millis: Long): String {
    val totalSec = (millis / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
