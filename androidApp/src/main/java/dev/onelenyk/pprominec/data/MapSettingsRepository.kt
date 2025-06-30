package dev.onelenyk.pprominec.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing map settings using DataStore
 */
class MapSettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val IS_ONLINE_MODE = booleanPreferencesKey("is_online_mode")
        private val SELECTED_FILE_UID = stringPreferencesKey("selected_file_uid")
    }

    /**
     * Flow that emits the current map mode (online/offline)
     */
    val isOnlineMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_ONLINE_MODE] ?: true // Default to online mode
    }

    /**
     * Flow that emits the currently selected file UID
     */
    val selectedFileUid: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_FILE_UID]
    }

    /**
     * Set the map mode (online/offline)
     */
    suspend fun setOnlineMode(isOnline: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ONLINE_MODE] = isOnline
        }
    }

    /**
     * Set the selected file UID
     */
    suspend fun setSelectedFileUid(fileUid: String?) {
        dataStore.edit { preferences ->
            if (fileUid != null) {
                preferences[SELECTED_FILE_UID] = fileUid
            } else {
                preferences.remove(SELECTED_FILE_UID)
            }
        }
    }

    /**
     * Clear the selected file UID
     */
    suspend fun clearSelectedFileUid() {
        dataStore.edit { preferences ->
            preferences.remove(SELECTED_FILE_UID)
        }
    }
}
