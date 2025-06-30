package dev.onelenyk.pprominec.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Centralized DataStore manager that handles all DataStore instances for the project.
 * This class provides a single point of access to all DataStore instances.
 */
class DataStoreManager(private val context: Context) {

    /**
     * DataStore for file repository preferences
     */
    private val Context._fileRepositoryDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "file_repository",
    )

    /**
     * DataStore for application settings
     */
    private val Context._settingsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "app_settings",
    )

    /**
     * DataStore for user preferences
     */
    private val Context._userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences",
    )

    /**
     * DataStore for map settings
     */
    private val Context._mapSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "map_settings",
    )

    /**
     * DataStore for permissions state
     */
    private val Context._permissionsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "permissions",
    )

    // Public properties for easy access
    val fileRepositoryDataStore: DataStore<Preferences>
        get() = context._fileRepositoryDataStore

    val settingsDataStore: DataStore<Preferences>
        get() = context._settingsDataStore

    val userPreferencesDataStore: DataStore<Preferences>
        get() = context._userPreferencesDataStore

    val mapSettingsDataStore: DataStore<Preferences>
        get() = context._mapSettingsDataStore

    val permissionsDataStore: DataStore<Preferences>
        get() = context._permissionsDataStore
}
