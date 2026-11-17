package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.data.repository.BangladeshLocationCatalog
import com.example.domain.model.BdDistrict
import com.example.domain.model.BdDivision
import com.example.domain.model.BdUpazila
import com.example.domain.model.CityLocation
import com.example.domain.model.LocationMode
import com.example.ui.components.DeenCard
import com.example.ui.viewmodel.LocationUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    state: LocationUiState,
    language: AppLanguage,
    onSelectCountry: (String) -> Unit,
    onSelectDivision: (BdDivision) -> Unit,
    onSelectDistrict: (BdDistrict) -> Unit,
    onSelectUpazila: (BdUpazila?) -> Unit,
    onDivisionSearch: (String) -> Unit,
    onDistrictSearch: (String) -> Unit,
    onConfirmBangladesh: () -> Unit,
    onConfirmInternational: (CityLocation, String) -> Unit,
    onDetectGps: () -> Unit,
    onSetManualMode: () -> Unit,
    onResetDefault: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        ) {
            onDetectGps()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.locationSettingsTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("location_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetDefault,
                        modifier = Modifier.testTag("location_reset_default_button")
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = strings.resetToDefaultDhaka)
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
                    .testTag("location_lazy_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    DeenCard(highlighted = true) {
                        Text(
                            text = strings.activeLocationSummary,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val locName = if (language == AppLanguage.BENGALI) {
                            buildString {
                                append(state.currentLocation.nameBn)
                                if (state.currentLocation.divisionNameBn.isNotBlank()) {
                                    append(" • ${state.currentLocation.divisionNameBn}")
                                }
                                if (state.currentLocation.countryBn.isNotBlank()) {
                                    append(", ${state.currentLocation.countryBn}")
                                }
                            }
                        } else {
                            buildString {
                                append(state.currentLocation.nameEn)
                                if (state.currentLocation.divisionNameEn.isNotBlank()) {
                                    append(" • ${state.currentLocation.divisionNameEn}")
                                }
                                if (state.currentLocation.countryEn.isNotBlank()) {
                                    append(", ${state.currentLocation.countryEn}")
                                }
                            }
                        }
                        Text(
                            text = locName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${strings.coordinatesLabel}: ${String.format(Locale.US, "%.4f°, %.4f°", state.currentLocation.latitude, state.currentLocation.longitude)} • ${state.currentLocation.timeZoneId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.currentLocation.isGpsDetected) strings.gpsDetectedBadge else strings.referenceCoordsNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                item {
                    DeenCard {
                        Text(
                            text = strings.locationModeLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = state.locationMode == LocationMode.MANUAL,
                                onClick = onSetManualMode,
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                                modifier = Modifier.testTag("location_mode_manual")
                            ) {
                                Text(strings.locationModeManual)
                            }
                            SegmentedButton(
                                selected = state.locationMode == LocationMode.AUTOMATIC,
                                onClick = {
                                    val coarse = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    val fine = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (coarse || fine) {
                                        onDetectGps()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            )
                                        )
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                                modifier = Modifier.testTag("location_mode_auto")
                            ) {
                                Text(strings.locationModeAuto)
                            }
                        }

                        if (state.isDetectingGps) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(strings.loading, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (state.gpsError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.gpsError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(onClick = onSetManualMode) {
                                Text(strings.useManualFallback)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    val coarse = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    val fine = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (coarse || fine) {
                                        onDetectGps()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("location_gps_detect_btn")
                            ) {
                                Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.detectLocation)
                            }
                        }
                    }
                }

                // Step 1: Country selector
                item {
                    DeenCard {
                        Text(
                            text = "1. ${strings.countryLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.countries.forEach { country ->
                                FilterChip(
                                    selected = state.selectedCountryCode == country.code,
                                    onClick = { onSelectCountry(country.code) },
                                    label = {
                                        Text(if (language == AppLanguage.BENGALI) country.nameBn else country.nameEn)
                                    },
                                    modifier = Modifier.testTag("country_chip_${country.code}")
                                )
                            }
                        }
                    }
                }

                if (state.selectedCountryCode == BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE) {
                    // Step 2: Division Selector (8 Official Divisions)
                    item {
                        DeenCard {
                            Text(
                                text = "2. ${strings.divisionLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.divisionSearchQuery,
                                onValueChange = onDivisionSearch,
                                placeholder = { Text(strings.searchDivisionPlaceholder) },
                                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (state.divisionSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onDivisionSearch("") }) {
                                            Icon(Icons.Outlined.Close, contentDescription = strings.clearSearch)
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("division_search_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.filteredDivisions.forEach { division ->
                                    FilterChip(
                                        selected = state.selectedDivision == division,
                                        onClick = { onSelectDivision(division) },
                                        label = {
                                            Text(if (language == AppLanguage.BENGALI) division.nameBn else division.nameEn)
                                        },
                                        modifier = Modifier.testTag("division_chip_${division.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Step 3: District Selector (All 64 Districts)
                    item {
                        DeenCard {
                            Text(
                                text = "3. ${strings.districtLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.districtSearchQuery,
                                onValueChange = onDistrictSearch,
                                placeholder = { Text(strings.searchDistrictPlaceholder) },
                                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (state.districtSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onDistrictSearch("") }) {
                                            Icon(Icons.Outlined.Close, contentDescription = strings.clearSearch)
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("district_search_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                state.filteredDistricts.forEach { district ->
                                    val selected = state.selectedDistrict.id == district.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectDistrict(district) }
                                            .padding(vertical = 6.dp)
                                            .testTag("district_row_${district.id}"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { onSelectDistrict(district) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (language == AppLanguage.BENGALI) {
                                                    "${district.nameBn} (${district.nameEn})"
                                                } else {
                                                    "${district.nameEn} (${district.nameBn})"
                                                },
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = if (language == AppLanguage.BENGALI) {
                                                    "${district.division.nameBn} বিভাগ"
                                                } else {
                                                    "${district.division.nameEn} Division"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.2f, %.2f", district.latitude, district.longitude),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Step 4: Optional Upazila (where reliable coordinates exist)
                    if (state.selectedDistrict.upazilas.isNotEmpty()) {
                        item {
                            DeenCard {
                                Text(
                                    text = "4. ${strings.upazilaLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = state.selectedUpazila == null,
                                        onClick = { onSelectUpazila(null) },
                                        label = { Text(strings.upazilaOptionalNone) }
                                    )
                                    state.selectedDistrict.upazilas.forEach { upazila ->
                                        FilterChip(
                                            selected = state.selectedUpazila?.id == upazila.id,
                                            onClick = { onSelectUpazila(upazila) },
                                            label = {
                                                Text(if (language == AppLanguage.BENGALI) upazila.nameBn else upazila.nameEn)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Step 5: Confirm Button
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onResetDefault,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.resetToDefaultDhaka, maxLines = 1)
                            }
                            Button(
                                onClick = {
                                    onConfirmBangladesh()
                                    onBack()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_bangladesh_location_button")
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.confirmLocation, maxLines = 1)
                            }
                        }
                    }
                } else {
                    items(state.internationalCities, key = { it.id }) { city ->
                        DeenCard(
                            modifier = Modifier
                                .clickable {
                                    onConfirmInternational(city, state.selectedCountryCode)
                                    onBack()
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) city.nameBn else city.nameEn,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${city.timeZoneId} • ${String.format(Locale.US, "%.2f, %.2f", city.latitude, city.longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
