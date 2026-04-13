package de.thomba.andropicsort.core

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLocalePolicyTest {
    @Test
    fun `returns German locale for German system locale`() {
        assertEquals(Locale.GERMAN, AppLocalePolicy.effectiveLocale(Locale.GERMAN))
    }

    @Test
    fun `returns German locale for de_DE variant`() {
        assertEquals(Locale.GERMAN, AppLocalePolicy.effectiveLocale(Locale("de", "DE")))
    }

    @Test
    fun `returns German locale for de_AT variant`() {
        assertEquals(Locale.GERMAN, AppLocalePolicy.effectiveLocale(Locale("de", "AT")))
    }

    @Test
    fun `returns English fallback for French locale`() {
        assertEquals(Locale.ENGLISH, AppLocalePolicy.effectiveLocale(Locale.FRENCH))
    }

    @Test
    fun `returns English fallback for Japanese locale`() {
        assertEquals(Locale.ENGLISH, AppLocalePolicy.effectiveLocale(Locale.JAPANESE))
    }

    @Test
    fun `returns English fallback for root locale`() {
        assertEquals(Locale.ENGLISH, AppLocalePolicy.effectiveLocale(Locale.ROOT))
    }
}

