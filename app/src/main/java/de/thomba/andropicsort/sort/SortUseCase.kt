package de.thomba.andropicsort.sort

import de.thomba.andropicsort.core.SortReport

interface SortUseCase {
    suspend fun run(config: SortConfig, onProgress: (SortProgress) -> Unit): SortReport
}

