package de.thomba.andropicsort.ui

import android.net.Uri
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.core.SortReport
import de.thomba.andropicsort.sort.TimestampRepairReport

data class MainUiState(
    val sourceUri: Uri? = null,
    val targetUri: Uri? = null,
    val repairRootUri: Uri? = null,
    val mode: OperationMode = OperationMode.COPY,
    val conflictPolicy: ConflictPolicy = ConflictPolicy.RENAME,
    val dateSourceMode: DateSourceMode = DateSourceMode.METADATA_THEN_FILE,
    val sortNonImages: Boolean = false,
    val dryRun: Boolean = false,
    val repairDryRun: Boolean = true,
    val repairCustomPattern: String = "",
    val isRepairRunning: Boolean = false,
    val isRunning: Boolean = false,
    val progressProcessed: Int = 0,
    val progressTotal: Int = 0,
    val report: SortReport? = null,
    val repairReport: TimestampRepairReport? = null,
    val errorMessage: String? = null,
)

