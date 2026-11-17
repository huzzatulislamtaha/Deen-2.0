package com.example.data.repository

import com.example.domain.model.BdDistrict
import com.example.domain.model.BdDivision
import com.example.domain.model.BdUpazila
import com.example.domain.model.CityLocation
import com.example.domain.model.CountryOption
import com.example.domain.model.LocationMode

class LocationRepository(
    private val prefsRepo: PreferencesRepository
) {
    fun getCountries(): List<CountryOption> = BangladeshLocationCatalog.countries

    fun getDivisions(): List<BdDivision> = BangladeshLocationCatalog.divisions

    fun getDistrictsForDivision(division: BdDivision): List<BdDistrict> =
        BangladeshLocationCatalog.districtsForDivision(division)

    fun getAllDistricts(): List<BdDistrict> = BangladeshLocationCatalog.districts

    fun searchDistricts(query: String, division: BdDivision? = null): List<BdDistrict> {
        val base = if (division != null) {
            BangladeshLocationCatalog.districtsForDivision(division)
        } else {
            BangladeshLocationCatalog.districts
        }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return base
        return base.filter {
            it.nameEn.contains(trimmed, ignoreCase = true) ||
                it.nameBn.contains(trimmed, ignoreCase = true) ||
                it.division.nameEn.contains(trimmed, ignoreCase = true) ||
                it.division.nameBn.contains(trimmed, ignoreCase = true)
        }
    }

    suspend fun selectBangladeshDistrict(
        district: BdDistrict,
        upazila: BdUpazila? = null
    ): CityLocation {
        val location = BangladeshLocationCatalog.toCityLocation(district, upazila, isGpsDetected = false)
        prefsRepo.setLocation(
            city = location,
            mode = LocationMode.MANUAL,
            countryCode = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE,
            divisionId = district.division.id,
            districtId = district.id,
            upazilaId = upazila?.id ?: "",
            syncTimezoneIfAutomaticOrLocationDefault = true
        )
        return location
    }

    suspend fun selectInternationalCity(city: CityLocation, countryCode: String) {
        prefsRepo.setLocation(
            city = city.copy(isGpsDetected = false, isReferenceCoordinate = true),
            mode = LocationMode.MANUAL,
            countryCode = countryCode,
            divisionId = "",
            districtId = "",
            upazilaId = "",
            syncTimezoneIfAutomaticOrLocationDefault = true
        )
    }

    suspend fun applyDetectedGpsLocation(city: CityLocation) {
        val nearestDistrict = BangladeshLocationCatalog.findNearestDistrict(city.latitude, city.longitude)
        if (nearestDistrict != null) {
            val enriched = city.copy(
                countryEn = "Bangladesh",
                countryBn = "বাংলাদেশ",
                divisionNameEn = nearestDistrict.division.nameEn,
                divisionNameBn = nearestDistrict.division.nameBn,
                timeZoneId = BangladeshLocationCatalog.BANGLADESH_TIMEZONE_ID,
                isGpsDetected = true,
                isReferenceCoordinate = false
            )
            prefsRepo.setLocation(
                city = enriched,
                mode = LocationMode.AUTOMATIC,
                countryCode = BangladeshLocationCatalog.BANGLADESH_COUNTRY_CODE,
                divisionId = nearestDistrict.division.id,
                districtId = nearestDistrict.id,
                upazilaId = "",
                syncTimezoneIfAutomaticOrLocationDefault = true
            )
        } else {
            prefsRepo.setLocation(
                city = city.copy(isGpsDetected = true, isReferenceCoordinate = false),
                mode = LocationMode.AUTOMATIC,
                syncTimezoneIfAutomaticOrLocationDefault = true
            )
        }
    }

    suspend fun resetToDefault(): CityLocation {
        return selectBangladeshDistrict(BangladeshLocationCatalog.defaultDistrict, null)
    }
}
