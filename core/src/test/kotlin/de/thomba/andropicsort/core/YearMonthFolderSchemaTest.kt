package de.thomba.andropicsort.core

import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `formats december with correct zero-padded month`() {
        val (year, month) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2024, 12, 31, 23, 59),
            locale = Locale.ENGLISH,
        )
        assertEquals("2024", year)
        assertEquals("12 December", month)
    }

    @Test
    fun `formats december in german`() {
        val (_, month) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2024, 12, 1, 0, 0),
            locale = Locale.GERMAN,
        )
        assertEquals("12 Dezember", month)
    }

    @Test
    fun `formats all 12 english months with correct numbering`() {
        val expectedMonths = listOf(
            "01 January", "02 February", "03 March", "04 April",
            "05 May", "06 June", "07 July", "08 August",
            "09 September", "10 October", "11 November", "12 December",
        )
        expectedMonths.forEachIndexed { index, expected ->
            val (_, month) = YearMonthFolderSchema.pathFor(
                date = LocalDateTime.of(2024, index + 1, 1, 0, 0),
                locale = Locale.ENGLISH,
            )
            assertEquals(expected, month, "Month ${index + 1} mismatch")
        }
    }

    @Test
    fun `year is formatted as plain string without padding`() {
        val (year, _) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2026, 6, 15, 10, 0),
            locale = Locale.ENGLISH,
        )
        assertEquals("2026", year)
    }

    @Test
    fun `month name starts with uppercase`() {
        val (_, month) = YearMonthFolderSchema.pathFor(
            date = LocalDateTime.of(2024, 7, 4, 0, 0),
            locale = Locale.ENGLISH,
        )
        assertTrue(month[3].isUpperCase(), "Month name should start with uppercase, got: $month")
    }
}

