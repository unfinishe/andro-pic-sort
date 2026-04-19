package de.thomba.andropicsort.sort

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.system.Os
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.ConflictResolver
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.InputFilePolicy
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.core.SortReport
import de.thomba.andropicsort.core.YearMonthFolderSchema
import kotlinx.coroutines.ensureActive
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.coroutineContext

class AndroidSortUseCase(
    private val context: Context,
    private val contentResolver: ContentResolver,
) : SortUseCase {

    // ── Internal helper types ───────────────────────────────────────────

    private data class SortCandidate(
        val file: DocumentFile,
        val dateSourceMode: DateSourceMode,
    )

    private sealed interface TargetPlanResult {
        data class Success(val plan: TargetPlan) : TargetPlanResult
        data class Failure(val deleteConflictFailed: Boolean = false) : TargetPlanResult
    }

    private data class TargetPlan(
        val targetFile: DocumentFile,
        val wasRenamed: Boolean,
    )

    private data class TargetFolderState(
        val folder: DocumentFile,
        val existingNames: MutableSet<String>,
    )

    /** P7 – Mutable accumulator replacing 10 loose counter variables. */
    private class Counters {
        var processed = 0
        var copied = 0
        var moved = 0
        var failed = 0
        var skipped = 0
        var planned = 0
        var renamed = 0
        var createFailed = 0
        var copyFailed = 0
        var deleteFailed = 0
        var osCopyUsed = 0
        var streamFallbackUsed = 0
        var timestampPreserved = 0
        var timestampFailed = 0

        fun toReport(dryRun: Boolean, durationMillis: Long) = SortReport(
            processed = processed,
            copied = copied,
            moved = moved,
            failed = failed,
            skipped = skipped,
            planned = planned,
            renamed = renamed,
            createFailed = createFailed,
            copyFailed = copyFailed,
            deleteFailed = deleteFailed,
            osCopyUsed = osCopyUsed,
            streamFallbackUsed = streamFallbackUsed,
            timestampPreserved = timestampPreserved,
            timestampFailed = timestampFailed,
            dryRun = dryRun,
            durationMillis = durationMillis,
        )
    }

    private val exifDateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    // ── Public API ──────────────────────────────────────────────────────

    override suspend fun run(config: SortConfig, onProgress: (SortProgress) -> Unit): SortReport {
        val startTime = System.nanoTime()

        // P1 — Source ≠ Target validation
        if (config.sourceTreeUri == config.targetTreeUri) {
            return errorReport(config.dryRun)
        }

        val sourceRoot = DocumentFile.fromTreeUri(context, config.sourceTreeUri)
        val targetRoot = DocumentFile.fromTreeUri(context, config.targetTreeUri)

        if (sourceRoot == null || !sourceRoot.isDirectory || targetRoot == null || !targetRoot.isDirectory) {
            return SortReport(processed = 0, copied = 0, moved = 0, failed = 1, skipped = 0, dryRun = config.dryRun)
        }

        val files = collectCandidateFiles(sourceRoot, config)
        val yearDirCache = mutableMapOf<String, DocumentFile?>()
        val monthStateCache = mutableMapOf<String, TargetFolderState?>()
        val c = Counters()

        // P6 — Progress throttling state
        var lastProgressTime = System.nanoTime()
        val progressIntervalNanos = 100_000_000L // 100 ms

        onProgress(SortProgress(processed = 0, total = files.size))

        for (candidate in files) {
            // P2 — Cancellation support
            coroutineContext.ensureActive()

            val sourceFile = candidate.file
            c.processed += 1
            try {
                val fileName = sourceFile.name
                if (fileName.isNullOrBlank()) {
                    c.skipped += 1
                    lastProgressTime = emitThrottled(c, files.size, onProgress, lastProgressTime, progressIntervalNanos)
                    continue
                }

                val sourceDate = resolveDate(sourceFile, candidate.dateSourceMode)
                val (yearFolder, monthFolder) = YearMonthFolderSchema.pathFor(sourceDate, config.locale)
                val monthState = resolveMonthState(
                    targetRoot, yearFolder, monthFolder,
                    createIfMissing = !config.dryRun,
                    yearDirCache, monthStateCache,
                )

                // P7 — Separated dry-run path
                if (config.dryRun) {
                    processDryRun(fileName, monthState, config.conflictPolicy, c)
                    lastProgressTime = emitThrottled(c, files.size, onProgress, lastProgressTime, progressIntervalNanos)
                    continue
                }

                if (monthState == null) {
                    c.failed += 1; c.createFailed += 1
                    lastProgressTime = emitThrottled(c, files.size, onProgress, lastProgressTime, progressIntervalNanos)
                    continue
                }

                // P7 — Separated real-file path
                processRealFile(sourceFile, fileName, monthState, config, c)
            } catch (_: Exception) {
                c.failed += 1
            }

            lastProgressTime = emitThrottled(c, files.size, onProgress, lastProgressTime, progressIntervalNanos)
        }

        // Always emit final progress
        onProgress(SortProgress(processed = c.processed, total = files.size))

        // P8 — Duration measurement
        val durationMillis = (System.nanoTime() - startTime) / 1_000_000
        return c.toReport(config.dryRun, durationMillis)
    }

    // ── Per-file processing (P7) ────────────────────────────────────────

    private fun processDryRun(
        fileName: String,
        monthState: TargetFolderState?,
        conflictPolicy: ConflictPolicy,
        c: Counters,
    ) {
        val existingNames = monthState?.existingNames ?: emptySet<String>()
        val plannedName = when (conflictPolicy) {
            ConflictPolicy.RENAME -> ConflictResolver.resolveUniqueName(fileName, existingNames)
            ConflictPolicy.OVERWRITE -> fileName
        }
        if (plannedName != fileName) c.renamed += 1
        // Update cache so subsequent dry-run entries see realistic names (P3 fix)
        monthState?.existingNames?.add(plannedName)
        c.planned += 1
    }

    private fun processRealFile(
        sourceFile: DocumentFile,
        fileName: String,
        monthState: TargetFolderState,
        config: SortConfig,
        c: Counters,
    ) {
        val plannedName = when (config.conflictPolicy) {
            ConflictPolicy.RENAME -> ConflictResolver.resolveUniqueName(fileName, monthState.existingNames)
            ConflictPolicy.OVERWRITE -> fileName
        }
        val plannedWasRenamed = plannedName != fileName
        if (plannedWasRenamed) c.renamed += 1

        val mime = sourceFile.type ?: "application/octet-stream"
        val targetPlanResult = createTargetFileWithPolicy(monthState, fileName, mime, config.conflictPolicy)

        if (targetPlanResult is TargetPlanResult.Failure) {
            c.failed += 1
            if (targetPlanResult.deleteConflictFailed) c.deleteFailed += 1 else c.createFailed += 1
            return
        }
        val targetPlan = (targetPlanResult as TargetPlanResult.Success).plan

        if (targetPlan.wasRenamed && !plannedWasRenamed) c.renamed += 1

        // Resolve real on-disk paths to enable OS-level copy (primary strategy)
        val sourceRealPath = resolveRealPath(sourceFile.uri, "r")
        val targetRealPath = resolveRealPath(targetPlan.targetFile.uri, "rw")

        if (sourceRealPath != null && targetRealPath != null) {
            // ── Primary strategy: OS copy via Files.copy(COPY_ATTRIBUTES) ──────────
            val osOk = performOsCopy(sourceRealPath, targetRealPath, config.mode)
            if (osOk) {
                c.osCopyUsed += 1
                when (config.mode) {
                    OperationMode.COPY -> c.copied += 1
                    OperationMode.MOVE -> c.moved += 1
                }
                return
            }
            // OS copy failed (e.g. cross-filesystem edge case) — fall through to stream copy
        }

        // ── Fallback strategy: stream copy ────────────────────────────────────────
        val sourceLastModifiedMillis = sourceFile.lastModified().takeIf { it > 0 }

        val copiedOk = copyContent(sourceFile.uri, targetPlan.targetFile.uri)
        if (!copiedOk) {
            cleanupTarget(targetPlan.targetFile, monthState)
            c.failed += 1; c.copyFailed += 1
            return
        }

        c.streamFallbackUsed += 1

        // Best-effort mtime restoration for stream-copy fallback
        if (sourceLastModifiedMillis != null && targetRealPath != null) {
            val mtimeOk = runCatching {
                java.io.File(targetRealPath).setLastModified(sourceLastModifiedMillis)
            }.getOrDefault(false)
            if (mtimeOk) c.timestampPreserved += 1 else c.timestampFailed += 1
        } else {
            c.timestampFailed += 1
        }

        when (config.mode) {
            OperationMode.COPY -> c.copied += 1
            OperationMode.MOVE -> {
                if (sourceFile.delete()) {
                    c.moved += 1
                } else {
                    cleanupTarget(targetPlan.targetFile, monthState)
                    c.failed += 1; c.deleteFailed += 1
                }
            }
        }
    }

    private fun cleanupTarget(targetFile: DocumentFile, monthState: TargetFolderState) {
        if (targetFile.delete()) {
            targetFile.name?.let { monthState.existingNames.remove(it) }
        }
    }

    // ── Progress throttling (P6) ────────────────────────────────────────

    private fun emitThrottled(
        c: Counters,
        total: Int,
        onProgress: (SortProgress) -> Unit,
        lastTime: Long,
        intervalNanos: Long,
    ): Long {
        val now = System.nanoTime()
        if (now - lastTime >= intervalNanos || c.processed == total) {
            onProgress(SortProgress(processed = c.processed, total = total))
            return now
        }
        return lastTime
    }

    // ── Directory and file helpers ──────────────────────────────────────

    private fun errorReport(dryRun: Boolean) = SortReport(
        processed = 0, copied = 0, moved = 0, failed = 1, skipped = 0, dryRun = dryRun,
    )

    private fun ensureDirectory(parent: DocumentFile, name: String, createIfMissing: Boolean): DocumentFile? {
        val existing = parent.listFiles().firstOrNull { it.isDirectory && it.name == name }
        return existing ?: if (createIfMissing) parent.createDirectory(name) else null
    }

    private fun createTargetFileWithPolicy(
        targetState: TargetFolderState,
        desiredName: String,
        mimeType: String,
        conflictPolicy: ConflictPolicy,
    ): TargetPlanResult = when (conflictPolicy) {
        ConflictPolicy.RENAME -> createRenamedTargetFile(targetState, desiredName, mimeType)
        ConflictPolicy.OVERWRITE -> createOverwrittenTargetFile(targetState, desiredName, mimeType)
    }

    private fun createRenamedTargetFile(
        targetState: TargetFolderState,
        desiredName: String,
        mimeType: String,
    ): TargetPlanResult {
        repeat(50) {
            val resolvedName = ConflictResolver.resolveUniqueName(desiredName, targetState.existingNames)
            val created = targetState.folder.createFile(mimeType, resolvedName)
            if (created != null) {
                targetState.existingNames.add(resolvedName)
                return TargetPlanResult.Success(
                    TargetPlan(targetFile = created, wasRenamed = resolvedName != desiredName)
                )
            }
            targetState.existingNames.add(resolvedName)
        }
        return TargetPlanResult.Failure(deleteConflictFailed = false)
    }

    private fun createOverwrittenTargetFile(
        targetState: TargetFolderState,
        desiredName: String,
        mimeType: String,
    ): TargetPlanResult {
        if (targetState.existingNames.contains(desiredName)) {
            val existing = targetState.folder.findFile(desiredName)
            if (existing != null && !existing.delete()) {
                return TargetPlanResult.Failure(deleteConflictFailed = true)
            }
            targetState.existingNames.remove(desiredName) // R3 fix
        }

        val created = targetState.folder.createFile(mimeType, desiredName)
            ?: return TargetPlanResult.Failure(deleteConflictFailed = false)

        targetState.existingNames.add(desiredName)
        return TargetPlanResult.Success(TargetPlan(targetFile = created, wasRenamed = false))
    }

    private fun copyContent(sourceUri: Uri, targetUri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(sourceUri)?.use { src ->
                contentResolver.openOutputStream(targetUri, "w")?.use { dst ->
                    src.copyTo(dst)
                } ?: return false
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves the real on-disk path for a SAF [Uri] by opening a file descriptor and reading
     * the `/proc/self/fd/{fd}` symlink. Returns null if the provider does not back a real path.
     */
    private fun resolveRealPath(uri: Uri, mode: String): String? {
        return try {
            contentResolver.openFileDescriptor(uri, mode)?.use { pfd ->
                Os.readlink("/proc/self/fd/${pfd.fd}")
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Primary copy/move strategy: uses [java.nio.file.Files.copy] with
     * [java.nio.file.StandardCopyOption.COPY_ATTRIBUTES] so the OS preserves mtime, atime,
     * and any other attributes the underlying filesystem supports — without manual intervention.
     *
     * For MOVE mode: copies with attributes first, then deletes the source.
     * (The target file already exists as an empty SAF placeholder; REPLACE_EXISTING overwrites it.)
     *
     * Returns true on success, false on any error (caller falls back to stream copy).
     */
    private fun performOsCopy(sourcePath: String, targetPath: String, mode: OperationMode): Boolean {
        return try {
            val src = java.nio.file.Paths.get(sourcePath)
            val dst = java.nio.file.Paths.get(targetPath)
            java.nio.file.Files.copy(
                src, dst,
                java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            if (mode == OperationMode.MOVE) {
                java.nio.file.Files.delete(src)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // ── Folder cache ────────────────────────────────────────────────────

    private fun resolveMonthState(
        targetRoot: DocumentFile,
        yearFolder: String,
        monthFolder: String,
        createIfMissing: Boolean,
        yearDirCache: MutableMap<String, DocumentFile?>,
        monthStateCache: MutableMap<String, TargetFolderState?>,
    ): TargetFolderState? {
        val monthKey = "$yearFolder/$monthFolder"
        if (monthStateCache.containsKey(monthKey)) return monthStateCache[monthKey]

        val yearDir = yearDirCache.getOrPut(yearFolder) {
            ensureDirectory(targetRoot, yearFolder, createIfMissing)
        }

        if (yearDir == null) {
            monthStateCache[monthKey] = null
            return null
        }

        val monthDir = ensureDirectory(yearDir, monthFolder, createIfMissing)
        val state = monthDir?.let {
            TargetFolderState(
                folder = it,
                existingNames = it.listFiles().mapNotNull { file -> file.name }.toMutableSet(),
            )
        }
        monthStateCache[monthKey] = state
        return state
    }

    // ── File collection ─────────────────────────────────────────────────

    private fun collectCandidateFiles(root: DocumentFile, config: SortConfig): List<SortCandidate> {
        val files = mutableListOf<SortCandidate>()
        val stack = ArrayDeque<DocumentFile>()
        stack.add(root)

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            node.listFiles().forEach { child ->
                when {
                    child.isDirectory -> stack.add(child)
                    child.isFile && InputFilePolicy.shouldInclude(child.name, config.sortNonImages) -> {
                        files.add(
                            SortCandidate(
                                file = child,
                                dateSourceMode = InputFilePolicy.effectiveDateSourceMode(
                                    fileName = child.name,
                                    configuredMode = config.dateSourceMode,
                                    includeNonImages = config.sortNonImages,
                                ),
                            )
                        )
                    }
                }
            }
        }
        return files
    }

    // ── Date resolution ─────────────────────────────────────────────────

    private fun resolveDate(file: DocumentFile, mode: DateSourceMode): LocalDateTime = when (mode) {
        DateSourceMode.METADATA_THEN_FILE -> tryReadExifDate(file.uri) ?: fileDate(file)
        DateSourceMode.FILE_ONLY -> fileDate(file)
    }

    private fun fileDate(file: DocumentFile): LocalDateTime {
        val millis = file.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    }

    private fun tryReadExifDate(uri: Uri): LocalDateTime? {
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
}
