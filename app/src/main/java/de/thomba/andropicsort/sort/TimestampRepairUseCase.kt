package de.thomba.andropicsort.sort

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.system.Os
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimestampRepairConfig(
    val rootTreeUri: Uri,
    val customPattern: String?,
    val dryRun: Boolean,
)

data class TimestampRepairProgress(
    val processed: Int,
    val total: Int,
)

data class TimestampRepairReport(
    val processed: Int,
    val repaired: Int,
    val planned: Int,
    val failed: Int,
    val exifSource: Int,
    val fileNameSource: Int,
    val customPatternSource: Int,
    val unchangedNoDate: Int,
    val mtimeApplyFailed: Int,
    val invalidCustomPattern: Boolean,
    val usedPatterns: List<String>,
    val dryRun: Boolean,
    val durationMillis: Long,
)

class TimestampRepairUseCase(
    private val context: Context,
    private val contentResolver: ContentResolver,
) {
    private val exifDateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    suspend fun run(
        config: TimestampRepairConfig,
        onProgress: (TimestampRepairProgress) -> Unit,
    ): TimestampRepairReport {
        val start = System.nanoTime()
        val sourceRoot = DocumentFile.fromTreeUri(context, config.rootTreeUri)
            ?: return TimestampRepairReport(
                processed = 0,
                repaired = 0,
                planned = 0,
                failed = 1,
                exifSource = 0,
                fileNameSource = 0,
                customPatternSource = 0,
                unchangedNoDate = 0,
                mtimeApplyFailed = 0,
                invalidCustomPattern = false,
                usedPatterns = emptyList(),
                dryRun = config.dryRun,
                durationMillis = 0,
            )

        val files = collectFiles(sourceRoot)
        val counters = Counters(dryRun = config.dryRun)
        val custom = TimestampFileNamePatterns.compileCustom(config.customPattern)
        counters.invalidCustomPattern = custom == null && !config.customPattern.isNullOrBlank()

        onProgress(TimestampRepairProgress(0, files.size))

        files.forEachIndexed { index, file ->
            counters.processed += 1
            runCatching {
                val exifDate = readExifDate(file.uri)
                if (exifDate != null) {
                    counters.exifSource += 1
                    applyDate(file, exifDate, config.dryRun, counters)
                } else {
                    val parsed = TimestampFileNamePatterns.parse(file.name, custom)
                    if (parsed == null) {
                        counters.unchangedNoDate += 1
                    } else {
                        counters.fileNameSource += 1
                        if (parsed.patternLabel == TimestampFileNamePatterns.CUSTOM_LABEL) {
                            counters.customPatternSource += 1
                        }
                        counters.usedPatterns[parsed.patternLabel] =
                            (counters.usedPatterns[parsed.patternLabel] ?: 0) + 1
                        applyDate(file, parsed.dateTime, config.dryRun, counters)
                    }
                }
            }.onFailure {
                counters.failed += 1
            }
            onProgress(TimestampRepairProgress(index + 1, files.size))
        }

        val durationMillis = (System.nanoTime() - start) / 1_000_000
        return counters.toReport(durationMillis)
    }

    private fun applyDate(
        file: DocumentFile,
        date: LocalDateTime,
        dryRun: Boolean,
        counters: Counters,
    ) {
        if (dryRun) {
            counters.planned += 1
            return
        }
        val targetMillis = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val realPath = resolveRealPath(file.uri)
        if (realPath == null) {
            counters.failed += 1
            counters.mtimeApplyFailed += 1
            return
        }
        val ok = runCatching { java.io.File(realPath).setLastModified(targetMillis) }.getOrDefault(false)
        if (ok) {
            counters.repaired += 1
        } else {
            counters.failed += 1
            counters.mtimeApplyFailed += 1
        }
    }

    private fun resolveRealPath(uri: Uri): String? {
        return try {
            contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                Os.readlink("/proc/self/fd/${pfd.fd}")
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun collectFiles(root: DocumentFile): List<DocumentFile> {
        val files = mutableListOf<DocumentFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            node.listFiles().forEach { child ->
                if (child.isDirectory) stack.add(child)
                if (child.isFile) files.add(child)
            }
        }
        return files
    }

    private fun readExifDate(uri: Uri): LocalDateTime? {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                if (date.isNullOrBlank()) null else LocalDateTime.parse(date, exifDateFormatter)
            }
        } catch (_: Exception) {
            null
        }
    }

    private class Counters(private val dryRun: Boolean) {
        var processed = 0
        var repaired = 0
        var planned = 0
        var failed = 0
        var exifSource = 0
        var fileNameSource = 0
        var customPatternSource = 0
        var unchangedNoDate = 0
        var mtimeApplyFailed = 0
        var invalidCustomPattern = false
        val usedPatterns = linkedMapOf<String, Int>()

        fun toReport(durationMillis: Long): TimestampRepairReport {
            val patternList = usedPatterns.entries.map { "${it.key}: ${it.value}" }
            return TimestampRepairReport(
                processed = processed,
                repaired = repaired,
                planned = planned,
                failed = failed,
                exifSource = exifSource,
                fileNameSource = fileNameSource,
                customPatternSource = customPatternSource,
                unchangedNoDate = unchangedNoDate,
                mtimeApplyFailed = mtimeApplyFailed,
                invalidCustomPattern = invalidCustomPattern,
                usedPatterns = patternList,
                dryRun = dryRun,
                durationMillis = durationMillis,
            )
        }
    }
}


