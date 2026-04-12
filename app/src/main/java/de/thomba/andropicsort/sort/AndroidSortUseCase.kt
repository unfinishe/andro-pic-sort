package de.thomba.andropicsort.sort

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.ConflictResolver
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.core.SortReport
import de.thomba.andropicsort.core.SupportedImageFormats
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

    private sealed interface TargetPlanResult {
        data class Success(val plan: TargetPlan) : TargetPlanResult
        data class Failure(val deleteConflictFailed: Boolean = false) : TargetPlanResult
    }

    private data class TargetPlan(
        val targetFile: DocumentFile,
        val wasRenamed: Boolean,
    )

    private val exifDateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    override suspend fun run(config: SortConfig, onProgress: (SortProgress) -> Unit): SortReport {
        val sourceRoot = DocumentFile.fromTreeUri(context, config.sourceTreeUri)
        val targetRoot = DocumentFile.fromTreeUri(context, config.targetTreeUri)

        if (sourceRoot == null || !sourceRoot.isDirectory || targetRoot == null || !targetRoot.isDirectory) {
            return SortReport(processed = 0, copied = 0, moved = 0, failed = 1, skipped = 0, dryRun = config.dryRun)
        }

        val files = collectImageFiles(sourceRoot)
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

        for (sourceFile in files) {
            processed += 1
            try {
                val fileName = sourceFile.name
                if (fileName.isNullOrBlank()) {
                    skipped += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                val sourceDate = resolveDate(sourceFile, config.dateSourceMode)
                val (yearFolder, monthFolder) = YearMonthFolderSchema.pathFor(sourceDate, config.locale)
                val yearDir = ensureDirectory(targetRoot, yearFolder, createIfMissing = !config.dryRun)
                val monthDir = yearDir?.let { ensureDirectory(it, monthFolder, createIfMissing = !config.dryRun) }

                if (!config.dryRun && monthDir == null) {
                    failed += 1
                    createFailed += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }

                val existingNames = monthDir?.listFiles()?.mapNotNull { it.name }?.toSet().orEmpty()
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

                if (monthDir == null) {
                    failed += 1
                    createFailed += 1
                    onProgress(SortProgress(processed = processed, total = files.size))
                    continue
                }
                val mime = sourceFile.type ?: "application/octet-stream"
                val targetPlanResult = createTargetFileWithPolicy(
                    targetFolder = monthDir,
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
                    targetPlan.targetFile.delete()
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
                            targetPlan.targetFile.delete()
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
        targetFolder: DocumentFile,
        desiredName: String,
        mimeType: String,
        conflictPolicy: ConflictPolicy,
    ): TargetPlanResult {
        return when (conflictPolicy) {
            ConflictPolicy.RENAME -> createRenamedTargetFile(targetFolder, desiredName, mimeType)
            ConflictPolicy.OVERWRITE -> createOverwrittenTargetFile(targetFolder, desiredName, mimeType)
        }
    }

    private fun createRenamedTargetFile(
        targetFolder: DocumentFile,
        desiredName: String,
        mimeType: String,
    ): TargetPlanResult {
        repeat(50) {
            val existingNames = targetFolder.listFiles().mapNotNull { it.name }.toSet()
            val resolvedName = ConflictResolver.resolveUniqueName(desiredName, existingNames)
            if (targetFolder.findFile(resolvedName) != null) {
                return@repeat
            }

            val created = targetFolder.createFile(mimeType, resolvedName)
            if (created != null) {
                return TargetPlanResult.Success(
                    TargetPlan(
                        targetFile = created,
                        wasRenamed = resolvedName != desiredName,
                    )
                )
            }
        }
        return TargetPlanResult.Failure(deleteConflictFailed = false)
    }

    private fun createOverwrittenTargetFile(
        targetFolder: DocumentFile,
        desiredName: String,
        mimeType: String,
    ): TargetPlanResult {
        val existing = targetFolder.findFile(desiredName)
        if (existing != null && !existing.delete()) {
            return TargetPlanResult.Failure(deleteConflictFailed = true)
        }

        val created = targetFolder.createFile(mimeType, desiredName)
            ?: return TargetPlanResult.Failure(deleteConflictFailed = false)

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

    private fun collectImageFiles(root: DocumentFile): List<DocumentFile> {
        val files = mutableListOf<DocumentFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.add(root)

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            node.listFiles().forEach { child ->
                when {
                    child.isDirectory -> stack.add(child)
                    child.isFile && SupportedImageFormats.isSupported(child.name) -> files.add(child)
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



