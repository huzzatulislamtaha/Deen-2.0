package com.example

import com.example.core.i18n.BengaliStrings
import com.example.core.i18n.EnglishStrings
import com.example.data.content.VerifiedIslamicContent
import com.example.domain.model.DuaCategory
import com.example.domain.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentIntegrityTest {

    @Test
    fun `all hadiths have unique ids complete text and sunnah sources`() {
        val hadiths = VerifiedIslamicContent.hadiths
        assertTrue(hadiths.isNotEmpty())
        assertEquals(hadiths.size, hadiths.map { it.id }.toSet().size)

        hadiths.forEach { h ->
            assertTrue("Missing Arabic for ${h.id}", h.arabicText.isNotBlank())
            assertTrue("Missing Bengali for ${h.id}", h.bengaliMeaning.isNotBlank())
            assertTrue("Missing English for ${h.id}", h.englishMeaning.isNotBlank())
            assertTrue("Missing reference for ${h.id}", h.reference.isNotBlank())
            assertTrue("Missing grade for ${h.id}", h.authenticityGrade.isNotBlank())
            assertTrue("Invalid URL for ${h.id}", h.sourceUrl.startsWith("https://sunnah.com/"))
            assertEquals(VerificationStatus.VERIFIED, h.verificationStatus)
        }
    }

    @Test
    fun `all duas cover all categories have unique ids pronunciation and sources`() {
        val duas = VerifiedIslamicContent.duas
        assertTrue(duas.isNotEmpty())
        assertEquals(duas.size, duas.map { it.id }.toSet().size)

        val coveredCategories = duas.map { it.category }.toSet()
        assertEquals(DuaCategory.entries.toSet(), coveredCategories)

        duas.forEach { d ->
            assertTrue("Missing Arabic for ${d.id}", d.arabicText.isNotBlank())
            assertTrue("Missing Bengali pronunciation for ${d.id}", d.bengaliPronunciation.isNotBlank())
            assertTrue("Missing Bengali meaning for ${d.id}", d.bengaliMeaning.isNotBlank())
            assertTrue("Missing English meaning for ${d.id}", d.englishMeaning.isNotBlank())
            assertTrue("Missing reference for ${d.id}", d.reference.isNotBlank())
            assertTrue("Repetition must be >= 1 for ${d.id}", d.repetitionCount >= 1)
            assertTrue("Invalid URL for ${d.id}", d.sourceUrl.startsWith("https://sunnah.com/"))
            assertEquals(VerificationStatus.VERIFIED, d.verificationStatus)
        }
    }

    @Test
    fun `english and bengali localization catalogs are complete`() {
        assertEquals(12, EnglishStrings.hijriMonths.size)
        assertEquals(12, BengaliStrings.hijriMonths.size)
        assertTrue(EnglishStrings.hijriMonths.all { it.isNotBlank() })
        assertTrue(BengaliStrings.hijriMonths.all { it.isNotBlank() })
        assertTrue(BengaliStrings.prayerTimetableTitle.isNotBlank())
        assertTrue(BengaliStrings.qiblaTitle.isNotBlank())
        assertTrue(BengaliStrings.settingsTitle.isNotBlank())
    }
}
