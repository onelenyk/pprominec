package dev.onelenyk.pprominec.presentation.ui.screens.map

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import dev.onelenyk.pprominec.presentation.components.main.MapEffect
import dev.onelenyk.pprominec.presentation.components.main.MapIntent
import dev.onelenyk.pprominec.presentation.components.main.MapState
import dev.onelenyk.pprominec.presentation.mvi.MviScreen
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.MapViewState
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// ============================================================================
// MAP BUILDER - Shared functionality for both online and offline maps
// ============================================================================

@Composable
fun rememberMapBuilder(): MapBuilder {
    return remember { MapBuilder() }
}

class MapBuilder {
    private var locationOverlay: MyLocationNewOverlay? = null

    fun configureOSMDroid(context: android.content.Context) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().apply {
            tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L // 50MB
            tileDownloadThreads = 2
            tileDownloadMaxQueueSize = 8
        }
    }

    fun getCurrentLocation(): org.osmdroid.util.GeoPoint? {
        return locationOverlay?.myLocation
    }

    fun moveToCurrentLocation(map: MapView) {
        val currentLocation = getCurrentLocation()
        if (currentLocation != null) {
            map.controller.animateTo(currentLocation)
        }
    }

    fun createMapEventsOverlay(dispatch: (MapIntent) -> Unit): MapEventsOverlay {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                dispatch(MapIntent.MapTapped(p.latitude, p.longitude))
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                dispatch(MapIntent.MapLongPressed(p.latitude, p.longitude))
                return true
            }
        }
        return MapEventsOverlay(receiver)
    }

    fun createMapView(
        context: android.content.Context,
        state: MapViewState,
        isMapInitialized: Boolean,
        onMapCreated: (MapView) -> Unit
    ): MapView {
        return MapView(context).apply {
            if (!isMapInitialized) {
                setMultiTouchControls(true)
                controller.setZoom(state.zoomLevel)
                controller.setCenter(state.center)

                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                setWillNotDraw(false)
                clipToOutline = true
                clipChildren = true

                if (state.showMyLocation) {
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)
                    this@MapBuilder.locationOverlay = locationOverlay
                }
            }
            onMapCreated(this)
        }
    }

    fun updateMapState(map: MapView, newState: MapViewState, currentState: MapViewState) {
        if (newState.center != currentState.center || newState.zoomLevel != currentState.zoomLevel) {
            map.controller.animateTo(
                newState.center.latitude.toInt(),
                newState.center.longitude.toInt(),
            )
        }

        if (newState.tileSource != currentState.tileSource) {
            map.setTileSource(TileSourceFactory.getTileSource(newState.tileSource))
        }
    }

    fun updateMapView(map: MapView, state: MapViewState) {
        if (map.tileProvider.tileSource.name() != state.tileSource) {
            map.setTileSource(TileSourceFactory.getTileSource(state.tileSource))
        }
    }

    fun updateMarkers(
        map: MapView,
        markers: List<dev.onelenyk.pprominec.presentation.ui.MapMarker>,
        dispatch: (MapIntent) -> Unit,
        context: android.content.Context
    ) {
        val existingMarkers = map.overlays.filterIsInstance<Marker>()
        map.overlays.removeAll(existingMarkers)

        markers.forEach { marker ->
            val mapMarker = Marker(map).apply {
                position = GeoPoint(marker.latitude, marker.longitude)
                title = marker.title
                snippet = marker.description
                isDraggable = true

                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker?) {}
                    override fun onMarkerDrag(marker: Marker?) {}
                    override fun onMarkerDragEnd(marker: Marker?) {
                        marker?.let {
                            dispatch(
                                MapIntent.MapCenterChanged(
                                    it.position.latitude, it.position.longitude
                                )
                            )
                        }
                    }
                })

                marker.icon?.let { iconResId ->
                    setIcon(context.getDrawable(iconResId))
                }

                setOnMarkerClickListener { _, _ ->
                    dispatch(MapIntent.MarkerClicked(marker))
                    true
                }
            }
            map.overlays.add(mapMarker)
        }

        map.invalidate()
    }
}

// ============================================================================
// MAIN SCREEN COMPOSABLES
// ============================================================================

/**
 * Main Map Screen using MVI architecture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(component: MapComponent) {
    val context = LocalContext.current

    MviScreen(
        component = component, onEffect = { effect ->
            when (effect) {
                is MapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MapEffect.NavigateToMarkerDetails -> {
                    Toast.makeText(
                        context, "Navigate to: ${effect.marker.title}", Toast.LENGTH_SHORT
                    ).show()
                }

                is MapEffect.StartCaching -> {
                    Toast.makeText(
                        context, "Starting cache for: ${effect.boundingBox}", Toast.LENGTH_SHORT
                    ).show()
                }

                is MapEffect.CacheCompleted -> {
                    Toast.makeText(
                        context, "Cache completed: ${effect.tileCount} tiles", Toast.LENGTH_SHORT
                    ).show()
                }

                is MapEffect.ErrorOccurred -> {
                    Toast.makeText(context, "Error: ${effect.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) { state, dispatch ->
        AppScreen(
            toolbar = { AppToolbar(title = "Map") },
            content = {
                MapContent(
                    modifier = Modifier,
                    state = state,
                    dispatch = dispatch,
                )
            },
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    val activity = LocalContext.current as ComponentActivity

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            // Map Container based on mode
            when (state.mapViewState.mapMode) {
                MapMode.ONLINE -> {
                    OnlineMapContainer(
                        state = state,
                        dispatch = dispatch,
                    //    onCache = { downloadRegionForOfflineUse(it, activity) },
                    )
                }

                MapMode.OFFLINE -> {
                    OfflineMapContainer(
                        state = state,
                        dispatch = dispatch,
                    )
                }
            }

            // Overlay UI Elements
            MapOverlayUI(
                state = state,
                dispatch = dispatch,
            )
        }
    }
}

// ============================================================================
// SHARED MAP CONTAINER
// ============================================================================

@Composable
private fun MapContainer(
    modifier: Modifier = Modifier,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
    mapBuilder: MapBuilder,
    onMapReady: (MapView) -> Unit,
    overlayContent: @Composable BoxScope.(MapView, MapBuilder) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var currentMapState by remember { mutableStateOf(state.mapViewState) }

    // Configure OSMDroid
    LaunchedEffect(Unit) {
        mapBuilder.configureOSMDroid(context)
    }

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    mapView?.onDetach()
                    mapView = null
                    isMapInitialized = false
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
            mapView = null
            isMapInitialized = false
        }
    }

    // Map events receiver
    val eventsOverlay = mapBuilder.createMapEventsOverlay(dispatch)

    // Update map state when it changes
    LaunchedEffect(state.mapViewState) {
        mapView?.let { map ->
            mapBuilder.updateMapState(map, state.mapViewState, currentMapState)
            currentMapState = state.mapViewState
        }
    }

    // Update markers when the list changes
    LaunchedEffect(state.markers) {
        mapView?.let { map ->
            mapBuilder.updateMarkers(map, state.markers, dispatch, context)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                mapBuilder.createMapView(
                    context = ctx,
                    state = state.mapViewState,
                    isMapInitialized = isMapInitialized,
                    onMapCreated = { map ->
                        mapView = map
                        isMapInitialized = true
                        onMapReady(map)
                        map.overlays.add(0, eventsOverlay)
                    })
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            update = { map ->
                mapBuilder.updateMapView(map, state.mapViewState)
            },
        )

        // Overlay content
        mapView?.let { overlayContent(it, mapBuilder) }
    }
}

// ============================================================================
// OVERLAY UI COMPONENTS
// ============================================================================

@Composable
private fun MapOverlayUI(
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    Box() {
        // Crosshair overlay
        if (state.mapViewState.showCrosshair) {
            CrosshairOverlay()
        }

        // Markers dialog
        UsersMarkersDialog(
            isVisible = state.showMarkersDialog,
            onDismiss = { dispatch(MapIntent.HideMarkersDialog) },
            markers = state.markers,
            onMarkerEdit = { marker -> dispatch(MapIntent.AddMarker(marker)) },
            onMarkerDelete = { markerId -> dispatch(MapIntent.RemoveMarker(markerId)) },
            onMarkerAdd = { /* Handle marker add */ })
    }
}

@Composable
private fun CrosshairOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Horizontal line
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(
                    color = Color.Red,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                )
        )

        // Vertical line
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(
                    color = Color.Red,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                )
        )
    }
}

// ============================================================================
// MAP CONTAINERS
// ============================================================================

@Composable
fun OnlineMapContainer(
    modifier: Modifier = Modifier,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    val mapBuilder = rememberMapBuilder()

    MapContainer(
        modifier = modifier,
        state = state,
        dispatch = dispatch,
        mapBuilder = mapBuilder,
        onMapReady = { mapView ->
            // Online-specific setup
            mapView.setTileSource(TileSourceFactory.getTileSource(state.mapViewState.tileSource))
        },
        overlayContent = { mapView, mapBuilder ->
            MapOverlayContainer(
                mapView = mapView,
                state = state,
                dispatch = dispatch,
                mapBuilder = mapBuilder,
            )
        })
}

@Composable
fun OfflineMapContainer(
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    val mapBuilder = rememberMapBuilder()

    MapContainer(
        modifier = Modifier.background(Color.Green), // Offline indicator
        state = state, dispatch = dispatch, mapBuilder = mapBuilder, onMapReady = { mapView ->
            // Offline-specific setup
            mapView.setTileSource(TileSourceFactory.MAPNIK)

            // Load MBTiles if available
            state.selectedMapFile?.let { fileInfo ->
                if (fileInfo.type.extension == "mbtiles") {
                    val mbtilesFile = File(fileInfo.path)
                    if (mbtilesFile.exists()) {
                        mapView.setupOfflineMap(mbtilesFile)
                    }
                }
            }
        }, overlayContent = { mapView, mapBuilder ->
            // Offline mode indicator
            OfflineModeIndicator(
                selectedFile = state.selectedMapFile,
                mapMode = state.mapViewState.mapMode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            )

            // Map controls
            MapOverlayContainer(
                mapView = mapView,
                state = state,
                dispatch = dispatch,
                mapBuilder = mapBuilder,
            )
        })
}


@Composable
private fun OfflineModeIndicator(
    mapMode: MapMode,
    selectedFile: dev.onelenyk.pprominec.presentation.components.main.FileInfo?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = when (mapMode) {
                    MapMode.ONLINE -> "\uD83C\uDF10 Online mode"
                    MapMode.OFFLINE -> "\uD83D\uDCF5 Offline mode"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (mapMode == MapMode.OFFLINE) {
                Text(
                    text = if (selectedFile != null) {
                        "Файл: ${selectedFile.name}"
                    } else {
                        "Файл не вибрано"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

        }
    }
}

@Composable
fun MapOverlayContainer(
    mapView: MapView,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
    mapBuilder: MapBuilder,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Offline mode indicator
        OfflineModeIndicator(
            selectedFile = state.selectedMapFile,
            mapMode = state.mapViewState.mapMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )

        // My Location button - separate overlay block above the main controls
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            androidx.compose.material3.IconButton(
                onClick = {
                    mapBuilder.moveToCurrentLocation(mapView)
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Move to My Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Main controls card
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TileSourceDropdown(
                    currentTileSourceName = state.mapViewState.tileSource,
                    onTileSourceSelected = { newTileSource ->
                        dispatch(MapIntent.TileSourceChanged(newTileSource.name()))
                    },
                    modifier = Modifier,
                )

                // Zoom controls as icon buttons
                if (state.mapViewState.showZoomControls) {
                    androidx.compose.material3.IconButton(
                        onClick = {
                            mapView.controller.zoomIn()
                            dispatch(MapIntent.ZoomLevelChanged(mapView.zoomLevelDouble))
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.IconButton(
                        onClick = {
                            mapView.controller.zoomOut()
                            dispatch(MapIntent.ZoomLevelChanged(mapView.zoomLevelDouble))
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Zoom Out",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Show markers dialog button
                androidx.compose.material3.IconButton(
                    onClick = {
                        dispatch(MapIntent.ShowMarkersDialog)
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Show Markers",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle crosshair button
                androidx.compose.material3.IconButton(
                    onClick = {
                        dispatch(MapIntent.ToggleCrosshair(!state.mapViewState.showCrosshair))
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    CrosshairIcon(
                        isEnabled = state.mapViewState.showCrosshair,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Add marker at center button
                androidx.compose.material3.IconButton(
                    onClick = {
                        dispatch(
                            MapIntent.AddMarkerAtPosition(
                                mapView.mapCenter.latitude, mapView.mapCenter.longitude
                            )
                        )
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Add Marker at Center",
                        tint = Color.Red
                    )
                }
            }
        }
    }

}

// Helper functions (same as in original MapScreen)
fun MapView.setupOfflineMap(mbTilesFile: File) {
    val context = this.context

    if (!mbTilesFile.exists()) {
        Toast.makeText(
            context,
            "Map file not found: ${mbTilesFile.absolutePath}",
            Toast.LENGTH_LONG,
        ).show()
        return
    }

    try {
        val tileProvider =
            OfflineTileProvider(SimpleRegisterReceiver(context), arrayOf(mbTilesFile))
        this.tileProvider = tileProvider

        val tileSource = tileProvider.tileSource

        if (tileSource != null) {
            this.setTileSource(tileSource)
        } else {
            Toast.makeText(
                context,
                "Could not find a tile source in the archive.",
                Toast.LENGTH_SHORT,
            ).show()
            this.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        }

        this.invalidate()
        Toast.makeText(
            context,
            "Successfully loaded offline map: '$mbTilesFile'",
            Toast.LENGTH_SHORT,
        ).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error loading offline map: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun downloadRegionForOfflineUse(mapView: MapView, activity: ComponentActivity) {
    val lvivBoundingBox = BoundingBox(49.87, 24.08, 49.80, 23.95)
    val cacheManager = CacheManager(mapView)
    val zoomMin = 12
    val zoomMax = 17

    Toast.makeText(activity, "Starting download for Lviv...", Toast.LENGTH_SHORT).show()
    cacheManager.downloadAreaAsync(activity, lvivBoundingBox, zoomMin, zoomMax)
}

@Composable
fun MapModeChip(
    mapMode: MapMode,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = when (mapMode) {
                    MapMode.ONLINE -> Icons.Default.Star
                    MapMode.OFFLINE -> Icons.Default.Warning
                },
                contentDescription = "Map mode icon",
                modifier = Modifier.size(16.dp),
                tint = when (mapMode) {
                    MapMode.ONLINE -> MaterialTheme.colorScheme.primary
                    MapMode.OFFLINE -> MaterialTheme.colorScheme.secondary
                },
            )
            Text(
                text = when (mapMode) {
                    MapMode.ONLINE -> "Online"
                    MapMode.OFFLINE -> "Offline"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TileSourceDropdown(
    currentTileSourceName: String,
    onTileSourceSelected: (ITileSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val availableTileSources = remember { TileSourceFactory.getTileSources() }

    Card(
        modifier = modifier.wrapContentWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                Text(
                    modifier = Modifier
                        .menuAnchor()
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    text = "\uD83D\uDDFA\uFE0F",
                    style = MaterialTheme.typography.bodyLarge
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    matchTextFieldWidth = false
                ) {
                    availableTileSources.forEach { tileSource ->
                        if (tileSource.name() != "MBTiles") {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        tileSource.name(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    onTileSourceSelected(tileSource)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrosshairIcon(
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (isEnabled) Color.Red else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Horizontal line
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(
                    color = color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                )
        )

        // Vertical line
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .background(
                    color = color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                )
        )
    }
}
