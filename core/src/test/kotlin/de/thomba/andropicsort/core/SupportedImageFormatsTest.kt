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
    fun `accepts all declared formats`() {
        SupportedImageFormats.extensions.forEach { ext ->
            assertTrue(
                SupportedImageFormats.isSupported("file.$ext"),
                "Expected $ext to be supported",
            )
        }
    }

    @Test
    fun `accepts uppercase extensions`() {
        assertTrue(SupportedImageFormats.isSupported("IMG_001.JPG"))
        assertTrue(SupportedImageFormats.isSupported("photo.HEIC"))
        assertTrue(SupportedImageFormats.isSupported("scan.PNG"))
    }

    @Test
    fun `accepts mixed case extensions`() {
        assertTrue(SupportedImageFormats.isSupported("photo.Jpg"))
        assertTrue(SupportedImageFormats.isSupported("raw.DnG"))
    }

    @Test
    fun `rejects unsupported extension`() {
        assertFalse(SupportedImageFormats.isSupported("video.mp4"))
        assertFalse(SupportedImageFormats.isSupported("document.pdf"))
        assertFalse(SupportedImageFormats.isSupported("archive.zip"))
    }

    @Test
    fun `rejects filename without extension`() {
        assertFalse(SupportedImageFormats.isSupported("README"))
        assertFalse(SupportedImageFormats.isSupported("Makefile"))
    }

    @Test
    fun `rejects null filename`() {
        assertFalse(SupportedImageFormats.isSupported(null))
    }

    @Test
    fun `rejects blank filename`() {
        assertFalse(SupportedImageFormats.isSupported(""))
        assertFalse(SupportedImageFormats.isSupported("   "))
    }
}

