package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.onelenyk.pprominec.R
import dev.onelenyk.pprominec.data.MapSettingsRepository
import dev.onelenyk.pprominec.presentation.coroutineScope
import dev.onelenyk.pprominec.presentation.mvi.Effect
import dev.onelenyk.pprominec.presentation.mvi.Intent
import dev.onelenyk.pprominec.presentation.mvi.MviComponent
import dev.onelenyk.pprominec.presentation.mvi.State
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import dev.onelenyk.pprominec.presentation.ui.MapMarkerType
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.MapViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.getKoin
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
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

    // --- Additions for region caching ---
    data class EnableCacheMode(val center: GeoPoint) : MapIntent()
    object DisableCacheMode : MapIntent()
    data class SetCacheRegionPoints(val points: List<MapMarker>) : MapIntent()
    object ShowCacheUsage : MapIntent()
    data class UpdateMarkerPosition(
        val mapMarker: MapMarker,
        val marker: org.osmdroid.views.overlay.Marker,
    ) : MapIntent()
}

/**
 * State for Map screen - the complete state of the map screen
 */
data class MapState(
    val mapViewState: MapViewState = MapViewState(),
    val userMarkers: List<MapMarker> = emptyList(),
    val selectedMapFile: FileInfo? = null,
    val showMarkersDialog: Boolean = false,
    // --- Cache region selection state ---
    val isCacheModeEnabled: Boolean = false,
    val cacheRegionPoints: List<MapMarker> = listOf(),
    val isCachingInProgress: Boolean = false,
    val lastCacheUsageMB: Double? = null,
) : State {
    val markers: List<MapMarker>
        get() = userMarkers.plus(
            if (isCacheModeEnabled) {
                cacheRegionPoints
            } else {
                listOf()
            },
        )
}

/**
 * Effects for Map screen - one-time events that should be handled
 */
sealed class MapEffect : Effect {
    data class ShowToast(val message: String) : MapEffect()
    data class NavigateToMarkerDetails(val marker: MapMarker) : MapEffect()
    data class StartCaching(val boundingBox: String) : MapEffect()
    data class ErrorOccurred(val message: String) : MapEffect()

    // --- Cache region selection effects ---
    data class ShowCacheUsage(val sizeMB: Double) : MapEffect()
    object CacheStarted : MapEffect()
    object CacheCompleted : MapEffect()
    data class CacheError(val message: String) : MapEffect()
}

/**
 * MVI-based Map Component interface
 */
interface MapComponent : MviComponent<MapIntent, MapState, MapEffect> {
    // Dialog logic
    val dialog: Value<ChildSlot<DialogConfig, Dialog>>
    fun showUserMarkerDialog()
    suspend fun startCacheRegion(zoomMin: Int, zoomMax: Int, mapView: org.osmdroid.views.MapView)

    @Serializable
    sealed class DialogConfig {
        @Serializable
        data object UserMarker : DialogConfig()
    }

    sealed class Dialog {
        data class UserMarkers(val usersMarkersComponent: UsersMarkersComponent) : Dialog()
    }
}

/**
 * Implementation of MapComponent
 */
class DefaultMapComponent(
    componentContext: ComponentContext,
) : MapComponent, ComponentContext by componentContext {
    override val _state = MutableStateFlow(MapState())
    override val _effect = Channel<MapEffect>(Channel.BUFFERED)

    private val fileManager: FileManager = getKoin().get()
    private val mapSettingsRepository: MapSettingsRepository = getKoin().get()
    private val usersMarkersRepository: UsersMarkersRepository = getKoin().get()

    private val dialogNavigation = SlotNavigation<MapComponent.DialogConfig>()
    override val dialog: Value<ChildSlot<MapComponent.DialogConfig, MapComponent.Dialog>> =
        childSlot(
            source = dialogNavigation,
            serializer = MapComponent.DialogConfig.serializer(),
            handleBackButton = true,
        ) { config, componentContext ->
            when (config) {
                is MapComponent.DialogConfig.UserMarker -> MapComponent.Dialog.UserMarkers(
                    DefaultUsersMarkersComponent(
                        componentContext = componentContext,
                        onClose = { dialogNavigation.dismiss() },
                    ),
                )
            }
        }

    override fun showUserMarkerDialog() {
        dialogNavigation.activate(MapComponent.DialogConfig.UserMarker)
    }

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

                    _state.update { state ->
                        state.copy(
                            mapViewState = state.mapViewState.copy(mapMode = mapMode),
                            selectedMapFile = selectedFile,
                        )
                    }
                }.collectLatest { }
            }
            // Observe marker changes from repository
            coroutineScope.launch {
                usersMarkersRepository.markersFlow.collect { markers ->
                    _state.update { state -> state.copy(userMarkers = markers) }
                }
            }
        }
    }

    override suspend fun processIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MarkerClicked -> {
                emitEffect(MapEffect.ShowToast("Marker clicked: ${intent.marker.title}"))
            }

            is MapIntent.AddMarker -> {
                usersMarkersRepository.addMarker(intent.marker)
            }

            is MapIntent.RemoveMarker -> {
                usersMarkersRepository.deleteMarker(intent.markerId)
            }

            is MapIntent.ClearMarkers -> {
                usersMarkersRepository.clear()
            }

            is MapIntent.UpdateMapViewState -> {
                val currentState = _state.value
                _state.update { state -> state.copy(mapViewState = intent.newState) }
            }

            is MapIntent.MapCenterChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(
                    center = org.osmdroid.util.GeoPoint(intent.latitude, intent.longitude),
                )
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.ZoomLevelChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(zoomLevel = intent.zoomLevel)
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.TileSourceChanged -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(tileSource = intent.tileSource)
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.ToggleMyLocation -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showMyLocation = intent.show)
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.ToggleZoomControls -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showZoomControls = intent.show)
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.ToggleCrosshair -> {
                val currentState = _state.value
                val newMapViewState = currentState.mapViewState.copy(showCrosshair = intent.show)
                _state.update { state -> state.copy(mapViewState = newMapViewState) }
            }

            is MapIntent.ShowMarkersDialog -> {
                showUserMarkerDialog()
            }

            is MapIntent.HideMarkersDialog -> {
                val currentState = _state.value
                _state.update { state -> state.copy(showMarkersDialog = false) }
            }

            is MapIntent.MapTapped -> {
                emitEffect(
                    MapEffect.ShowToast(
                        "Map tapped at: ${
                            String.format(
                                "%.4f",
                                intent.latitude,
                            )
                        }, ${String.format("%.4f", intent.longitude)}",
                    ),
                )
            }

            is MapIntent.MapLongPressed -> {
                emitEffect(
                    MapEffect.ShowToast(
                        "Map long pressed at: ${
                            String.format(
                                "%.4f",
                                intent.latitude,
                            )
                        }, ${String.format("%.4f", intent.longitude)}",
                    ),
                )
            }

            is MapIntent.AddMarkerAtPosition -> {
                val currentState = _state.value
                val newMarker = MapMarker.new(
                    latitude = intent.latitude,
                    longitude = intent.longitude,
                    lastIndex = currentState.markers.lastIndex,
                    lastSymbol = currentState.markers.lastOrNull()?.code,
                )
                usersMarkersRepository.addMarker(newMarker)
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
            // --- Cache region selection intent handling (stubs) ---
            is MapIntent.EnableCacheMode -> {
                val currentState = _state.value

                // When enabling, initialize 4 MapMarkers for corners around map center
                val center = intent.center
                val offset = 0.01 // ~1km, adjust as needed
                val corners = listOf(
                    MapMarker(
                        id = "cache_NW",
                        latitude = center.latitude + offset,
                        longitude = center.longitude - offset,
                        title = "NW Corner",
                        description = "North-West corner",
                        type = MapMarkerType.CACHE_CORNER,
                        iconResId = R.drawable.angle_frame_nw, // Use your drawable resource
                    ),
                    MapMarker(
                        id = "cache_NE",
                        latitude = center.latitude + offset,
                        longitude = center.longitude + offset,
                        title = "NE Corner",
                        description = "North-East corner",
                        type = MapMarkerType.CACHE_CORNER,
                        iconResId = R.drawable.angle_frame_ne, // Use your drawable resource
                    ),
                    MapMarker(
                        id = "cache_SE",
                        latitude = center.latitude - offset,
                        longitude = center.longitude + offset,
                        title = "SE Corner",
                        description = "South-East corner",
                        type = MapMarkerType.CACHE_CORNER,
                        iconResId = R.drawable.angle_frame_se, // Use your drawable resource
                    ),
                    MapMarker(
                        id = "cache_SW",
                        latitude = center.latitude - offset,
                        longitude = center.longitude - offset,
                        title = "SW Corner",
                        description = "South-West corner",
                        type = MapMarkerType.CACHE_CORNER,
                        iconResId = R.drawable.angle_frame_sw, // Use your drawable resource
                    ),
                )

                _state.update { state ->
                    state.copy(
                        isCacheModeEnabled = true,
                        cacheRegionPoints = corners,

                    )
                }
            }

            is MapIntent.DisableCacheMode -> {
                val currentState = _state.value
                _state.update { state ->
                    state.copy(
                        isCacheModeEnabled = false,
                        cacheRegionPoints = emptyList(),
                    )
                }
            }

            is MapIntent.SetCacheRegionPoints -> {
                val currentState = _state.value
                _state.update { state -> state.copy(cacheRegionPoints = intent.points) }
            }

            is MapIntent.ShowCacheUsage -> {
                // TODO: Provide MapView instance here
                val mapView: org.osmdroid.views.MapView =
                    TODO("Provide MapView instance to component")
                val sizeMB = mapView.getCacheUsageMB()
                emitEffect(MapEffect.ShowCacheUsage(sizeMB))
            }

            is MapIntent.UpdateMarkerPosition -> {
                val currentState = _state.value
                val updatedMarker = intent.mapMarker.copy(
                    latitude = intent.marker.position.latitude,
                    longitude = intent.marker.position.longitude,
                )
                when (intent.mapMarker.type) {
                    MapMarkerType.CACHE_CORNER -> {
                        val updatedCacheMarkers = currentState.cacheRegionPoints.map {
                            if (it.id == updatedMarker.id) updatedMarker else it
                        }
                        _state.update { state -> state.copy(cacheRegionPoints = updatedCacheMarkers) }
                    }

                    MapMarkerType.DEFAULT -> {
                        val updatedUserMarkers = currentState.userMarkers.map {
                            if (it.id == updatedMarker.id) updatedMarker else it
                        }
                        _state.update { state -> state.copy(userMarkers = updatedUserMarkers) }
                    }
                }
            }
        }
    }

    private fun buildBoundingBoxFromMarkers(markers: List<MapMarker>): BoundingBox? {
        if (markers.size != 4) return null
        val north = markers.maxOfOrNull { it.latitude } ?: return null
        val south = markers.minOfOrNull { it.latitude } ?: return null
        val east = markers.maxOfOrNull { it.longitude } ?: return null
        val west = markers.minOfOrNull { it.longitude } ?: return null
        return BoundingBox(north, east, south, west)
    }

    override suspend fun startCacheRegion(
        zoomMin: Int,
        zoomMax: Int,
        mapView: org.osmdroid.views.MapView,
    ) {
        mapView.tileProvider.tileSource.let { tileSource ->
            if (tileSource !is OnlineTileSourceBase) {
                emitEffect(MapEffect.CacheError("Tile source must be an online source for caching."))
                return
            }

            if (!tileSource.tileSourcePolicy.acceptsBulkDownload()) {
                emitEffect(MapEffect.CacheError("Tile source doesnt support bulk download."))
                return
            }
        }

        val currentState = _state.value
        val boundingBox = buildBoundingBoxFromMarkers(currentState.cacheRegionPoints)
        if (boundingBox != null) {
            _state.update { state -> state.copy(isCachingInProgress = true) }
            emitEffect(MapEffect.CacheStarted)
            val result = mapView.downloadAndCacheRegionSuspend(boundingBox, zoomMin, zoomMax)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isCachingInProgress = false,
                        )
                    }
                    emitEffect(MapEffect.CacheCompleted)
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isCachingInProgress = false,
                        )
                    }
                    emitEffect(MapEffect.CacheError(e.message ?: "Unknown error"))
                },
            )
        } else {
            emitEffect(MapEffect.CacheError("Please select 4 points to define the region."))
        }
    }
}
