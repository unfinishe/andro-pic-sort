package de.thomba.andropicsort.sort

import java.time.LocalDateTime

data class ParsedFileNameDate(
    val dateTime: LocalDateTime,
    val patternLabel: String,
)

data class PatternPreviewResult(
    val patternValid: Boolean,
    val extracted: ParsedFileNameDate?,
)

data class RepairPatternDetail(
    val label: String,
    val example: String,
)

object TimestampFileNamePatterns {
    const val CUSTOM_LABEL = "Custom pattern"

    private data class NamePattern(
        val label: String,
        val regex: Regex,
        val example: String,
    )

    private val builtIns = listOf(
        NamePattern(
            label = "IMG_yyyyMMdd_HHmmss",
            regex = Regex("""(?:IMG|VID|MVIMG|PXL|Screenshot)[_-](?<year>\d{4})(?<month>\d{2})(?<day>\d{2})[_-](?<hour>\d{2})(?<minute>\d{2})(?<second>\d{2})""", RegexOption.IGNORE_CASE),
            example = "IMG_20240419_183025.jpg / PXL_20240419_183025123.jpg",
        ),
        NamePattern(
            label = "yyyyMMdd_HHmmss",
            regex = Regex("""(?<year>\d{4})(?<month>\d{2})(?<day>\d{2})[_-](?<hour>\d{2})(?<minute>\d{2})(?<second>\d{2})"""),
            example = "20240419_183025.jpg",
        ),
        NamePattern(
            label = "yyyy-MM-dd_HH-mm-ss",
            regex = Regex("""(?<year>\d{4})[-_](?<month>\d{2})[-_](?<day>\d{2})[-_T](?<hour>\d{2})[-_.:](?<minute>\d{2})[-_.:](?<second>\d{2})"""),
            example = "2024-04-19_18-30-25.jpg / 2024-04-19T18.30.25.mp4",
        ),
        NamePattern(
            label = "yyyyMMdd (date only)",
            regex = Regex("""(?:IMG|VID|PXL|WA)?[-_]?(?<year>\d{4})(?<month>\d{2})(?<day>\d{2})(?:\D|$)""", RegexOption.IGNORE_CASE),
            example = "WA_20230214-Chat.jpg / IMG-20240419-WA0001.jpg",
        ),
    )

    fun availablePatternDetails(customPattern: String?): List<RepairPatternDetail> {
        val defaults = builtIns.map { RepairPatternDetail(label = it.label, example = it.example) }
        return if (customPattern.isNullOrBlank()) {
            defaults
        } else {
            defaults + RepairPatternDetail(label = CUSTOM_LABEL, example = customPattern)
        }
    }

    fun compileCustom(customPattern: String?): Regex? {
        if (customPattern.isNullOrBlank()) return null
        return runCatching { Regex(customPattern) }.getOrNull()
    }

    fun parse(fileName: String?, customPattern: Regex?): ParsedFileNameDate? {
        if (fileName.isNullOrBlank()) return null

        builtIns.forEach { pattern ->
            parseDate(fileName, pattern.regex)?.let {
                return ParsedFileNameDate(it, pattern.label)
            }
        }

        if (customPattern != null) {
            parseDate(fileName, customPattern)?.let {
                return ParsedFileNameDate(it, CUSTOM_LABEL)
            }
        }
        return null
    }

    fun previewCustomPattern(fileName: String?, customPattern: String?): PatternPreviewResult {
        if (customPattern.isNullOrBlank()) return PatternPreviewResult(patternValid = false, extracted = null)
        val compiled = compileCustom(customPattern) ?: return PatternPreviewResult(patternValid = false, extracted = null)
        val parsed = if (fileName.isNullOrBlank()) {
            null
        } else {
            parseDate(fileName, compiled)?.let { ParsedFileNameDate(it, CUSTOM_LABEL) }
        }
        return PatternPreviewResult(patternValid = true, extracted = parsed)
    }

    fun previewBuiltInPattern(fileName: String?): ParsedFileNameDate? {
        if (fileName.isNullOrBlank()) return null
        builtIns.forEach { pattern ->
            parseDate(fileName, pattern.regex)?.let {
                return ParsedFileNameDate(it, pattern.label)
            }
        }
        return null
    }

    private fun parseDate(value: String, regex: Regex): LocalDateTime? {
        val match = regex.find(value) ?: return null
        val year = readGroup(match, "year")?.toIntOrNull() ?: return null
        val month = readGroup(match, "month")?.toIntOrNull() ?: return null
        val day = readGroup(match, "day")?.toIntOrNull() ?: return null
        val hour = readGroup(match, "hour")?.toIntOrNull() ?: 0
        val minute = readGroup(match, "minute")?.toIntOrNull() ?: 0
        val second = readGroup(match, "second")?.toIntOrNull() ?: 0

        return runCatching {
            LocalDateTime.of(year, month, day, hour, minute, second)
        }.getOrNull()
    }

    private fun readGroup(match: MatchResult, name: String): String? {
        return runCatching { match.groups[name]?.value }.getOrNull()
    }
}




