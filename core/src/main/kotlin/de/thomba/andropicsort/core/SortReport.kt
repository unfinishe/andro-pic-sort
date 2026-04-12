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
    val dryRun: Boolean = false,
)

