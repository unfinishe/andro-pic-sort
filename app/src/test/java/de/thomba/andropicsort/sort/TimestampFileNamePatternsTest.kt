package de.thomba.andropicsort.sort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimestampFileNamePatternsTest {

    @Test
    fun `parse uses built-in IMG pattern`() {
        val parsed = TimestampFileNamePatterns.parse("IMG_20240419_183025.jpg", customPattern = null)
        assertNotNull(parsed)
        assertEquals("IMG_yyyyMMdd_HHmmss", parsed?.patternLabel)
        assertEquals(2024, parsed?.dateTime?.year)
        assertEquals(4, parsed?.dateTime?.monthValue)
        assertEquals(19, parsed?.dateTime?.dayOfMonth)
        assertEquals(18, parsed?.dateTime?.hour)
        assertEquals(30, parsed?.dateTime?.minute)
        assertEquals(25, parsed?.dateTime?.second)
    }

    @Test
    fun `parse supports date-only fallback with midnight`() {
        val parsed = TimestampFileNamePatterns.parse("WA_20230214-Chat.jpg", customPattern = null)
        assertNotNull(parsed)
        assertEquals("yyyyMMdd (date only)", parsed?.patternLabel)
        assertEquals(0, parsed?.dateTime?.hour)
        assertEquals(0, parsed?.dateTime?.minute)
        assertEquals(0, parsed?.dateTime?.second)
    }

    @Test
    fun `parse uses custom pattern when no built-in matches`() {
        val custom = TimestampFileNamePatterns.compileCustom("""foo_(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})_(?<hour>\d{2})(?<minute>\d{2})(?<second>\d{2})""")
        val parsed = TimestampFileNamePatterns.parse("foo_2025-01-31_235959.png", custom)
        assertNotNull(parsed)
        assertEquals(TimestampFileNamePatterns.CUSTOM_LABEL, parsed?.patternLabel)
        assertEquals(2025, parsed?.dateTime?.year)
        assertEquals(1, parsed?.dateTime?.monthValue)
        assertEquals(31, parsed?.dateTime?.dayOfMonth)
    }

    @Test
    fun `parse returns null for unsupported names`() {
        val parsed = TimestampFileNamePatterns.parse("holiday_photo.jpg", customPattern = null)
        assertNull(parsed)
    }

    @Test
    fun `compileCustom returns null for invalid regex`() {
        val compiled = TimestampFileNamePatterns.compileCustom("(invalid")
        assertNull(compiled)
    }

    @Test
    fun `availablePatternDetails appends custom pattern detail`() {
        val details = TimestampFileNamePatterns.availablePatternDetails(
            "(?<year>\\d{4})(?<month>\\d{2})(?<day>\\d{2})",
        )

        assertEquals(TimestampFileNamePatterns.CUSTOM_LABEL, details.last().label)
        assertEquals("(?<year>\\d{4})(?<month>\\d{2})(?<day>\\d{2})", details.last().example)
    }

    @Test
    fun `availablePatternDetails exposes built in examples`() {
        val details = TimestampFileNamePatterns.availablePatternDetails(customPattern = null)

        assertTrue(details.isNotEmpty())
        assertEquals("IMG_yyyyMMdd_HHmmss", details.first().label)
        assertTrue(details.first().example.contains("IMG_20240419_183025"))
    }

    @Test
    fun `previewCustomPattern returns extracted datetime for matching filename`() {
        val preview = TimestampFileNamePatterns.previewCustomPattern(
            fileName = "foo_2025-01-31_235959.png",
            customPattern = """foo_(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})_(?<hour>\d{2})(?<minute>\d{2})(?<second>\d{2})""",
        )
        assertEquals(true, preview.patternValid)
        assertNotNull(preview.extracted)
        assertEquals(2025, preview.extracted?.dateTime?.year)
        assertEquals(1, preview.extracted?.dateTime?.monthValue)
        assertEquals(31, preview.extracted?.dateTime?.dayOfMonth)
    }

    @Test
    fun `previewCustomPattern marks invalid regex`() {
        val preview = TimestampFileNamePatterns.previewCustomPattern(
            fileName = "IMG_20240419_183025.jpg",
            customPattern = "(invalid",
        )
        assertEquals(false, preview.patternValid)
        assertNull(preview.extracted)
    }

    @Test
    fun `previewBuiltInPattern returns first matching built-in pattern`() {
        val preview = TimestampFileNamePatterns.previewBuiltInPattern("IMG_20240419_183025.jpg")
        assertNotNull(preview)
        assertEquals("IMG_yyyyMMdd_HHmmss", preview?.patternLabel)
        assertEquals(2024, preview?.dateTime?.year)
        assertEquals(4, preview?.dateTime?.monthValue)
        assertEquals(19, preview?.dateTime?.dayOfMonth)
    }

    @Test
    fun `previewBuiltInPattern returns null when no built-in pattern matches`() {
        val preview = TimestampFileNamePatterns.previewBuiltInPattern("my_random_file_name.jpg")
        assertNull(preview)
    }
}


