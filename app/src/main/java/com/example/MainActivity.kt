package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.core.i18n.stringsFor
import com.example.data.local.database.DeenDatabase
import com.example.location.CompassReading
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FontSettingsScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LocationSelectionScreen
import com.example.ui.screens.NotebookScreen
import com.example.ui.screens.PrayerHomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimezoneSelectionScreen
import com.example.ui.screens.WorshipScreen
import com.example.ui.theme.DeenTheme
import com.example.ui.viewmodel.DeenViewModelFactory
import com.example.ui.viewmodel.FontSettingsUiState
import com.example.ui.viewmodel.FontSettingsViewModel
import com.example.ui.viewmodel.LibraryTab
import com.example.ui.viewmodel.LibraryUiState
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.LocationUiState
import com.example.ui.viewmodel.LocationViewModel
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NotebookUiState
import com.example.ui.viewmodel.NotebookViewModel
import com.example.ui.viewmodel.TimezoneUiState
import com.example.ui.viewmodel.TimezoneViewModel
import com.example.ui.viewmodel.WorshipUiState
import com.example.ui.viewmodel.WorshipViewModel

object DeenRoutes {
    const val PRAYER = "prayer"
    const val QIBLA = "qibla"
    const val LIBRARY = "library"
    const val WORSHIP = "worship"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val NOTEBOOK = "notebook"
    const val LOCATION = "location"
    const val TIMEZONE = "timezone"
    const val FONT_SETTINGS = "font_settings"
    const val QURAN = "quran"
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = DeenViewModelFactory(application as DeenApplication)

        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val libraryViewModel: LibraryViewModel = viewModel(factory = factory)
            val worshipViewModel: WorshipViewModel = viewModel(factory = factory)
            val notebookViewModel: NotebookViewModel = viewModel(factory = factory)
            val locationViewModel: LocationViewModel = viewModel(factory = factory)
            val timezoneViewModel: TimezoneViewModel = viewModel(factory = factory)
            val fontSettingsViewModel: FontSettingsViewModel = viewModel(factory = factory)

            val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val compassReading by mainViewModel.compassReading.collectAsStateWithLifecycle()
            val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
            val worshipState by worshipViewModel.uiState.collectAsStateWithLifecycle()
            val notebookState by notebookViewModel.uiState.collectAsStateWithLifecycle()
            val locationState by locationViewModel.uiState.collectAsStateWithLifecycle()
            val timezoneState by timezoneViewModel.uiState.collectAsStateWithLifecycle()
            val fontSettingsState by fontSettingsViewModel.uiState.collectAsStateWithLifecycle()

            val strings = stringsFor(mainState.prefs.language)

            CompositionLocalProvider(LocalAppStrings provides strings) {
                DeenTheme(
                    themeMode = mainState.prefs.themeMode,
                    fontConfig = mainState.prefs.fontConfig,
                    language = mainState.prefs.language
                ) {
                    DeenAppScaffold(
                        mainViewModel = mainViewModel,
                        libraryViewModel = libraryViewModel,
                        worshipViewModel = worshipViewModel,
                        notebookViewModel = notebookViewModel,
                        locationViewModel = locationViewModel,
                        timezoneViewModel = timezoneViewModel,
                        fontSettingsViewModel = fontSettingsViewModel,
                        mainState = mainState,
                        compassReading = compassReading,
                        libraryState = libraryState,
                        worshipState = worshipState,
                        notebookState = notebookState,
                        locationState = locationState,
                        timezoneState = timezoneState,
                        fontSettingsState = fontSettingsState
                    )
                }
            }
        }
    }
}

@Composable
private fun DeenAppScaffold(
    mainViewModel: MainViewModel,
    libraryViewModel: LibraryViewModel,
    worshipViewModel: WorshipViewModel,
    notebookViewModel: NotebookViewModel,
    locationViewModel: LocationViewModel,
    timezoneViewModel: TimezoneViewModel,
    fontSettingsViewModel: FontSettingsViewModel,
    mainState: MainUiState,
    compassReading: CompassReading?,
    libraryState: LibraryUiState,
    worshipState: WorshipUiState,
    notebookState: NotebookUiState,
    locationState: LocationUiState,
    timezoneState: TimezoneUiState,
    fontSettingsState: FontSettingsUiState
) {
    val strings = LocalAppStrings.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: DeenRoutes.PRAYER

    val destinations = listOf(
        NavDestination(DeenRoutes.PRAYER, strings.navPrayer, Icons.Filled.AccessTime, Icons.Outlined.AccessTime),
        NavDestination(DeenRoutes.QIBLA, strings.navQibla, Icons.Filled.Explore, Icons.Outlined.Explore),
        NavDestination(DeenRoutes.LIBRARY, strings.navLibrary, Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavDestination(DeenRoutes.WORSHIP, strings.navWorship, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavDestination(DeenRoutes.CALENDAR, strings.navCalendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    )

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val topLevelRoutes = setOf(
        DeenRoutes.PRAYER,
        DeenRoutes.QIBLA,
        DeenRoutes.LIBRARY,
        DeenRoutes.WORSHIP,
        DeenRoutes.CALENDAR
    )
    val showPrimaryNav = currentRoute in topLevelRoutes

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavRail && showPrimaryNav) {
                    NavigationBar {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && showPrimaryNav) {
                    NavigationRail {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = DeenRoutes.PRAYER,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(DeenRoutes.PRAYER) {
                        PrayerHomeScreen(
                            state = mainState,
                            notebookCount = notebookState.allActiveNotesCount,
                            prayedTodayCount = worshipState.todayCompletedCount,
                            onDetectGps = mainViewModel::detectGpsLocation,
                            onTogglePrayerNotification = mainViewModel::setPrayerNotification,
                            onOpenLocationSelection = { navController.navigate(DeenRoutes.LOCATION) },
                            onOpenTimezoneSelection = { navController.navigate(DeenRoutes.TIMEZONE) },
                            onOpenQuran = { navController.navigate(DeenRoutes.QURAN) },
                            onOpenQibla = { navigateTopLevel(DeenRoutes.QIBLA) },
                            onOpenHadith = {
                                libraryViewModel.setTab(LibraryTab.HADITH)
                                navigateTopLevel(DeenRoutes.LIBRARY)
                            },
                            onOpenDuaAzkar = {
                                libraryViewModel.setTab(LibraryTab.DUA)
                                navigateTopLevel(DeenRoutes.LIBRARY)
                            },
                            onOpenTasbih = {
                                worshipViewModel.setSection(0)
                                navigateTopLevel(DeenRoutes.WORSHIP)
                            },
                            onOpenPrayerTracker = {
                                worshipViewModel.setSection(1)
                                navigateTopLevel(DeenRoutes.WORSHIP)
                            },
                            onOpenCalendar = { navigateTopLevel(DeenRoutes.CALENDAR) },
                            onOpenNotebook = { navController.navigate(DeenRoutes.NOTEBOOK) },
                            onOpenSettings = { navController.navigate(DeenRoutes.SETTINGS) }
                        )
                    }
                    composable(DeenRoutes.QIBLA) {
                        QiblaScreen(
                            state = mainState,
                            compassReading = compassReading,
                            onDetectGps = mainViewModel::detectGpsLocation
                        )
                    }
                    composable(DeenRoutes.LIBRARY) {
                        LibraryScreen(
                            state = libraryState,
                            language = mainState.prefs.language,
                            onSelectTab = libraryViewModel::setTab,
                            onSearchChange = libraryViewModel::setSearchQuery,
                            onSelectDuaCategory = libraryViewModel::setDuaCategory,
                            onToggleHadithBookmark = libraryViewModel::toggleHadithBookmark,
                            onToggleDuaBookmark = libraryViewModel::toggleDuaBookmark
                        )
                    }
                    composable(DeenRoutes.WORSHIP) {
                        WorshipScreen(
                            state = worshipState,
                            language = mainState.prefs.language,
                            onSelectSection = worshipViewModel::setSection,
                            onIncrementTasbih = worshipViewModel::incrementTasbih,
                            onResetTasbih = { worshipViewModel.resetTasbih(saveProgressIfPositive = true) },
                            onSelectPreset = worshipViewModel::selectPreset,
                            onSetTarget = worshipViewModel::setTarget,
                            onToggleHaptic = worshipViewModel::setHapticEnabled,
                            onAddCustomPreset = worshipViewModel::addCustomPreset,
                            onDeleteCustomPreset = worshipViewModel::deleteCustomPreset,
                            onShiftDate = worshipViewModel::shiftTrackerDate,
                            onSetPrayerStatus = worshipViewModel::setPrayerStatus
                        )
                    }
                    composable(DeenRoutes.CALENDAR) {
                        CalendarScreen(
                            state = mainState,
                            onChangeHijriOffset = mainViewModel::setHijriOffset
                        )
                    }
                    composable(DeenRoutes.QURAN) {
                        QuranScreen(
                            language = mainState.prefs.language,
                            onReflectInNotebook = { excerpt ->
                                val title = if (mainState.prefs.language == AppLanguage.BENGALI) {
                                    excerpt.titleBn
                                } else {
                                    excerpt.titleEn
                                }
                                val meaning = if (mainState.prefs.language == AppLanguage.BENGALI) {
                                    excerpt.bengaliTranslation
                                } else {
                                    excerpt.englishTranslation
                                }
                                val ref = "Surah ${excerpt.surahNumber}:${excerpt.ayahRange}"
                                notebookViewModel.openNewNote(
                                    defaultCategoryId = "cat_quran",
                                    prefillTitle = "$title ($ref)",
                                    prefillContent = "${excerpt.arabicText}\n\n$meaning",
                                    prefillBook = "Al-Quran — $ref"
                                )
                                navController.navigate(DeenRoutes.NOTEBOOK)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(DeenRoutes.NOTEBOOK) {
                        NotebookScreen(
                            state = notebookState,
                            language = mainState.prefs.language,
                            storedPinHash = mainState.prefs.notebookPinHash,
                            viewModel = notebookViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(DeenRoutes.LOCATION) {
                        LocationSelectionScreen(
                            state = locationState,
                            language = mainState.prefs.language,
                            onSelectCountry = locationViewModel::selectCountry,
                            onSelectDivision = locationViewModel::selectDivision,
                            onSelectDistrict = locationViewModel::selectDistrict,
                            onSelectUpazila = locationViewModel::selectUpazila,
                            onDivisionSearch = locationViewModel::setDivisionSearchQuery,
                            onDistrictSearch = locationViewModel::setDistrictSearchQuery,
                            onConfirmBangladesh = locationViewModel::confirmBangladeshLocation,
                            onConfirmInternational = locationViewModel::confirmInternationalCity,
                            onDetectGps = locationViewModel::detectGpsLocation,
                            onSetManualMode = locationViewModel::setManualMode,
                            onResetDefault = locationViewModel::resetToDefault,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(DeenRoutes.TIMEZONE) {
                        TimezoneSelectionScreen(
                            state = timezoneState,
                            onSearchChange = timezoneViewModel::setSearchQuery,
                            onSelectTimezone = timezoneViewModel::selectManualTimezone,
                            onSetAutomatic = timezoneViewModel::setAutomaticTimezone,
                            onResetBangladeshDefault = timezoneViewModel::resetToBangladeshDefault,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(DeenRoutes.FONT_SETTINGS) {
                        FontSettingsScreen(
                            state = fontSettingsState,
                            onSelectPreset = fontSettingsViewModel::setPreset,
                            onArabicScaleChange = fontSettingsViewModel::setArabicScale,
                            onBengaliScaleChange = fontSettingsViewModel::setBengaliScale,
                            onEnglishScaleChange = fontSettingsViewModel::setEnglishScale,
                            onLatinFontChange = fontSettingsViewModel::setLatinFontChoice,
                            onResetDefaults = fontSettingsViewModel::resetToDefaults,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(DeenRoutes.SETTINGS) {
                        SettingsScreen(
                            state = mainState,
                            notebookCategories = notebookState.categories,
                            onLanguageChange = mainViewModel::setLanguage,
                            onThemeChange = mainViewModel::setThemeMode,
                            onOpenLocationSelection = { navController.navigate(DeenRoutes.LOCATION) },
                            onOpenTimezoneSelection = { navController.navigate(DeenRoutes.TIMEZONE) },
                            onOpenFontSettings = { navController.navigate(DeenRoutes.FONT_SETTINGS) },
                            onSelectMethod = mainViewModel::setCalculationMethod,
                            onSelectMadhab = mainViewModel::setMadhab,
                            onSelectHighLat = mainViewModel::setHighLatitudeRule,
                            onSelectSound = mainViewModel::setAzanSoundOption,
                            onSelectNotebookDefaultCategory = mainViewModel::setNotebookDefaultCategory,
                            onToggleNotebookAutosave = mainViewModel::setNotebookAutosaveEnabled,
                            onSetNotebookPin = mainViewModel::setNotebookPin,
                            onClearNotebookPin = mainViewModel::clearNotebookPin,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
