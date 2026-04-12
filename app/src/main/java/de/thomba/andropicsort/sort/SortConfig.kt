package de.thomba.andropicsort.sort

import android.net.Uri
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import java.util.Locale

data class SortConfig(
    val sourceTreeUri: Uri,
    val targetTreeUri: Uri,
    val mode: OperationMode,
    val conflictPolicy: ConflictPolicy,
    val dateSourceMode: DateSourceMode,
    val locale: Locale,
    val dryRun: Boolean,
)

