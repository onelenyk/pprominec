package dev.onelenyk.pprominec.presentation.components.main

import MapFilesRepository
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

interface MapComponent {
    val state: StateFlow<MapState>

    fun onAddMapUri(uri: String)

    fun onRemoveMapUri(uri: String)

    fun onSelectMapUri(uri: String?)

    fun onRemoveFromStorage(file: File)

    fun onClearStorage()
}

data class MapState(
    val mapUris: Set<String> = emptySet(),
    val selectedMapUri: String? = null,
    val isLoading: Boolean = false,
    val selectedMapFile: File? = null,
    val storedFiles: List<File> = emptyList(),
)

class DefaultMapComponent(
    componentContext: ComponentContext,
    private val appContext: Context,
    private val repository: MapFilesRepository,
    private val mapFileStorage: MapFileStorage,
    private val coroutineScope: CoroutineScope,
) : MapComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow(MapState())
    override val state: StateFlow<MapState>
        get() = _state.asStateFlow()

    init {
        coroutineScope.launch {
            repository.getMapUris().collectLatest { uris ->
                _state.value = _state.value.copy(mapUris = uris)
            }
        }

        coroutineScope.launch {
            repository.getSelectedMapUri().collectLatest { uriString ->
                val file = findLocalFileForUri(uriString)
                _state.value = _state.value.copy(selectedMapUri = uriString, selectedMapFile = file)
            }
        }

        coroutineScope.launch {
            refreshStoredFiles()
        }
    }

    private fun findLocalFileForUri(uriString: String?): File? {
        if (uriString.isNullOrBlank()) return null
        val uri = Uri.parse(uriString)
        val fileName = getFileNameFromUri(appContext, uri) ?: return null
        val file = File(File(appContext.filesDir, "map_files"), fileName)
        return if (file.exists()) file else null
    }

    private suspend fun refreshStoredFiles() {
        val files = mapFileStorage.listStoredFiles()
        _state.value = _state.value.copy(storedFiles = files)
    }

    override fun onAddMapUri(uriString: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val uri = Uri.parse(uriString)
                appContext.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                val fileName = getFileNameFromUri(appContext, uri) ?: "stored_map_${System.currentTimeMillis()}.map"
                mapFileStorage.copyUriToStorage(uri, fileName)
                repository.addMapUri(uriString)
                refreshStoredFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onRemoveMapUri(uriString: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val uri = Uri.parse(uriString)
                val fileName = getFileNameFromUri(appContext, uri)
                if (fileName != null) {
                    mapFileStorage.removeFromStorage(fileName)
                }
                if (_state.value.selectedMapUri == uriString) {
                    repository.selectMapUri("")
                }
                repository.removeMapUri(uriString)
                refreshStoredFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onSelectMapUri(uriString: String?) {
        coroutineScope.launch {
            repository.selectMapUri(uriString ?: "")
        }
    }

    override fun onRemoveFromStorage(file: File) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                mapFileStorage.removeFromStorage(file.name)
                // Also remove the corresponding URI from the DataStore
                val uriToRemove = _state.value.mapUris.find { getFileNameFromUri(appContext, Uri.parse(it)) == file.name }
                if (uriToRemove != null) {
                    onRemoveMapUri(uriToRemove)
                } else {
                    refreshStoredFiles()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onClearStorage() {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                mapFileStorage.clearStorage()
                _state.value.mapUris.forEach { repository.removeMapUri(it) }
                repository.selectMapUri("")
                refreshStoredFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun getFileNameFromUri(
        context: Context,
        uri: Uri,
    ): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
}
