package de.thomba.andropicsort.core

object SupportedImageFormats {
    val extensions: Set<String> = setOf(
        "jpg",
        "jpeg",
        "png",
        "webp",
        "gif",
        "bmp",
        "tiff",
        "heic",
        "heif",
        "dng",
    )

    fun isSupported(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return extension in extensions
    }
}

