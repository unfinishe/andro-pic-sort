package de.thomba.andropicsort.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputFilePolicyTest {
    @Test
    fun `includes only images by default`() {
        assertTrue(InputFilePolicy.shouldInclude("photo.jpg", includeNonImages = false))
        assertFalse(InputFilePolicy.shouldInclude("clip.mp4", includeNonImages = false))
    }

    @Test
    fun `includes non images when option is enabled`() {
        assertTrue(InputFilePolicy.shouldInclude("clip.mp4", includeNonImages = true))
        assertTrue(InputFilePolicy.shouldInclude("note.txt", includeNonImages = true))
    }

    @Test
    fun `forces file date mode for non images`() {
        val mode = InputFilePolicy.effectiveDateSourceMode(
            fileName = "clip.mp4",
            configuredMode = DateSourceMode.METADATA_THEN_FILE,
            includeNonImages = true,
        )

        assertEquals(DateSourceMode.FILE_ONLY, mode)
    }

    @Test
    fun `keeps configured mode for images`() {
        val mode = InputFilePolicy.effectiveDateSourceMode(
            fileName = "photo.heic",
            configuredMode = DateSourceMode.METADATA_THEN_FILE,
            includeNonImages = true,
        )

        assertEquals(DateSourceMode.METADATA_THEN_FILE, mode)
    }
}

