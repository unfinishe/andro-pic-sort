package de.thomba.andropicsort.core

object ConflictResolver {
    fun resolveUniqueName(desiredName: String, existingNames: Set<String>): String {
        if (desiredName !in existingNames) return desiredName

        val dotIndex = desiredName.lastIndexOf('.')
        val base = if (dotIndex > 0) desiredName.substring(0, dotIndex) else desiredName
        val ext = if (dotIndex > 0) desiredName.substring(dotIndex) else ""

        var counter = 1
        var candidate = "${base}_$counter$ext"
        while (candidate in existingNames) {
            counter += 1
            candidate = "${base}_$counter$ext"
        }
        return candidate
    }
}

