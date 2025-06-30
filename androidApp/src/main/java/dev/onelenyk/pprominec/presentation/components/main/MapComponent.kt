package dev.onelenyk.pprominec.presentation.components.main

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.onelenyk.pprominec.data.MapSettingsRepository
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.MapViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

interface MapComponent {
    val state: StateFlow<MapState>

    fun onMarkerClick(marker: MapMarker)

    fun addMarker(marker: MapMarker)

    fun removeMarker(markerId: String)

    fun clearMarkers()

    fun updateMapViewState(newState: MapViewState)
}

data class MapState(
    val markers: List<MapMarker> = emptyList(),
    val mapViewState: MapViewState = MapViewState(),
    val currentMapMode: MapMode = MapMode.ONLINE,
    val selectedMapFile: FileInfo? = null,
)

class DefaultMapComponent(
    componentContext: ComponentContext,
    private val appContext: Context,
    private val coroutineScope: CoroutineScope,
) : MapComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow(MapState())
    override val state: StateFlow<MapState> = _state.asStateFlow()

    private val fileManager: FileManager = getKoin().get()
    private val mapSettingsRepository: MapSettingsRepository = getKoin().get()

    init {
        componentContext.lifecycle.doOnStart {
            coroutineScope.launch {
                // Observe map settings to update the current map mode and selected file
                combine(
                    mapSettingsRepository.isOnlineMode,
                    mapSettingsRepository.selectedFileUid,
                    fileManager.files,
                ) { isOnlineMode, selectedFileUid, files ->
                    val mapMode = if (isOnlineMode) MapMode.ONLINE else MapMode.OFFLINE
                    val selectedFile = if (selectedFileUid != null) {
                        files.find { it.uid == selectedFileUid }
                    } else {
                        null
                    }

                    _state.value = _state.value.copy(
                        currentMapMode = mapMode,
                        mapViewState = _state.value.mapViewState.copy(mapMode = mapMode),
                        selectedMapFile = selectedFile,
                    )
                }.collectLatest { }
            }
        }
    }

    override fun onMarkerClick(marker: MapMarker) {
        // Handle marker click - you can show a dialog, navigate to details, etc.
        println("Marker clicked: ${marker.title} at ${marker.latitude}, ${marker.longitude}")
    }

    override fun addMarker(marker: MapMarker) {
        val currentMarkers = _state.value.markers.toMutableList()
        // Remove existing marker with same ID if it exists
        currentMarkers.removeAll { it.id == marker.id }
        currentMarkers.add(marker)
        _state.value = _state.value.copy(markers = currentMarkers)
    }

    override fun removeMarker(markerId: String) {
        val currentMarkers = _state.value.markers.toMutableList()
        currentMarkers.removeAll { it.id == markerId }
        _state.value = _state.value.copy(markers = currentMarkers)
    }

    override fun clearMarkers() {
        _state.value = _state.value.copy(markers = emptyList())
    }

    override fun updateMapViewState(newState: MapViewState) {
        _state.value = _state.value.copy(mapViewState = newState)
    }
}
