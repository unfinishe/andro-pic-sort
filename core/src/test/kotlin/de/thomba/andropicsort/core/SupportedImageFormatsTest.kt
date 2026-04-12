package de.thomba.andropicsort.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportedImageFormatsTest {
    @Test
    fun `accepts typical android camera formats`() {
        assertTrue(SupportedImageFormats.isSupported("x.jpg"))
        assertTrue(SupportedImageFormats.isSupported("x.heic"))
        assertTrue(SupportedImageFormats.isSupported("x.heif"))
        assertTrue(SupportedImageFormats.isSupported("x.dng"))
    }

    @Test
    fun `rejects unsupported or missing extension`() {
        assertFalse(SupportedImageFormats.isSupported("video.mp4"))
        assertFalse(SupportedImageFormats.isSupported("README"))
    }
}

