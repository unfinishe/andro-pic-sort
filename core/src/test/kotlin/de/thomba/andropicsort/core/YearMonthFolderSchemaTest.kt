package de.thomba.andropicsort.core

import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class YearMonthFolderSchemaTest {
    @Test
    fun `uses german month names for german locale`() {
        val (year, month) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2024, 1, 15, 12, 0),
            locale = Locale.GERMAN,
        )

        assertEquals("2024", year)
        assertEquals("01 Januar", month)
    }

    @Test
    fun `uses english fallback locale for non german`() {
        val locale = AppLocalePolicy.effectiveLocale(Locale.FRENCH)
        val (_, month) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2024, 3, 10, 8, 30),
            locale = locale,
        )

        assertEquals(Locale.ENGLISH, locale)
        assertEquals("03 March", month)
    }
}

