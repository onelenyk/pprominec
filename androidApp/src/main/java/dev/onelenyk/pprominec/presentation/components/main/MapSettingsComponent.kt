package dev.onelenyk.pprominec.presentation.components.main

import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.onelenyk.pprominec.data.MapSettingsRepository
import dev.onelenyk.pprominec.presentation.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

enum class MapMode {
    ONLINE,
    OFFLINE,
}

data class MapSettingsState(
    val mapMode: MapMode = MapMode.ONLINE,
    val mapFiles: List<FileInfo> = emptyList(),
    val selectedMapFile: FileInfo? = null,
    val loading: Boolean = false,
)

interface MapSettingsComponent {
    val state: StateFlow<MapSettingsState>
    val onBack: () -> Unit
    fun setMapMode(mode: MapMode)
    fun onFileSelected(uri: Uri)
    fun deleteFile(uid: String)
    fun clearFolder()
    fun refreshFiles()
    fun selectFile(fileInfo: FileInfo)
}

class DefaultMapSettingsComponent(
    componentContext: ComponentContext,
    override val onBack: () -> Unit,
) : MapSettingsComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow(MapSettingsState())
    override val state: StateFlow<MapSettingsState> = _state.asStateFlow()
    private val fileManager: FileManager = getKoin().get()
    private val mapSettingsRepository: MapSettingsRepository = getKoin().get()

    init {
        componentContext.lifecycle.doOnStart {
            componentContext.coroutineScope.launch {
                // Combine file list with stored settings
                combine(
                    fileManager.files,
                    mapSettingsRepository.isOnlineMode,
                    mapSettingsRepository.selectedFileUid,
                ) { files, isOnlineMode, selectedFileUid ->
                    val mapMode = if (isOnlineMode) MapMode.ONLINE else MapMode.OFFLINE
                    val selectedFile = if (selectedFileUid != null) {
                        files.find { it.uid == selectedFileUid }
                    } else {
                        null
                    }

                    _state.value = _state.value.copy(
                        mapMode = mapMode,
                        mapFiles = files,
                        selectedMapFile = selectedFile,
                    )
                }.collectLatest { }
            }
        }
    }

    override fun setMapMode(mode: MapMode) {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            val isOnline = mode == MapMode.ONLINE
            mapSettingsRepository.setOnlineMode(isOnline)
            _state.value = _state.value.copy(loading = false)
        }
    }

    override fun onFileSelected(uri: Uri) {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            fileManager.processSelectedFile(uri)
                .onSuccess { fileInfo ->
                    println("File processed successfully: ${fileInfo.name}")
                }
                .onFailure { exception ->
                    println("Error processing file: ${exception.message}")
                }
            _state.value = _state.value.copy(loading = false)
        }
    }

    override fun deleteFile(uid: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            fileManager.deleteFile(uid)
                .onSuccess {
                    println("File deleted successfully: $uid")
                    if (_state.value.selectedMapFile?.uid == uid) {
                        mapSettingsRepository.clearSelectedFileUid()
                    }
                }
                .onFailure { exception ->
                    println("Error deleting file: ${exception.message}")
                }
            _state.value = _state.value.copy(loading = false)
        }
    }

    override fun clearFolder() {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            fileManager.clearFolder()
                .onSuccess {
                    println("Folder cleared successfully")
                    mapSettingsRepository.clearSelectedFileUid()
                }
                .onFailure { exception ->
                    println("Error clearing folder: ${exception.message}")
                }
            _state.value = _state.value.copy(loading = false)
        }
    }

    override fun refreshFiles() {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            fileManager.refreshFiles()
                .onSuccess {
                    println("Files refreshed successfully")
                }
                .onFailure { exception ->
                    println("Error refreshing files: ${exception.message}")
                }
            _state.value = _state.value.copy(loading = false)
        }
    }

    override fun selectFile(fileInfo: FileInfo) {
        coroutineScope.launch {
            _state.value = _state.value.copy(loading = true)
            if (state.value.selectedMapFile?.uid == fileInfo.uid) {
                mapSettingsRepository.clearSelectedFileUid()
            } else {
                mapSettingsRepository.setSelectedFileUid(fileInfo.uid)
            }
            _state.value = _state.value.copy(loading = false)
        }
    }
}
