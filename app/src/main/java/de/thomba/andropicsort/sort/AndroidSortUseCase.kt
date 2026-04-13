package de.thomba.andropicsort.sort

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.ConflictResolver
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.InputFilePolicy
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.core.SortReport
import de.thomba.andropicsort.core.YearMonthFolderSchema
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AndroidSortUseCase(
    private val context: Context,
    private val contentResolver: ContentResolver,
) : SortUseCase {

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

    private val exifDateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    override suspend fun run(config: SortConfig, onProgress: (SortProgress) -> Unit): SortReport {
        val sourceRoot = DocumentFile.fromTreeUri(context, config.sourceTreeUri)
        val targetRoot = DocumentFile.fromTreeUri(context, config.targetTreeUri)

        if (sourceRoot == null || !sourceRoot.isDirectory || targetRoot == null || !targetRoot.isDirectory) {
            return SortReport(processed = 0, copied = 0, moved = 0, failed = 1, skipped = 0, dryRun = config.dryRun)
        }

        val files = collectCandidateFiles(sourceRoot, config)
        val yearDirCache = mutableMapOf<String, DocumentFile?>()
        val monthStateCache = mutableMapOf<String, TargetFolderState?>()
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

        onProgress(SortProgress(processed = 0, total = files.size))

        for (candidate in files) {
            val sourceFile = candidate.file
            processed += 1
            try {
                val fileName = sourceFile.name
                if (fileName.isNullOrBlank()) {
                    skipped += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                val sourceDate = resolveDate(sourceFile, candidate.dateSourceMode)
                val (yearFolder, monthFolder) = YearMonthFolderSchema.pathFor(sourceDate, config.locale)
                val monthState = resolveMonthState(
                    targetRoot = targetRoot,
                    yearFolder = yearFolder,
                    monthFolder = monthFolder,
                    createIfMissing = !config.dryRun,
                    yearDirCache = yearDirCache,
                    monthStateCache = monthStateCache,
                )

                if (!config.dryRun && monthState == null) {
                    failed += 1
                    createFailed += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                val existingNames = monthState?.existingNames ?: emptySet()
                val plannedName = when (config.conflictPolicy) {
                    ConflictPolicy.RENAME -> ConflictResolver.resolveUniqueName(fileName, existingNames)
                    ConflictPolicy.OVERWRITE -> fileName
                }
                val plannedWasRenamed = plannedName != fileName
                if (plannedWasRenamed) renamed += 1

                if (config.dryRun) {
                    planned += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                if (monthState == null) {
                    failed += 1
                    createFailed += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }
                val mime = sourceFile.type ?: "application/octet-stream"
                val targetPlanResult = createTargetFileWithPolicy(
                    targetState = monthState,
                    desiredName = fileName,
                    mimeType = mime,
                    conflictPolicy = config.conflictPolicy,
                )

                if (targetPlanResult is TargetPlanResult.Failure) {
                    failed += 1
                    if (targetPlanResult.deleteConflictFailed) {
                        deleteFailed += 1
                    } else {
                        createFailed += 1
                    }
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }
                val targetPlan = (targetPlanResult as TargetPlanResult.Success).plan

                if (targetPlan.wasRenamed && !plannedWasRenamed) {
                    renamed += 1
                }

                val copiedOk = copyContent(sourceFile.uri, targetPlan.targetFile.uri)
                if (!copiedOk) {
                    if (targetPlan.targetFile.delete()) {
                        targetPlan.targetFile.name?.let { monthState.existingNames.remove(it) }
                    }
                    failed += 1
                    copyFailed += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                when (config.mode) {
                    OperationMode.COPY -> copied += 1
                    OperationMode.MOVE -> {
                        if (sourceFile.delete()) {
                            moved += 1
                        } else {
                            if (targetPlan.targetFile.delete()) {
                                targetPlan.targetFile.name?.let { monthState.existingNames.remove(it) }
                            }
                            failed += 1
                            deleteFailed += 1
                        }
                    }
                }
            } catch (_: Exception) {
                failed += 1
            }

            onProgress(SortProgress(processed = processed, total = files.size))
        }

        return SortReport(
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
            dryRun = config.dryRun,
        )
    }

    private fun ensureDirectory(parent: DocumentFile, name: String, createIfMissing: Boolean): DocumentFile? {
        val existing = parent.listFiles().firstOrNull { it.isDirectory && it.name == name }
        return existing ?: if (createIfMissing) parent.createDirectory(name) else null
    }

    private fun createTargetFileWithPolicy(
        targetState: TargetFolderState,
        desiredName: String,
        mimeType: String,
        conflictPolicy: ConflictPolicy,
    ): TargetPlanResult {
        return when (conflictPolicy) {
            ConflictPolicy.RENAME -> createRenamedTargetFile(targetState, desiredName, mimeType)
            ConflictPolicy.OVERWRITE -> createOverwrittenTargetFile(targetState, desiredName, mimeType)
        }
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
                    TargetPlan(
                        targetFile = created,
                        wasRenamed = resolvedName != desiredName,
                    )
                )
            }

            // SAF providers can race externally; reserve attempted name and retry.
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
        }

        val created = targetState.folder.createFile(mimeType, desiredName)
            ?: return TargetPlanResult.Failure(deleteConflictFailed = false)

        targetState.existingNames.add(desiredName)

        return TargetPlanResult.Success(
            TargetPlan(
                targetFile = created,
                wasRenamed = false,
            )
        )
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

    private fun resolveMonthState(
        targetRoot: DocumentFile,
        yearFolder: String,
        monthFolder: String,
        createIfMissing: Boolean,
        yearDirCache: MutableMap<String, DocumentFile?>,
        monthStateCache: MutableMap<String, TargetFolderState?>,
    ): TargetFolderState? {
        val monthKey = "$yearFolder/$monthFolder"
        if (monthStateCache.containsKey(monthKey)) {
            return monthStateCache[monthKey]
        }

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

    private fun resolveDate(file: DocumentFile, mode: DateSourceMode): LocalDateTime {
        return when (mode) {
            DateSourceMode.METADATA_THEN_FILE -> {
                val fromExif = tryReadExifDate(file.uri)
                if (fromExif != null) {
                    fromExif
                } else {
                    fileDate(file)
                }
            }
            DateSourceMode.FILE_ONLY -> fileDate(file)
        }
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



