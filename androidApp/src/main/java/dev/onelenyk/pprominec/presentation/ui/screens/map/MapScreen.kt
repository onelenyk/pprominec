package dev.onelenyk.pprominec.presentation.ui.screens.map

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.onelenyk.pprominec.R
import dev.onelenyk.pprominec.presentation.components.main.FileInfo
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import dev.onelenyk.pprominec.presentation.components.main.MapEffect
import dev.onelenyk.pprominec.presentation.components.main.MapIntent
import dev.onelenyk.pprominec.presentation.components.main.MapState
import dev.onelenyk.pprominec.presentation.mvi.MviScreen
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.MapViewState
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// ============================================================================
// MAP BUILDER - Shared functionality for both online and offline maps
// ============================================================================

@Composable
fun rememberMapBuilder(): MapBuilder {
    return remember { MapBuilder() }
}

class MapBuilder {
    private var locationOverlay: MyLocationNewOverlay? = null

    fun configureOSMDroid(context: Context) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().apply {
            tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L // 50MB
            tileDownloadThreads = 2
            tileDownloadMaxQueueSize = 8
        }
    }

    fun getCurrentLocation(): GeoPoint? {
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
        context: Context,
        state: MapViewState,
        isMapInitialized: Boolean,
        onMapCreated: (MapView) -> Unit,
    ): MapView {
        return MapView(context).apply {
            if (!isMapInitialized) {
                setMultiTouchControls(true)
                controller.setZoom(state.zoomLevel)
                controller.setCenter(state.center)

                setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
        markers: List<MapMarker>,
        dispatch: (MapIntent) -> Unit,
        context: Context,
    ) {
        val existingMarkers = map.overlays.filterIsInstance<Marker>()
        map.overlays.removeAll(existingMarkers)

        markers.forEach { marker ->
            val mapMarker = Marker(map).apply {
                position = GeoPoint(marker.latitude, marker.longitude)
                title = marker.title
                snippet = marker.description
                isDraggable = true

                id = marker.id
                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker?) {}
                    override fun onMarkerDrag(marker: Marker?) {}
                    override fun onMarkerDragEnd(marker: Marker?) {
                        marker?.let {
                            val mapMarker = markers.firstOrNull { m -> m.id == marker.id }
                            if (mapMarker != null) {
                                dispatch(MapIntent.UpdateMarkerPosition(mapMarker, marker))
                            }
                        }
                    }
                })

                marker.code?.let {
                    setTextIcon(it.toString())
                    textLabelFontSize = 96
                    textLabelBackgroundColor
                }
                marker.iconResId?.let {
                    icon = context.getDrawable(it)
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
    val dialogSlot by component.dialog.subscribeAsState()
    // Handle effects using Channel
    LaunchedEffect(Unit) {
        component.effect.collectLatest { effect ->
            when (effect) {
                is MapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MapEffect.NavigateToMarkerDetails -> {
                    Toast.makeText(
                        context,
                        "Navigate to: ${effect.marker.title}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is MapEffect.ErrorOccurred -> {
                    Toast.makeText(context, "Error: ${effect.message}", Toast.LENGTH_LONG).show()
                }

                is MapEffect.StartCaching -> {
                    Toast.makeText(
                        context,
                        "Starting cache for: ${effect.boundingBox}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is MapEffect.CacheCompleted -> {
                }

                is MapEffect.CacheError -> {
                    Toast.makeText(context, "Cache Error: ${effect.message}", Toast.LENGTH_LONG)
                        .show()
                }

                MapEffect.CacheStarted -> {
                    Toast.makeText(context, "Cache started", Toast.LENGTH_SHORT).show()
                }

                is MapEffect.ShowCacheUsage -> {
                    Toast.makeText(
                        context,
                        "Cache usage: ${effect.sizeMB} MB",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                else -> Unit
            }
        }
    }

    MviScreen(
        component = component,
        onEffect = {},
    ) { state, dispatch ->
        AppScreen(
            toolbar = { AppToolbar(title = "Map") },
            content = {
                MapContent(
                    modifier = Modifier,
                    state = state,
                    dispatch = dispatch,
                    component = component,
                )
                // Render dialog if present
                dialogSlot.child?.instance?.also {
                    when (it) {
                        is MapComponent.Dialog.UserMarkers -> {
                            UsersMarkersDialog(component = it.usersMarkersComponent)
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    component: MapComponent,
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
            when (state.mapViewState.mapMode) {
                MapMode.ONLINE -> {
                    OnlineMapContainer(
                        component = component,
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
        }
    }
}

// ============================================================================
// SHARED MAP CONTAINER
// ============================================================================

@Composable
private fun MapContainer(
    modifier: Modifier = Modifier,
    component: MapComponent? = null,
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
                    },
                )
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
private fun CrosshairOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Horizontal line
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(
                    color = Color.Red,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp),
                ),
        )

        // Vertical line
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(
                    color = Color.Red,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp),
                ),
        )
    }
}

// ============================================================================
// MAP CONTAINERS
// ============================================================================

@Composable
fun OnlineMapContainer(
    modifier: Modifier = Modifier,
    component: MapComponent,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    val mapBuilder = rememberMapBuilder()

    MapContainer(
        modifier = modifier,
        component = component,
        state = state,
        dispatch = dispatch,
        mapBuilder = mapBuilder,
        onMapReady = { mapView ->
            // Online-specific setup
            mapView.setTileSource(TileSourceFactory.getTileSource(state.mapViewState.tileSource))
        },
        overlayContent = { mapView, mapBuilder ->
            MapOverlayContainer(
                component = component,
                mapView = mapView,
                state = state,
                dispatch = dispatch,
                mapBuilder = mapBuilder,
            )
        },
    )
}

@Composable
fun OfflineMapContainer(
    state: MapState,
    dispatch: (MapIntent) -> Unit,
) {
    val mapBuilder = rememberMapBuilder()

    MapContainer(
        modifier = Modifier.background(Color.Green), // Offline indicator
        state = state,
        dispatch = dispatch,
        mapBuilder = mapBuilder,
        onMapReady = { mapView ->
            // Offline-specific setup
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setUseDataConnection(false)

            mapView.setupOfflineMapFromSampleLogic()

            /*            // Load MBTiles if available
                        state.selectedMapFile?.let { fileInfo ->
                            //   if (fileInfo.type.extension == "mbtiles") {
                            val mbtilesFile = File(fileInfo.path)
                            if (mbtilesFile.exists()) {
                                mapView.setupOfflineMapFromSampleLogic()
                            }
                            //   }
                        }*/
        },
        overlayContent = { mapView, mapBuilder ->
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
        },
    )
}

@Composable
private fun OfflineModeIndicator(
    mapMode: MapMode,
    selectedFile: FileInfo?,
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
fun MapMenuContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
fun MapMenuItem(
    iconResId: Int,
    description: String,
    onClick: () -> Unit,
    tint: Color = Color.Black,
    containerColor: Color = Color.Transparent,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
        ),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = description,
            tint = if (enabled) tint else Color.Gray,
        )
    }
}

@Composable
fun MapOverlayContainer(
    component: MapComponent? = null,
    mapView: MapView,
    state: MapState,
    dispatch: (MapIntent) -> Unit,
    mapBuilder: MapBuilder,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        // Main Map Menu
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapMenuContainer(
                modifier = Modifier,
            ) {
                TileSourceDropdown(
                    modifier = Modifier,
                    onTileSourceSelected = { dispatch(MapIntent.TileSourceChanged(it.name())) },
                    currentTileSourceName = state.mapViewState.tileSource,
                )
                // Add more items as needed (e.g., my location, tile source, etc.)
            }

            MapMenuContainer(
                modifier = Modifier,
            ) {
                MapMenuItem(
                    iconResId = R.drawable.rounded_add_24,
                    description = "Zoom In",
                    onClick = { mapView.controller.zoomIn() },
                )
                MapMenuItem(
                    iconResId = R.drawable.rounded_remove_24,
                    description = "Zoom Out",
                    onClick = { mapView.controller.zoomOut() },
                )
                MapMenuItem(
                    iconResId = R.drawable.outline_my_location_24,
                    description = "Move to My Location",
                    onClick = {
                        mapBuilder.moveToCurrentLocation(mapView)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                )

                // Add more items as needed (e.g., my location, tile source, etc.)
            }
            MapMenuContainer(
                modifier = Modifier,
            ) {
                MapMenuItem(
                    iconResId = R.drawable.outline_cross_24,
                    description = "Enable crosshair",
                    onClick = {
                        dispatch(MapIntent.ToggleCrosshair(!state.mapViewState.showCrosshair))
                    },
                )

                MapMenuItem(
                    iconResId = R.drawable.outline_add_location_alt_24,
                    description = "Add Marker at Center",
                    onClick = {
                        val center = mapView.mapCenter
                        dispatch(MapIntent.AddMarkerAtPosition(center.latitude, center.longitude))
                    },
                )

                MapMenuItem(
                    iconResId = R.drawable.outline_list_alt_24,
                    description = "Open Marker list",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = {
                        dispatch(MapIntent.ShowMarkersDialog)
                    },
                )

                // Add more items as needed (e.g., my location, tile source, etc.)
            }

            MapMenuContainer(
                modifier = Modifier,
            ) {
                // Cache mode controls, progress, overlays, etc.
                CacheModeControls(
                    isCacheModeEnabled = state.isCacheModeEnabled,
                    onToggleCacheMode = {
                        mapView.mapCenter
                        if (state.isCacheModeEnabled) {
                            dispatch(MapIntent.DisableCacheMode)
                        } else {
                            dispatch(
                                MapIntent.EnableCacheMode(GeoPoint(mapView.mapCenter)),
                            )
                        }
                    },
                    onStartCache = {
                        component?.let {
                            coroutineScope.launch {
                                it.startCacheRegion(12, 17, mapView)
                            }
                        }
                    },
                    canStartCache = state.cacheRegionPoints.size == 4,
                )
            }
        }

        // Show progress indicator when caching is in progress
        if (state.isCachingInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Crosshair overlay
        if (state.mapViewState.showCrosshair) {
            CrosshairOverlay()
        }

        OfflineModeIndicator(
            selectedFile = state.selectedMapFile,
            mapMode = state.mapViewState.mapMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )
    }
}

/**
 * Extension function for MapView that directly translates the logic from
 * the official osmdroid Java sample 'SampleOfflineOnly.java'.
 */
private fun MapView.setupOfflineMapFromSampleLogic() {
    val context = this.context
    // 1. Force the map to not use the network connection.
    this.setUseDataConnection(false)

    // 2. Set a custom image for tiles that are not found in the archive.
    // Note: You must have a drawable named 'notfound.png' in your res/drawable folder.
    // If you don't have one, you can comment out the following line.
    // this.tileProvider.setTileLoadFailureImage(ContextCompat.getDrawable(context, R.drawable.notfound))

    // 3. Get the osmdroid base path (usually /sdcard/osmdroid/)
    val osmdroidDir = Configuration.getInstance().osmdroidBasePath

    this.setTileSource(
        XYTileSource(
            "Hikebikemap",
            7,
            17,
            256,
            ".png",
            arrayOf(),
        ),
    )

    /*    if (osmdroidDir.exists()) {
            val list = osmdroidDir.listFiles()
            if (list != null) {
                for (file in list) {
                    if (file.isDirectory) continue

                    val fileName = file.name.lowercase()
                    if (!fileName.contains(".")) continue

                    val fileExtension = fileName.substringAfterLast('.', "")
                    if (fileExtension.isEmpty()) continue

                    // 4. Check if osmdroid has a driver for this file type (e.g., zip, mbtiles, sqlite)
                    if (ArchiveFileFactory.isFileExtensionRegistered(fileExtension)) {
                        try {
                            // --- Found a compatible file, now set it up ---

                            // 5. Create an OfflineTileProvider using the found file.
                            val offlineTileProvider = OfflineTileProvider(
                                SimpleRegisterReceiver(context),
                                arrayOf(file)
                            )

                            // 6. Set this as the map's exclusive tile provider.
                            this.tileProvider = offlineTileProvider

                            // 7. Discover the tile source name from within the archive.
                            // val sourceName = offlineTileProvider.archives.firstOrNull()?.tileSources?.firstOrNull()

                            this.setTileSource(
                                XYTileSource(
                                    "Hikebikemap",
                                    7,
                                    17,
                                    offlineTileProvider.tileSource.tileSizePixels,
                                    ".png",
                                    arrayOf()
                                )
                            )

                     *//*       if (sourceName != null) {
                            // If a name is found, create a specific FileBasedTileSource.
                            this.setTileSource(FileBasedTileSource.getSource(sourceName))
                        } else {
                            // Otherwise, fall back to the default.
                            this.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                        }
*//*
                        Toast.makeText(
                            context,
                            "Using offline map: ${file.name}",
                            Toast.LENGTH_LONG
                        ).show()
                        this.invalidate()

                        // 8. IMPORTANT: Exit the loop and function after loading the first found map.
                        return
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
        // If the loop completes without finding a valid file
        Toast.makeText(
            context,
            "${osmdroidDir.absolutePath} did not have any files I can open!",
            Toast.LENGTH_LONG
        ).show()
    } else {
        Toast.makeText(context, "${osmdroidDir.absolutePath} dir not found!", Toast.LENGTH_SHORT)
            .show()
    }*/
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
                    style = MaterialTheme.typography.bodyLarge,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    matchTextFieldWidth = false,
                ) {
                    availableTileSources.forEach { tileSource ->
                        if (tileSource.name() != "MBTiles") {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        tileSource.name(),
                                        style = MaterialTheme.typography.bodySmall,
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
        contentAlignment = Alignment.Center,
    ) {
        // Horizontal line
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(
                    color = color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp),
                ),
        )

        // Vertical line
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .background(
                    color = color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp),
                ),
        )
    }
}

@Composable
fun CacheModeControls(
    isCacheModeEnabled: Boolean,
    onToggleCacheMode: () -> Unit,
    onStartCache: () -> Unit,
    canStartCache: Boolean,
) {
    Card(
        modifier = Modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = onToggleCacheMode,
                modifier = Modifier.size(36.dp),
            ) {
                if (isCacheModeEnabled) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Disable Cache Mode",
                        tint = Color.Red,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Enable Cache Mode",
                        tint = Color.Green,
                    )
                }
            }
            if (isCacheModeEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onStartCache,
                    enabled = canStartCache,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cache Selected Area",
                        tint = if (canStartCache) Color.Blue else Color.Gray,
                    )
                }
            }
        }
    }
}
