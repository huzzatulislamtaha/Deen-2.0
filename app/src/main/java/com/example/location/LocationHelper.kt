package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.repository.BangladeshLocationCatalog
import com.example.domain.model.CityLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentCity(): Result<CityLocation> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location: Location = suspendCancellableCoroutine<Location?> { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        client.lastLocation
                            .addOnSuccessListener { last -> cont.resume(last) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    client.lastLocation
                        .addOnSuccessListener { last -> cont.resume(last) }
                        .addOnFailureListener { cont.resume(null) }
                }
        } ?: return Result.failure(IllegalStateException("Unable to acquire device coordinates"))

        val nearestBd = BangladeshLocationCatalog.findNearestDistrict(location.latitude, location.longitude)
        if (nearestBd != null) {
            return Result.success(
                CityLocation(
                    id = "gps_${nearestBd.id}",
                    nameEn = "${nearestBd.nameEn} (GPS)",
                    nameBn = "${nearestBd.nameBn} (জিপিএস)",
                    countryEn = "Bangladesh",
                    countryBn = "বাংলাদেশ",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timeZoneId = BangladeshLocationCatalog.BANGLADESH_TIMEZONE_ID,
                    isGpsDetected = true,
                    divisionNameEn = nearestBd.division.nameEn,
                    divisionNameBn = nearestBd.division.nameBn,
                    isReferenceCoordinate = false
                )
            )
        }

        val (cityLabel, countryLabel) = reverseGeocode(location.latitude, location.longitude)
        val tzId = TimeZone.getDefault().id
        return Result.success(
            CityLocation(
                id = "gps_${String.format(Locale.US, "%.3f_%.3f", location.latitude, location.longitude)}",
                nameEn = cityLabel,
                nameBn = cityLabel,
                countryEn = countryLabel,
                countryBn = countryLabel,
                latitude = location.latitude,
                longitude = location.longitude,
                timeZoneId = tzId,
                isGpsDetected = true,
                isReferenceCoordinate = false
            )
        )
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): Pair<String, String> =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) {
                    return@withContext formatCoordsLabel(lat, lng) to ""
                }
                val geocoder = Geocoder(context, Locale.getDefault())
                val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            cont.resume(addresses.firstOrNull())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                }
                val locality = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                    ?: formatCoordsLabel(lat, lng)
                val country = address?.countryName ?: ""
                locality to country
            } catch (_: Exception) {
                formatCoordsLabel(lat, lng) to ""
            }
        }

    private fun formatCoordsLabel(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.2f°, %.2f°", lat, lng)
}
