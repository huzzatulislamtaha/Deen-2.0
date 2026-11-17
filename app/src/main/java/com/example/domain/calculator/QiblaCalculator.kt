package com.example.domain.calculator

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Pure spherical-trigonometry Qibla bearing & Haversine distance calculator.
 * Kaaba coordinates: 21.422487° N, 39.826206° E (Masjid al-Haram, Makkah).
 */
object QiblaCalculator {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Returns forward azimuth bearing in degrees [0, 360) clockwise from True North.
     */
    fun calculateBearing(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - longitude)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        val deg = Math.toDegrees(atan2(y, x))
        return ((deg % 360.0) + 360.0) % 360.0
    }

    /**
     * Returns Great-Circle Haversine distance to the Kaaba in kilometers.
     */
    fun calculateDistanceKm(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val dPhi = Math.toRadians(KAABA_LATITUDE - latitude)
        val dLambda = Math.toRadians(KAABA_LONGITUDE - longitude)

        val a = sin(dPhi / 2.0).pow(2) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt((1.0 - a).coerceAtLeast(0.0)))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Returns signed shortest angular difference in [-180, +180] from current device heading to Qibla.
     * Positive means turn right (clockwise); negative means turn left (counter-clockwise).
     */
    fun shortestAngularDelta(currentHeadingDeg: Float, qiblaBearingDeg: Double): Float {
        val diff = ((qiblaBearingDeg - currentHeadingDeg + 540.0) % 360.0) - 180.0
        return diff.toFloat()
    }
}
