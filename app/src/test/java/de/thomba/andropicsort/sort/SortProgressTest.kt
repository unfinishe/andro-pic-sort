package de.thomba.andropicsort.sort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SortProgressTest {

    @Test
    fun `processed and total are stored correctly`() {
        val progress = SortProgress(processed = 3, total = 10)
        assertEquals(3, progress.processed)
        assertEquals(10, progress.total)
    }

    @Test
    fun `zero state is valid`() {
        val progress = SortProgress(processed = 0, total = 0)
        assertEquals(0, progress.processed)
        assertEquals(0, progress.total)
    }

    @Test
    fun `processed equals total when complete`() {
        val progress = SortProgress(processed = 100, total = 100)
        assertEquals(progress.processed, progress.total)
    }

    @Test
    fun `data class equality holds`() {
        val a = SortProgress(processed = 5, total = 20)
        val b = SortProgress(processed = 5, total = 20)
        assertEquals(a, b)
    }

    @Test
    fun `initial progress has processed zero`() {
        val initial = SortProgress(processed = 0, total = 50)
        assertFalse("Should not be complete yet", initial.processed == initial.total)
    }

    @Test
    fun `fully processed progress is recognized`() {
        val done = SortProgress(processed = 50, total = 50)
        assertTrue("Should be complete", done.processed == done.total)
    }
}

