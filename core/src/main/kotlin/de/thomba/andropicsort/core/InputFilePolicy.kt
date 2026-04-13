package de.thomba.andropicsort.core

object InputFilePolicy {
    fun isImage(fileName: String?): Boolean = SupportedImageFormats.isSupported(fileName)

    fun shouldInclude(fileName: String?, includeNonImages: Boolean): Boolean {
        if (fileName.isNullOrBlank()) return false
        return isImage(fileName) || includeNonImages
    }

    fun effectiveDateSourceMode(
        fileName: String?,
        configuredMode: DateSourceMode,
        includeNonImages: Boolean,
    ): DateSourceMode {
        if (!shouldInclude(fileName, includeNonImages)) return configuredMode
        return if (isImage(fileName)) configuredMode else DateSourceMode.FILE_ONLY
    }
}

