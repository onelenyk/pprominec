package dev.onelenyk.pprominec.presentation.components.main

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.onelenyk.pprominec.data.MapSettingsRepository
import dev.onelenyk.pprominec.presentation.mvi.Effect
import dev.onelenyk.pprominec.presentation.mvi.Intent
import dev.onelenyk.pprominec.presentation.mvi.MviComponent
import dev.onelenyk.pprominec.presentation.mvi.State
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.MapViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

/**
 * Intents for Map screen - all user actions that can happen on the map
 */
sealed class MapIntent : Intent {
    // Marker related intents
    data class MarkerClicked(val marker: MapMarker) : MapIntent()
    data class AddMarker(val marker: MapMarker) : MapIntent()
    data class RemoveMarker(val markerId: String) : MapIntent()
    data object ClearMarkers : MapIntent()

    // Map view state intents
    data class UpdateMapViewState(val newState: MapViewState) : MapIntent()
    data class MapCenterChanged(val latitude: Double, val longitude: Double) : MapIntent()
    data class ZoomLevelChanged(val zoomLevel: Double) : MapIntent()
    data class TileSourceChanged(val tileSource: String) : MapIntent()
    data class ToggleMyLocation(val show: Boolean) : MapIntent()
    data class ToggleZoomControls(val show: Boolean) : MapIntent()
    data class ToggleCrosshair(val show: Boolean) : MapIntent()

    // Dialog intents
    data object ShowMarkersDialog : MapIntent()
    data object HideMarkersDialog : MapIntent()

    // Map interaction intents
    data class MapTapped(val latitude: Double, val longitude: Double) : MapIntent()
    data class MapLongPressed(val latitude: Double, val longitude: Double) : MapIntent()
    data class AddMarkerAtPosition(val latitude: Double, val longitude: Double) : MapIntent()
    data object MoveToMyLocation : MapIntent()

    // Cache intents
    data object CacheCurrentRegion : MapIntent()

    // File operations
    data class LoadOfflineMap(val filePath: String) : MapIntent()
}

/**
 * State for Map screen - the complete state of the map screen
 */
data class MapState(
    val mapViewState: MapViewState = MapViewState(),
    val markers: List<MapMarker> = emptyList(),
    val selectedMapFile: FileInfo? = null,
    val showMarkersDialog: Boolean = false,
) : State

/**
 * Effects for Map screen - one-time events that should be handled
 */
sealed class MapEffect : Effect {
    data class ShowToast(val message: String) : MapEffect()
    data class NavigateToMarkerDetails(val marker: MapMarker) : MapEffect()
    data class StartCaching(val boundingBox: String) : MapEffect()
    data class CacheCompleted(val tileCount: Int) : MapEffect()
    data class ErrorOccurred(val message: String) : MapEffect()
}

/**
 * MVI-based Map Component interface
 */
interface MapComponent : MviComponent<MapIntent, MapState, MapEffect>

/**
 * Implementation of MapComponent
 */
class DefaultMapComponent(
    componentContext: ComponentContext,
    private val appContext: Context,
    private val coroutineScope: CoroutineScope,
) : MapComponent, ComponentContext by componentContext {

    override val _state = MutableStateFlow(MapState())
    override val _effect = Channel<MapEffect>(Channel.BUFFERED)

    private val fileManager: dev.onelenyk.pprominec.presentation.components.main.FileManager =
        getKoin().get()
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

                    updateState(
                        _state.value.copy(
                            mapViewState = _state.value.mapViewState.copy(mapMode = mapMode),
                            selectedMapFile = selectedFile,
                        )
                    )
                }.collectLatest { }
            }
        }
    }

    override suspend fun processIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MarkerClicked -> {
                emitEffect(MapEffect.ShowToast("Marker clicked: ${intent.marker.title}"))
            }

            is MapIntent.AddMarker -> {
                val currentState = _state.value
                val currentMarkers = currentState.markers.toMutableList()
                // Remove existing marker with same ID if it exists
                currentMarkers.removeAll { it.id == intent.marker.id }
                currentMarkers.add(intent.marker)
                updateState(currentState.copy(markers = currentMarkers))
            }

            is MapIntent.RemoveMarker -> {
                val currentState = _state.value
                val currentMarkers = currentState.markers.toMutableList()
                currentMarkers.removeAll { it.id == intent.markerId }
                updateState(currentState.copy(markers = currentMarkers))
            }

            is MapIntent.ClearMarkers -> {
                val currentState = _state.value
                updateState(currentState.copy(markers = emptyList()))
            }

            is MapIntent.UpdateMapViewState -> {
                val currentState = _state.value
                updateState(currentState.copy(mapViewState = intent.newState))
            }

            is MapIntent.MapCenterChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(
                    center = org.osmdroid.util.GeoPoint(intent.latitude, intent.longitude)
                )
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.ZoomLevelChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(zoomLevel = intent.zoomLevel)
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.TileSourceChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(tileSource = intent.tileSource)
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.ToggleMyLocation -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showMyLocation = intent.show)
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.ToggleZoomControls -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showZoomControls = intent.show)
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.ToggleCrosshair -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showCrosshair = intent.show)
                updateState(currentState.copy(mapViewState = newMapViewState))
            }

            is MapIntent.ShowMarkersDialog -> {
                val currentState = _state.value
                updateState(currentState.copy(showMarkersDialog = true))
            }

            is MapIntent.HideMarkersDialog -> {
                val currentState = _state.value
                updateState(currentState.copy(showMarkersDialog = false))
            }

            is MapIntent.MapTapped -> {
                emitEffect(
                    MapEffect.ShowToast(
                        "Map tapped at: ${
                            String.format(
                                "%.4f",
                                intent.latitude
                            )
                        }, ${String.format("%.4f", intent.longitude)}"
                    )
                )
            }

            is MapIntent.MapLongPressed -> {
                emitEffect(
                    MapEffect.ShowToast(
                        "Map long pressed at: ${
                            String.format(
                                "%.4f",
                                intent.latitude
                            )
                        }, ${String.format("%.4f", intent.longitude)}"
                    )
                )
            }

            is MapIntent.AddMarkerAtPosition -> {
                val currentState = _state.value
                val newMarker = MapMarker(
                    id = "marker_${System.currentTimeMillis()}",
                    latitude = intent.latitude,
                    longitude = intent.longitude,
                    title = "Marker at ${
                        String.format(
                            "%.4f",
                            intent.latitude
                        )
                    }, ${String.format("%.4f", intent.longitude)}",
                    description = "Added at map center",
                )
                val currentMarkers = currentState.markers.toMutableList()
                currentMarkers.add(newMarker)
                updateState(currentState.copy(markers = currentMarkers))
            }

            is MapIntent.CacheCurrentRegion -> {
                // Simulate long-term caching operation
                kotlinx.coroutines.delay(1000) // Simulate work
                emitEffect(MapEffect.StartCaching("Lviv region"))
            }

            is MapIntent.LoadOfflineMap -> {
                try {
                    // Simulate file loading operation
                    kotlinx.coroutines.delay(2000) // Simulate file processing

                    val file = File(intent.filePath)
                    if (file.exists()) {
                        emitEffect(MapEffect.ShowToast("Offline map loaded successfully: ${file.name}"))
                    } else {
                        emitEffect(MapEffect.ErrorOccurred("File not found: ${intent.filePath}"))
                    }
                } catch (e: Exception) {
                    emitEffect(MapEffect.ErrorOccurred("Failed to load offline map: ${e.message}"))
                }
            }

            is MapIntent.MoveToMyLocation -> {
                emitEffect(MapEffect.ShowToast("Moving to your current location..."))
            }
        }
    }
}
