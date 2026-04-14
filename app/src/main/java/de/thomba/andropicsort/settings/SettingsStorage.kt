package de.thomba.andropicsort.settings

interface SettingsStorage {
    suspend fun load(): StoredUiSettings
    suspend fun save(settings: StoredUiSettings)
}

