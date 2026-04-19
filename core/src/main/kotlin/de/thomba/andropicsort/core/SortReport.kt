package de.thomba.andropicsort.core

data class SortReport(
    val processed: Int,
    val copied: Int,
    val moved: Int,
    val failed: Int,
    val skipped: Int,
    val planned: Int = 0,
    val renamed: Int = 0,
    val createFailed: Int = 0,
    val copyFailed: Int = 0,
    val deleteFailed: Int = 0,
    /**
     * Files copied/moved using OS-level [java.nio.file.Files.copy] with [java.nio.file.StandardCopyOption.COPY_ATTRIBUTES].
     * All file-system attributes the OS can preserve (mtime, atime, permissions) are retained automatically.
     * This is the primary/default strategy.
     */
    val osCopyUsed: Int = 0,
    /**
     * Files that fell back to stream copy because real on-disk paths could not be resolved via /proc/self/fd.
     * For these files a manual [java.io.File.setLastModified] is attempted to at least restore mtime.
     */
    val streamFallbackUsed: Int = 0,
    /**
     * Files (stream-fallback only) where mtime was successfully restored via [java.io.File.setLastModified].
     * Note: file-system creation time (btime) cannot be set on Android/Linux — the kernel provides
     * no write syscall for btime. EXIF capture dates are unaffected; they are part of the file
     * content and are copied verbatim.
     */
    val timestampPreserved: Int = 0,
    /** Files (stream-fallback only) where even mtime restoration failed. */
    val timestampFailed: Int = 0,
    val dryRun: Boolean = false,
    val durationMillis: Long = 0,
)
