package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BangladeshLocationCatalog
import com.example.data.repository.CityCatalog
import com.example.data.repository.LocationRepository
import com.example.data.repository.PreferencesRepository
import com.example.domain.model.BdDistrict
import com.example.domain.model.BdDivision
import com.example.domain.model.BdUpazila
import com.example.domain.model.CityLocation
import com.example.domain.model.CountryOption
import com.example.domain.model.LocationMode
import com.example.location.LocationHelper
import com.example.notifications.PrayerAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocationUiState(
    val currentLocation: CityLocation = CityCatalog.defaultCity,
    val locationMode: LocationMode = LocationMode.MANUAL,
    val selectedCountryCode: String = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE,
    val countries: List<CountryOption> = BangladeshLocationCatalog.countries,
    val divisions: List<BdDivision> = BangladeshLocationCatalog.divisions,
    val selectedDivision: BdDivision = BdDivision.DHAKA,
    val divisionSearchQuery: String = "",
    val districtSearchQuery: String = "",
    val filteredDivisions: List<BdDivision> = BangladeshLocationCatalog.divisions,
    val filteredDistricts: List<BdDistrict> = emptyList(),
    val selectedDistrict: BdDistrict = BangladeshLocationCatalog.defaultDistrict,
    val selectedUpazila: BdUpazila? = null,
    val internationalCities: List<CityLocation> = emptyList(),
    val isDetectingGps: Boolean = false,
    val gpsError: String? = null,
    val confirmationMessage: String? = null
)

class LocationViewModel(
    private val locationRepo: LocationRepository,
    private val prefsRepo: PreferencesRepository,
    private val locationHelper: LocationHelper,
    private val alarmScheduler: PrayerAlarmScheduler
) : ViewModel() {

    private val workingCountryCode = MutableStateFlow<String?>(null)
    private val workingDivision = MutableStateFlow<BdDivision?>(null)
    private val workingDistrict = MutableStateFlow<BdDistrict?>(null)
    private val workingUpazila = MutableStateFlow<BdUpazila?>(null)
    private val divisionSearchQuery = MutableStateFlow("")
    private val districtSearchQuery = MutableStateFlow("")
    private val isDetectingGps = MutableStateFlow(false)
    private val gpsError = MutableStateFlow<String?>(null)
    private val confirmationMessage = MutableStateFlow<String?>(null)

    private val selectionFlow = combine(
        workingCountryCode,
        workingDivision,
        workingDistrict,
        workingUpazila
    ) { country, division, district, upazila ->
        WorkingSelection(country, division, district, upazila)
    }

    private val searchAndStatusFlow = combine(
        divisionSearchQuery,
        districtSearchQuery,
        isDetectingGps,
        gpsError,
        confirmationMessage
    ) { divQ, distQ, detecting, err, msg ->
        SearchAndStatus(divQ, distQ, detecting, err, msg)
    }

    val uiState: StateFlow<LocationUiState> = combine(
        prefsRepo.userPreferences,
        selectionFlow,
        searchAndStatusFlow
    ) { prefs, selection, status ->
        val activeCountryCode = selection.countryCode ?: prefs.selectedCountryCode
        val savedDistrict = BangladeshLocationCatalog.findDistrictById(prefs.selectedDistrictId)
            ?: BangladeshLocationCatalog.defaultDistrict
        val activeDivision = selection.division
            ?: BdDivision.fromId(prefs.selectedDivisionId)
            ?: savedDistrict.division
        val activeDistrict = selection.district ?: savedDistrict
        val activeUpazila = selection.upazila ?: activeDistrict.upazilas.firstOrNull {
            it.id == prefs.selectedUpazilaId
        }

        val divQ = status.divisionQuery.trim()
        val filteredDivisions = if (divQ.isEmpty()) {
            BangladeshLocationCatalog.divisions
        } else {
            BangladeshLocationCatalog.divisions.filter {
                it.nameEn.contains(divQ, ignoreCase = true) ||
                    it.nameBn.contains(divQ, ignoreCase = true)
            }
        }

        val distQ = status.districtQuery.trim()
        val filteredDistricts = if (distQ.isEmpty()) {
            locationRepo.getDistrictsForDivision(activeDivision)
        } else {
            // Search across all 64 districts when typing, or prioritize matching districts cleanly
            locationRepo.searchDistricts(distQ, division = null)
        }

        val countryOption = BangladeshLocationCatalog.countries.firstOrNull { it.code == activeCountryCode }
        val intlCities = if (activeCountryCode != BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE && countryOption != null) {
            CityCatalog.internationalCitiesForCountry(countryOption.nameEn)
        } else {
            emptyList()
        }

        LocationUiState(
            currentLocation = prefs.effectiveLocation,
            locationMode = prefs.locationMode,
            selectedCountryCode = activeCountryCode,
            countries = BangladeshLocationCatalog.countries,
            divisions = BangladeshLocationCatalog.divisions,
            selectedDivision = activeDivision,
            divisionSearchQuery = status.divisionQuery,
            districtSearchQuery = status.districtQuery,
            filteredDivisions = filteredDivisions,
            filteredDistricts = filteredDistricts,
            selectedDistrict = activeDistrict,
            selectedUpazila = activeUpazila,
            internationalCities = intlCities,
            isDetectingGps = status.isDetectingGps,
            gpsError = status.gpsError,
            confirmationMessage = status.confirmationMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocationUiState()
    )

    fun selectCountry(countryCode: String) {
        workingCountryCode.value = countryCode
        confirmationMessage.value = null
    }

    fun selectDivision(division: BdDivision) {
        workingCountryCode.value = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE
        workingDivision.value = division
        val firstDistrict = locationRepo.getDistrictsForDivision(division).firstOrNull()
        if (firstDistrict != null) {
            workingDistrict.value = firstDistrict
            workingUpazila.value = null
        }
        confirmationMessage.value = null
    }

    fun selectDistrict(district: BdDistrict) {
        workingCountryCode.value = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE
        workingDivision.value = district.division
        workingDistrict.value = district
        workingUpazila.value = null
        confirmationMessage.value = null
    }

    fun selectUpazila(upazila: BdUpazila?) {
        workingUpazila.value = upazila
        confirmationMessage.value = null
    }

    fun setDivisionSearchQuery(query: String) {
        divisionSearchQuery.value = query
    }

    fun setDistrictSearchQuery(query: String) {
        districtSearchQuery.value = query
    }

    fun confirmBangladeshLocation() {
        val state = uiState.value
        viewModelScope.launch {
            locationRepo.selectBangladeshDistrict(state.selectedDistrict, state.selectedUpazila)
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
            confirmationMessage.value = "${state.selectedDistrict.nameEn}, Bangladesh"
        }
    }

    fun confirmInternationalCity(city: CityLocation, countryCode: String) {
        viewModelScope.launch {
            locationRepo.selectInternationalCity(city, countryCode)
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
            confirmationMessage.value = "${city.nameEn}, ${city.countryEn}"
        }
    }

    fun setManualMode() {
        viewModelScope.launch {
            prefsRepo.setLocationMode(LocationMode.MANUAL)
        }
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            isDetectingGps.value = true
            gpsError.value = null
            confirmationMessage.value = null
            val res = locationHelper.detectCurrentCity()
            res.onSuccess { city ->
                locationRepo.applyDetectedGpsLocation(city)
                val latest = prefsRepo.userPreferences.first()
                alarmScheduler.rescheduleAll(latest)
                workingCountryCode.value = null
                workingDivision.value = null
                workingDistrict.value = null
                workingUpazila.value = null
                confirmationMessage.value = latest.location.nameEn
            }.onFailure { err ->
                gpsError.value = err.message ?: "GPS unavailable"
            }
            isDetectingGps.value = false
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            val defaultLoc = locationRepo.resetToDefault()
            workingCountryCode.value = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE
            workingDivision.value = BdDivision.DHAKA
            workingDistrict.value = BangladeshLocationCatalog.defaultDistrict
            workingUpazila.value = null
            gpsError.value = null
            val latest = prefsRepo.userPreferences.first()
            alarmScheduler.rescheduleAll(latest)
            confirmationMessage.value = defaultLoc.nameEn
        }
    }

    private data class WorkingSelection(
        val countryCode: String?,
        val division: BdDivision?,
        val district: BdDistrict?,
        val upazila: BdUpazila?
    )

    private data class SearchAndStatus(
        val divisionQuery: String,
        val districtQuery: String,
        val isDetectingGps: Boolean,
        val gpsError: String?,
        val confirmationMessage: String?
    )
}
