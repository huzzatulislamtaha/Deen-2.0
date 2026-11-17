package com.example

import com.example.data.content.VerifiedQuranContent
import com.example.data.repository.BangladeshLocationCatalog
import com.example.data.repository.CityCatalog
import com.example.domain.calculator.PrayerTimesCalculator
import com.example.domain.calculator.QiblaCalculator
import com.example.domain.model.BdDivision
import com.example.domain.model.CalculationMethod
import com.example.domain.model.DeenFontConfig
import com.example.domain.model.FontScalePreset
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerName
import com.example.ui.viewmodel.WorshipViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class BangladeshLocationAndTimezoneTest {

    @Test
    fun `bangladesh catalog contains all 8 divisions and exact 64 districts without duplicates`() {
        assertEquals(8, BangladeshLocationCatalog.divisions.size)
        assertEquals(64, BangladeshLocationCatalog.districts.size)

        val expectedDivisionCounts = mapOf(
            BdDivision.DHAKA to 13,
            BdDivision.CHATTOGRAM to 11,
            BdDivision.RAJSHAHI to 8,
            BdDivision.KHULNA to 10,
            BdDivision.BARISHAL to 6,
            BdDivision.SYLHET to 4,
            BdDivision.RANGPUR to 8,
            BdDivision.MYMENSINGH to 4
        )
        expectedDivisionCounts.forEach { (division, expectedCount) ->
            val actual = BangladeshLocationCatalog.districtsForDivision(division)
            assertEquals("Wrong count for ${division.nameEn}", expectedCount, actual.size)
        }

        // Uniqueness checks
        assertEquals(64, BangladeshLocationCatalog.districts.map { it.id }.toSet().size)
        assertEquals(64, BangladeshLocationCatalog.districts.map { it.nameEn }.toSet().size)
        assertEquals(64, BangladeshLocationCatalog.districts.map { it.nameBn }.toSet().size)

        // All 64 districts sit inside valid Bangladesh geography and use Asia/Dhaka
        BangladeshLocationCatalog.districts.forEach { d ->
            assertTrue("Latitude out of BD bounds for ${d.nameEn}", d.latitude in 20.5..26.7)
            assertTrue("Longitude out of BD bounds for ${d.nameEn}", d.longitude in 88.0..92.8)
            assertTrue(d.nameEn.isNotBlank())
            assertTrue(d.nameBn.isNotBlank())
            val city = BangladeshLocationCatalog.toCityLocation(d)
            assertEquals("Asia/Dhaka", city.timeZoneId)
            assertTrue(city.isReferenceCoordinate)
        }
    }

    @Test
    fun `gps nearest district finder resolves major bangladesh districts and rejects outside coordinates`() {
        val nearChattogram = BangladeshLocationCatalog.findNearestDistrict(22.34, 91.81)
        assertEquals("dist_chattogram", nearChattogram?.id)

        val nearSylhet = BangladeshLocationCatalog.findNearestDistrict(24.90, 91.87)
        assertEquals("dist_sylhet", nearSylhet?.id)

        val nearRangpur = BangladeshLocationCatalog.findNearestDistrict(25.75, 89.26)
        assertEquals("dist_rangpur", nearRangpur?.id)

        val londonOutsideBd = BangladeshLocationCatalog.findNearestDistrict(51.5072, -0.1276)
        assertNull(londonOutsideBd)
    }

    @Test
    fun `city catalog exposes all 64 bangladesh districts plus international cities`() {
        val all = CityCatalog.cities
        assertTrue(all.size >= 64)
        assertNotNull(all.firstOrNull { it.id == "dist_coxs_bazar" })
        assertNotNull(all.firstOrNull { it.id == "dist_sunamganj" })
        assertNotNull(all.firstOrNull { it.id == "london" })
        assertNotNull(all.firstOrNull { it.id == "makkah" })
    }

    @Test
    fun `bangladesh districts produce chronological prayer times and valid qibla bearings`() {
        val sampleDistrictIds = listOf("dist_dhaka", "dist_chattogram", "dist_sylhet", "dist_rangpur", "dist_khulna")
        sampleDistrictIds.forEach { id ->
            val district = BangladeshLocationCatalog.findDistrictById(id)!!
            val loc = BangladeshLocationCatalog.toCityLocation(district)
            val schedule = PrayerTimesCalculator.calculateDaySchedule(
                2026, 11, 17, loc, CalculationMethod.KARACHI, Madhab.HANAFI
            )
            val times = schedule.entries.associateBy { it.prayer }
            assertTrue(times[PrayerName.FAJR]!!.timeMillis < times[PrayerName.SUNRISE]!!.timeMillis)
            assertTrue(times[PrayerName.SUNRISE]!!.timeMillis < times[PrayerName.DHUHR]!!.timeMillis)
            assertTrue(times[PrayerName.DHUHR]!!.timeMillis < times[PrayerName.ASR]!!.timeMillis)
            assertTrue(times[PrayerName.ASR]!!.timeMillis < times[PrayerName.MAGHRIB]!!.timeMillis)
            assertTrue(times[PrayerName.MAGHRIB]!!.timeMillis < times[PrayerName.ISHA]!!.timeMillis)

            val qibla = QiblaCalculator.calculateBearing(loc.latitude, loc.longitude)
            assertTrue("Qibla bearing for $id should face West (~270-285 deg), was $qibla", qibla in 268.0..286.0)
        }
    }

    @Test
    fun `timezone offsets dst and date boundaries behave consistently`() {
        val dhakaTz = TimeZone.getTimeZone("Asia/Dhaka")
        assertEquals(6 * 3600 * 1000, dhakaTz.getOffset(System.currentTimeMillis()))

        val londonTz = TimeZone.getTimeZone("Europe/London")
        val janCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JANUARY, 15, 12, 0, 0)
        }
        val julCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JULY, 15, 12, 0, 0)
        }
        assertEquals(0, londonTz.getOffset(janCal.timeInMillis))
        assertEquals(3600 * 1000, londonTz.getOffset(julCal.timeInMillis))

        // Worship date key uses requested timezone rather than assuming device default
        val dhakaDate = WorshipViewModel.todayDateKey("Asia/Dhaka")
        assertTrue(dhakaDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `font config effective scaling clamps safely across presets`() {
        val small = DeenFontConfig(appScalePreset = FontScalePreset.SMALL, arabicFontScale = 1.0f)
        val xl = DeenFontConfig(appScalePreset = FontScalePreset.EXTRA_LARGE, arabicFontScale = 1.50f)
        assertTrue(small.effectiveEnglishScale < 1.0f)
        assertTrue(xl.effectiveArabicScale <= 1.95f)
        assertTrue(xl.effectiveArabicScale > small.effectiveArabicScale)
    }

    @Test
    fun `verified quran excerpts have arabic bengali english and quran com sources`() {
        assertTrue(VerifiedQuranContent.excerpts.size >= 5)
        VerifiedQuranContent.excerpts.forEach { q ->
            assertTrue(q.arabicText.isNotBlank())
            assertTrue(q.bengaliTranslation.isNotBlank())
            assertTrue(q.englishTranslation.isNotBlank())
            assertTrue(q.sourceUrl.startsWith("https://quran.com/"))
        }
    }
}
