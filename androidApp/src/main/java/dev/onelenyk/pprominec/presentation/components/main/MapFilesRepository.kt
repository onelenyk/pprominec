import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val DATASTORE_NAME = "map_files_prefs"
val Context.mapFilesDataStore by preferencesDataStore(DATASTORE_NAME)

class MapFilesRepository(private val context: Context) {
    private val MAP_URIS_KEY = stringSetPreferencesKey("map_uris")
    private val SELECTED_MAP_URI_KEY = stringPreferencesKey("selected_map_uri")

    fun getMapUris(): Flow<Set<String>> =
        context.mapFilesDataStore.data.map { prefs -> prefs[MAP_URIS_KEY] ?: emptySet() }

    suspend fun addMapUri(uri: String) {
        context.mapFilesDataStore.edit { prefs ->
            val current = prefs[MAP_URIS_KEY] ?: emptySet()
            prefs[MAP_URIS_KEY] = current + uri
        }
    }

    suspend fun removeMapUri(uri: String) {
        context.mapFilesDataStore.edit { prefs ->
            val current = prefs[MAP_URIS_KEY] ?: emptySet()
            prefs[MAP_URIS_KEY] = current - uri
        }
    }

    suspend fun selectMapUri(uri: String) {
        context.mapFilesDataStore.edit { prefs ->
            prefs[SELECTED_MAP_URI_KEY] = uri
        }
    }

    fun getSelectedMapUri(): Flow<String?> =
        context.mapFilesDataStore.data.map { prefs -> prefs[SELECTED_MAP_URI_KEY] }
}
