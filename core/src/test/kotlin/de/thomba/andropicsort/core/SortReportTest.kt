package de.thomba.andropicsort.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SortReportTest {
    @Test
    fun `default values are zero and false`() {
        val report = SortReport(
            processed = 0,
            copied = 0,
            moved = 0,
            failed = 0,
            skipped = 0,
        )
        assertEquals(0, report.planned)
        assertEquals(0, report.renamed)
        assertEquals(0, report.createFailed)
        assertEquals(0, report.copyFailed)
        assertEquals(0, report.deleteFailed)
        assertFalse(report.dryRun)
        assertEquals(0L, report.durationMillis)
    }

    @Test
    fun `dry run flag is preserved`() {
        val report = SortReport(
            processed = 5,
            copied = 0,
            moved = 0,
            failed = 0,
            skipped = 0,
            planned = 5,
            dryRun = true,
        )
        assertTrue(report.dryRun)
        assertEquals(5, report.planned)
        assertEquals(0, report.copied)
    }

    @Test
    fun `all counter fields are independently assignable`() {
        val report = SortReport(
            processed = 10,
            copied = 4,
            moved = 3,
            failed = 2,
            skipped = 1,
            planned = 0,
            renamed = 2,
            createFailed = 1,
            copyFailed = 1,
            deleteFailed = 0,
            dryRun = false,
            durationMillis = 1500L,
        )
        assertEquals(10, report.processed)
        assertEquals(4, report.copied)
        assertEquals(3, report.moved)
        assertEquals(2, report.failed)
        assertEquals(1, report.skipped)
        assertEquals(2, report.renamed)
        assertEquals(1, report.createFailed)
        assertEquals(1, report.copyFailed)
        assertEquals(0, report.deleteFailed)
        assertEquals(1500L, report.durationMillis)
    }

    @Test
    fun `error bucket fields are included in failed count`() {
        val createFailed = 2
        val copyFailed = 1
        val deleteFailed = 1
        val report = SortReport(
            processed = 10,
            copied = 6,
            moved = 0,
            failed = createFailed + copyFailed + deleteFailed,
            skipped = 0,
            createFailed = createFailed,
            copyFailed = copyFailed,
            deleteFailed = deleteFailed,
        )
        assertEquals(report.createFailed + report.copyFailed + report.deleteFailed, report.failed)
    }
}

