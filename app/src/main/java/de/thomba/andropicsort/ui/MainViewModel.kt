package de.thomba.andropicsort.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.thomba.andropicsort.core.AppLocalePolicy
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.settings.StoredUiSettings
import de.thomba.andropicsort.settings.UiSettingsStorage
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
    private val settingsStorage = UiSettingsStorage(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        restoreSettings()
    }

    fun onSourceSelected(uri: Uri) {
        updateStateAndPersist { it.copy(sourceUri = uri, report = null, errorMessage = null) }
    }

    fun onTargetSelected(uri: Uri) {
        updateStateAndPersist { it.copy(targetUri = uri, report = null, errorMessage = null) }
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
                        errorMessage = e.message ?: "unknown_error",
                    )
                }
            }
        }
    }

    private fun restoreSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val stored = settingsStorage.load()
            val source = stored.sourceUri?.takeIf(::hasPersistedPermission)
            val target = stored.targetUri?.takeIf(::hasPersistedPermission)

            _uiState.update {
                it.copy(
                    sourceUri = source,
                    targetUri = target,
                    mode = stored.mode,
                    conflictPolicy = stored.conflictPolicy,
                    dateSourceMode = stored.dateSourceMode,
                    sortNonImages = stored.sortNonImages,
                    dryRun = stored.dryRun,
                )
            }

            // Keep storage consistent when a persisted URI is no longer accessible.
            if (source != stored.sourceUri || target != stored.targetUri) {
                settingsStorage.save(_uiState.value.toStoredSettings())
            }
        }
    }

    private fun updateStateAndPersist(update: (MainUiState) -> MainUiState) {
        _uiState.update(update)
        viewModelScope.launch(Dispatchers.IO) {
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
            mode = mode,
            conflictPolicy = conflictPolicy,
            dateSourceMode = dateSourceMode,
            sortNonImages = sortNonImages,
            dryRun = dryRun,
        )
    }
}

