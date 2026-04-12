package de.thomba.andropicsort.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.thomba.andropicsort.core.AppLocalePolicy
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.sort.AndroidSortUseCase
import de.thomba.andropicsort.sort.SortConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sortUseCase = AndroidSortUseCase(application, application.contentResolver)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onSourceSelected(uri: Uri) {
        _uiState.update { it.copy(sourceUri = uri, report = null, errorMessage = null) }
    }

    fun onTargetSelected(uri: Uri) {
        _uiState.update { it.copy(targetUri = uri, report = null, errorMessage = null) }
    }

    fun onModeChanged(mode: OperationMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onDryRunChanged(enabled: Boolean) {
        _uiState.update { it.copy(dryRun = enabled) }
    }

    fun onConflictPolicyChanged(policy: ConflictPolicy) {
        _uiState.update { it.copy(conflictPolicy = policy) }
    }

    fun onDateSourceModeChanged(mode: DateSourceMode) {
        _uiState.update { it.copy(dateSourceMode = mode) }
    }

    fun startSort() {
        val state = _uiState.value
        val source = state.sourceUri
        val target = state.targetUri

        if (source == null || target == null) {
            _uiState.update { it.copy(errorMessage = "missing_folders") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    report = null,
                    errorMessage = null,
                    progressProcessed = 0,
                    progressTotal = 0,
                )
            }

            try {
                val locale = AppLocalePolicy.effectiveLocale(Locale.getDefault())
                val report = sortUseCase.run(
                    SortConfig(
                        sourceTreeUri = source,
                        targetTreeUri = target,
                        mode = state.mode,
                        conflictPolicy = state.conflictPolicy,
                        dateSourceMode = state.dateSourceMode,
                        locale = locale,
                        dryRun = state.dryRun,
                    )
                ) { progress ->
                    _uiState.update {
                        it.copy(progressProcessed = progress.processed, progressTotal = progress.total)
                    }
                }

                _uiState.update { it.copy(isRunning = false, report = report) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        errorMessage = e.message ?: "unknown_error",
                    )
                }
            }
        }
    }
}

