package de.thomba.andropicsort.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.thomba.andropicsort.core.AppLocalePolicy
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.settings.SettingsStorage
import de.thomba.andropicsort.settings.StoredUiSettings
import de.thomba.andropicsort.settings.UiSettingsStorage
import de.thomba.andropicsort.sort.AndroidSortUseCase
import de.thomba.andropicsort.sort.TimestampFileNamePatterns
import de.thomba.andropicsort.sort.TimestampRepairConfig
import de.thomba.andropicsort.sort.TimestampRepairUseCase
import de.thomba.andropicsort.sort.SortConfig
import de.thomba.andropicsort.sort.SortUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(
    application: Application,
    private val sortUseCase: SortUseCase,
    private val repairUseCase: TimestampRepairUseCase,
    private val settingsStorage: SettingsStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AndroidViewModel(application) {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return MainViewModel(
                    application,
                    AndroidSortUseCase(application, application.contentResolver),
                    TimestampRepairUseCase(application, application.contentResolver),
                    UiSettingsStorage(application),
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        restoreSettings()
    }

    fun onSourceSelected(uri: Uri) {
        updateStateAndPersist { it.copy(sourceUri = uri, report = null, errorMessage = null) }
    }

    fun onTargetSelected(uri: Uri) {
        updateStateAndPersist { state ->
            val repairRootTracksTarget = state.repairRootUri == null || state.repairRootUri == state.targetUri
            state.copy(
                targetUri = uri,
                repairRootUri = if (repairRootTracksTarget) uri else state.repairRootUri,
                report = null,
                errorMessage = null,
            )
        }
    }

    fun onRepairRootSelected(uri: Uri) {
        updateStateAndPersist { it.copy(repairRootUri = uri, repairReport = null, errorMessage = null) }
    }

    fun onOpenRepairMode() {
        updateStateAndPersist { state ->
            if (state.repairRootUri != null) state
            else state.copy(repairRootUri = state.targetUri)
        }
    }

    fun onModeChanged(mode: OperationMode) {
        updateStateAndPersist { it.copy(mode = mode) }
    }

    fun onDryRunChanged(enabled: Boolean) {
        updateStateAndPersist { it.copy(dryRun = enabled) }
    }

    fun onConflictPolicyChanged(policy: ConflictPolicy) {
        updateStateAndPersist { it.copy(conflictPolicy = policy) }
    }

    fun onDateSourceModeChanged(mode: DateSourceMode) {
        updateStateAndPersist { it.copy(dateSourceMode = mode) }
    }

    fun onSortNonImagesChanged(enabled: Boolean) {
        updateStateAndPersist { it.copy(sortNonImages = enabled) }
    }

    fun onRepairDryRunChanged(enabled: Boolean) {
        updateStateAndPersist { it.copy(repairDryRun = enabled) }
    }

    fun onRepairCustomPatternChanged(pattern: String) {
        updateStateAndPersist { it.copy(repairCustomPattern = pattern) }
    }

    fun availableRepairPatternDetails() =
        TimestampFileNamePatterns.availablePatternDetails(_uiState.value.repairCustomPattern)

    fun startSort() {
        val state = _uiState.value
        if (state.isRunning) return // P4 guard

        val source = state.sourceUri
        val target = state.targetUri

        if (source == null || target == null) {
            _uiState.update { it.copy(errorMessage = "missing_folders") }
            return
        }

        // P1 — Source ≠ Target validation at UI level
        if (source == target) {
            _uiState.update { it.copy(errorMessage = "source_equals_target") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    isRepairRunning = false,
                    report = null,
                    repairReport = null,
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
                        sortNonImages = state.sortNonImages,
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
                        isRepairRunning = false,
                        errorMessage = e.message ?: "unknown_error",
                    )
                }
            }
        }
    }

    fun startRepair(): Boolean {
        val state = _uiState.value
        if (state.isRunning) return false

        val repairRoot = state.repairRootUri
        if (repairRoot == null) {
            _uiState.update { it.copy(errorMessage = "missing_repair_folder") }
            return false
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    isRepairRunning = true,
                    report = null,
                    repairReport = null,
                    errorMessage = null,
                    progressProcessed = 0,
                    progressTotal = 0,
                )
            }

            try {
                val report = repairUseCase.run(
                    TimestampRepairConfig(
                        rootTreeUri = repairRoot,
                        customPattern = state.repairCustomPattern.takeIf { it.isNotBlank() },
                        dryRun = state.repairDryRun,
                    )
                ) { progress ->
                    _uiState.update {
                        it.copy(progressProcessed = progress.processed, progressTotal = progress.total)
                    }
                }

                _uiState.update {
                    it.copy(
                        isRunning = false,
                        isRepairRunning = false,
                        repairReport = report,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        isRepairRunning = false,
                        errorMessage = e.message ?: "unknown_error",
                    )
                }
            }
        }
        return true
    }

    private fun restoreSettings() {
        viewModelScope.launch(ioDispatcher) {
            val stored = settingsStorage.load()
            val source = stored.sourceUri?.takeIf(::hasPersistedPermission)
            val target = stored.targetUri?.takeIf(::hasPersistedPermission)
            val repairRoot = stored.repairRootUri?.takeIf(::hasPersistedPermission)

            _uiState.update {
                it.copy(
                    sourceUri = source,
                    targetUri = target,
                    repairRootUri = repairRoot,
                    mode = stored.mode,
                    conflictPolicy = stored.conflictPolicy,
                    dateSourceMode = stored.dateSourceMode,
                    sortNonImages = stored.sortNonImages,
                    dryRun = stored.dryRun,
                    repairDryRun = stored.repairDryRun,
                    repairCustomPattern = stored.repairCustomPattern,
                )
            }

            // Keep storage consistent when a persisted URI is no longer accessible.
            if (source != stored.sourceUri || target != stored.targetUri || repairRoot != stored.repairRootUri) {
                settingsStorage.save(_uiState.value.toStoredSettings())
            }
        }
    }

    private fun updateStateAndPersist(update: (MainUiState) -> MainUiState) {
        _uiState.update(update)
        viewModelScope.launch(ioDispatcher) {
            settingsStorage.save(_uiState.value.toStoredSettings())
        }
    }

    private fun hasPersistedPermission(uri: Uri): Boolean {
        return getApplication<Application>().contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }
    }

    private fun MainUiState.toStoredSettings(): StoredUiSettings {
        return StoredUiSettings(
            sourceUri = sourceUri,
            targetUri = targetUri,
            repairRootUri = repairRootUri,
            mode = mode,
            conflictPolicy = conflictPolicy,
            dateSourceMode = dateSourceMode,
            sortNonImages = sortNonImages,
            dryRun = dryRun,
            repairDryRun = repairDryRun,
            repairCustomPattern = repairCustomPattern,
        )
    }
}

