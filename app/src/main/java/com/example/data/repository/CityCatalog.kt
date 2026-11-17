package com.example.data.repository

import com.example.domain.model.CityLocation

object CityCatalog {
    val defaultCity: CityLocation =
        BangladeshLocationCatalog.toCityLocation(BangladeshLocationCatalog.defaultDistrict)

    val internationalCities: List<CityLocation> = listOf(
        CityLocation("makkah", "Makkah", "মক্কা", "Saudi Arabia", "সৌদি আরব", 21.4225, 39.8262, "Asia/Riyadh"),
        CityLocation("madinah", "Madinah", "মদীনা", "Saudi Arabia", "সৌদি আরব", 24.4672, 39.6111, "Asia/Riyadh"),
        CityLocation("london", "London", "লন্ডন", "United Kingdom", "যুক্তরাজ্য", 51.5072, -0.1276, "Europe/London"),
        CityLocation("new_york", "New York", "নিউ ইয়র্ক", "United States", "যুক্তরাষ্ট্র", 40.7128, -74.0060, "America/New_York"),
        CityLocation("toronto", "Toronto", "টরন্টো", "Canada", "কানাডা", 43.6532, -79.3832, "America/Toronto"),
        CityLocation("kuala_lumpur", "Kuala Lumpur", "কুয়ালালামপুর", "Malaysia", "মালয়েশিয়া", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityLocation("jakarta", "Jakarta", "জাকার্তা", "Indonesia", "ইন্দোনেশিয়া", -6.2088, 106.8456, "Asia/Jakarta"),
        CityLocation("istanbul", "Istanbul", "ইস্তাম্বুল", "Turkey", "তুরস্ক", 41.0082, 28.9784, "Europe/Istanbul"),
        CityLocation("cairo", "Cairo", "কায়রো", "Egypt", "মিশর", 30.0444, 31.2357, "Africa/Cairo"),
        CityLocation("dubai", "Dubai", "দুবাই", "UAE", "সংযুক্ত আরব আমিরাত", 25.2048, 55.2708, "Asia/Dubai")
    )

    val cities: List<CityLocation> =
        BangladeshLocationCatalog.districts.map { BangladeshLocationCatalog.toCityLocation(it) } + internationalCities

    fun internationalCitiesForCountry(countryEn: String): List<CityLocation> =
        internationalCities.filter { it.countryEn.equals(countryEn, ignoreCase = true) }
}
