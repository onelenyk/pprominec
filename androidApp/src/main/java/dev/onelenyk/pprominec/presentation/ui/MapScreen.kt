package dev.onelenyk.pprominec.presentation.ui

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import dev.onelenyk.pprominec.presentation.components.main.FileInfo
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// Data class for map markers
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val icon: Int? = null, // Resource ID for custom icon
)

// Enum for map modes
enum class MapMode {
    ONLINE,
    OFFLINE,
}

// Data class for map state
data class MapViewState(
    val center: GeoPoint = GeoPoint(50.4501, 30.5234), // Kyiv, Ukraine
    val zoomLevel: Double = 10.0,
    val tileSource: String = TileSourceFactory.MAPNIK.name(),
    val showMyLocation: Boolean = true,
    val showZoomControls: Boolean = true,
    val mapMode: MapMode = MapMode.ONLINE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(component: MapComponent) {
    AppScreen(
        toolbar = { AppToolbar(title = "Map") },
        content = {
            MapContent(
                modifier = Modifier,
                component = component,
            )
        },
    )
}

@Composable
fun MapControlsContainer(
    markers: List<MapMarker>,
    mapState: MapViewState,
    onAddSampleMarkers: () -> Unit,
    onClearMarkers: () -> Unit,
    onRemoveMarker: (String) -> Unit,
    onMapStateChange: (MapViewState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Simple mode switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Режим карти:",
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            onMapStateChange(mapState.copy(mapMode = MapMode.ONLINE))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mapState.mapMode == MapMode.ONLINE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Text("Онлайн")
                    }

                    Button(
                        onClick = {
                            onMapStateChange(mapState.copy(mapMode = MapMode.OFFLINE))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mapState.mapMode == MapMode.OFFLINE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Text("Офлайн")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (mapState.mapMode) {
                        MapMode.ONLINE -> "Поточний режим: Онлайн (динамічне завантаження)"
                        MapMode.OFFLINE -> "Поточний режим: Офлайн (локальний файл)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MarkersCard(
            markers = markers,
            onAddSampleMarkers = onAddSampleMarkers,
            onClearMarkers = onClearMarkers,
            onRemoveMarker = onRemoveMarker,
        )

        Spacer(modifier = Modifier.height(8.dp))

        MapControlsCard(
            mapState = mapState,
            onMapStateChange = onMapStateChange,
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    component: MapComponent,
) {
    val state by component.state.collectAsState()
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
                        markers = state.markers,
                        onMarkerClick = { marker ->
                            component.onMarkerClick(marker)
                        },
                        onAddMarkerAtCenter = { latitude, longitude ->
                            component.addMarker(
                                MapMarker(
                                    id = "marker_${System.currentTimeMillis()}",
                                    latitude = latitude,
                                    longitude = longitude,
                                    title = "Marker at ${
                                        String.format(
                                            "%.4f",
                                            latitude,
                                        )
                                    }, ${String.format("%.4f", longitude)}",
                                    description = "Added at map center",
                                ),
                            )
                        },
                        mapState = state.mapViewState,
                        onMapStateChange = { newState -> component.updateMapViewState(newState) },
                        onCache = { downloadRegionForOfflineUse(it, activity) },
                    )
                }

                MapMode.OFFLINE -> {
                    OfflineMapContainer(
                        markers = state.markers,
                        onMarkerClick = { marker ->
                            component.onMarkerClick(marker)
                        },
                        onAddMarkerAtCenter = { latitude, longitude ->
                            component.addMarker(
                                MapMarker(
                                    id = "marker_${System.currentTimeMillis()}",
                                    latitude = latitude,
                                    longitude = longitude,
                                    title = "Marker at ${
                                        String.format(
                                            "%.4f",
                                            latitude,
                                        )
                                    }, ${String.format("%.4f", longitude)}",
                                    description = "Added at map center",
                                ),
                            )
                        },
                        mapState = state.mapViewState,
                        onMapStateChange = { newState -> component.updateMapViewState(newState) },
                        selectedFile = state.selectedMapFile,
                        modifier = Modifier,
                    )
                }
            }

            // Map mode chip overlay
            MapModeChip(
                mapMode = state.currentMapMode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
fun MarkersCard(
    markers: List<MapMarker>,
    onAddSampleMarkers: () -> Unit,
    onClearMarkers: () -> Unit,
    onRemoveMarker: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Маркери:",
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Button(onClick = onAddSampleMarkers, modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Додати прикладові маркери")
            }
            Button(onClick = onClearMarkers, modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Очистити всі маркери")
            }
            markers.forEach { marker ->
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(marker.title, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onRemoveMarker(marker.id) },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text("Видалити")
                    }
                }
            }
        }
    }
}

@Composable
fun MapControlsCard(
    mapState: MapViewState,
    onMapStateChange: (MapViewState) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Налаштування карти:",
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Button(
                onClick = { onMapStateChange(mapState.copy(showMyLocation = !mapState.showMyLocation)) },
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text("Показувати мою локацію: ${mapState.showMyLocation}")
            }
            Button(
                onClick = { onMapStateChange(mapState.copy(showZoomControls = !mapState.showZoomControls)) },
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text("Показувати керування масштабом: ${mapState.showZoomControls}")
            }
        }
    }
}

@Composable
fun MapOverlayContainer(
    mapView: MapView,
    mapState: MapViewState,
    onAddMarkerAtCenter: ((Double, Double) -> Unit)? = null,
    onMapStateChange: ((MapViewState) -> Unit)? = null,
    onCache: (MapView) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Zoom controls
        if (mapState.showZoomControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FloatingActionButton(
                    onClick = {
                        mapView?.controller?.zoomIn()
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Text("+")
                }
                FloatingActionButton(
                    onClick = {
                        mapView?.controller?.zoomOut()
                    },
                ) {
                    Text("-")
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { onAddMarkerAtCenter }) {
                Text("Add Marker at Center")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onCache(mapView) }) {
                Text("CACHE")
            }
        }
    }
}

@Composable
fun OnlineMapContainer(
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    onAddMarkerAtCenter: ((Double, Double) -> Unit)? = null,
    mapState: MapViewState = MapViewState(),
    onMapStateChange: ((MapViewState) -> Unit)? = null,
    onCache: (MapView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var currentMapState by remember { mutableStateOf(mapState) }

    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().apply {
            tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L // 50MB
            tileDownloadThreads = 2
            tileDownloadMaxQueueSize = 8
        }
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

    // Update map state when it changes
    LaunchedEffect(mapState) {
        mapView?.let { map ->
            if (mapState.center != currentMapState.center || mapState.zoomLevel != currentMapState.zoomLevel) {
                map.controller.animateTo(
                    mapState.center.latitude.toInt(),
                    mapState.center.longitude.toInt(),
                )
            }

            if (mapState.tileSource != currentMapState.tileSource) {
                map.setTileSource(TileSourceFactory.getTileSource(mapState.tileSource))
            }

            currentMapState = mapState
        }
    }

    // Update markers when the list changes
    LaunchedEffect(markers) {
        mapView?.let { map ->
            val existingMarkers = map.overlays.filterIsInstance<Marker>()
            map.overlays.removeAll(existingMarkers)

            markers.forEach { marker ->
                val mapMarker = Marker(map).apply {
                    position = GeoPoint(marker.latitude, marker.longitude)
                    title = marker.title
                    snippet = marker.description

                    marker.icon?.let { iconResId ->
                        setIcon(context.getDrawable(iconResId))
                    }

                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(marker)
                        true
                    }
                }
                map.overlays.add(mapMarker)
            }

            map.invalidate()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue), // Different color to distinguish online mode
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    if (!isMapInitialized) {
                        setTileSource(TileSourceFactory.getTileSource(mapState.tileSource))
                        setMultiTouchControls(true)
                        controller.setZoom(mapState.zoomLevel)
                        controller.setCenter(mapState.center)

                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        setWillNotDraw(false)
                        clipToOutline = true
                        clipChildren = true

                        if (mapState.showMyLocation) {
                            val locationOverlay =
                                MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlay.enableMyLocation()
                            overlays.add(locationOverlay)
                        }

                        isMapInitialized = true
                    }

                    mapView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            update = { map ->
                if (map.tileProvider.tileSource.name() != mapState.tileSource) {
                    map.setTileSource(TileSourceFactory.getTileSource(mapState.tileSource))
                }
            },
        )

        TileSourceDropdown(
            currentTileSourceName = mapState.tileSource,
            onTileSourceSelected = { newTileSource ->
                onMapStateChange?.invoke(mapState.copy(tileSource = newTileSource.name()))
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        )

        // Overlay controls
        if (mapView != null) {
            MapOverlayContainer(
                mapView = mapView!!,
                mapState = mapState,
                onAddMarkerAtCenter = onAddMarkerAtCenter,
                onMapStateChange = onMapStateChange,
                onCache = onCache,
            )
        }
    }
}

@Composable
fun OfflineMapContainer(
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    onAddMarkerAtCenter: ((Double, Double) -> Unit)? = null,
    mapState: MapViewState = MapViewState(),
    onMapStateChange: ((MapViewState) -> Unit)? = null,
    selectedFile: FileInfo? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var currentMapState by remember { mutableStateOf(mapState) }

    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().apply {
            tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L // 50MB
            tileDownloadThreads = 2
            tileDownloadMaxQueueSize = 8
        }
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

    // Update map state when it changes
    LaunchedEffect(mapState) {
        mapView?.let { map ->
            if (mapState.center != currentMapState.center || mapState.zoomLevel != currentMapState.zoomLevel) {
                map.controller.animateTo(
                    mapState.center.latitude.toInt(),
                    mapState.center.longitude.toInt(),
                )
            }

            currentMapState = mapState
        }
    }

    // Update markers when the list changes
    LaunchedEffect(markers) {
        mapView?.let { map ->
            val existingMarkers = map.overlays.filterIsInstance<Marker>()
            map.overlays.removeAll(existingMarkers)

            markers.forEach { marker ->
                val mapMarker = Marker(map).apply {
                    position = GeoPoint(marker.latitude, marker.longitude)
                    title = marker.title
                    snippet = marker.description

                    marker.icon?.let { iconResId ->
                        setIcon(context.getDrawable(iconResId))
                    }

                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(marker)
                        true
                    }
                }
                map.overlays.add(mapMarker)
            }

            map.invalidate()
        }
    }

    // Load MBTiles file when selected file changes
    LaunchedEffect(selectedFile) {
        mapView?.let { map ->
            if (selectedFile != null && selectedFile.type.extension == "mbtiles") {
                try {
                    val mbtilesFile = File(selectedFile.path)
                    if (mbtilesFile.exists()) {
                        map.setupOfflineMap(mbtilesFile)
                        println("MBTiles file loaded successfully: ${selectedFile.name}")
                    } else {
                        println("MBTiles file not found: ${selectedFile.path}")
                    }
                } catch (e: Exception) {
                    println("Error loading MBTiles file: ${e.message}")
                }
            } else {
                // Fallback to default tile source if no valid file is selected
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.invalidate()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Green), // Different color to distinguish offline mode
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    if (!isMapInitialized) {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(mapState.zoomLevel)
                        controller.setCenter(mapState.center)

                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        setWillNotDraw(false)
                        clipToOutline = true
                        clipChildren = true

                        if (mapState.showMyLocation) {
                            val locationOverlay =
                                MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlay.enableMyLocation()
                            overlays.add(locationOverlay)
                        }

                        isMapInitialized = true
                    }

                    mapView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            update = { map ->
                // Handle any additional updates here
            },
        )

        // Offline mode indicator
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            ),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "🔄 Офлайн режим",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
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

        // Overlay controls
        if (mapView != null) {
            MapOverlayContainer(
                mapView = mapView!!,
                mapState = mapState,
                onAddMarkerAtCenter = onAddMarkerAtCenter,
                onMapStateChange = onMapStateChange,
                onCache = { },
            )
        }
    }
}

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
        // 1. Create the OfflineTileProvider. This is the heart of the offline setup.
        val tileProvider =
            OfflineTileProvider(SimpleRegisterReceiver(context), arrayOf(mbTilesFile))
        this.tileProvider = tileProvider

        // 2. CORRECTED: Let the OfflineTileProvider discover the TileSource from the archive.
        // This is the most reliable way to handle various formats (zip, mbtiles, sqlite)
        // as it reads the metadata directly from the file.
        val tileSource = tileProvider.tileSource

        if (tileSource != null) {
            this.setTileSource(tileSource)
        } else {
            // If for some reason no source is found, fall back to a default but warn the user.
            Toast.makeText(
                context,
                "Could not find a tile source in the archive.",
                Toast.LENGTH_SHORT,
            ).show()
            this.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        }

        // 3. Redraw the map and notify the user
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

/**
 * Downloads a specific geographic area for offline use.
 *
 * @param mapView The MapView instance.
 * @param activity The current activity context.
 */
fun downloadRegionForOfflineUse(mapView: MapView, activity: ComponentActivity) {
    // Define the bounding box for the city of Lviv, Ukraine
    val lvivBoundingBox = BoundingBox(49.87, 24.08, 49.80, 23.95)

    val cacheManager = CacheManager(mapView)

    // Define the zoom levels to download (e.g., from street level to city overview)
    val zoomMin = 12
    val zoomMax = 17

    Toast.makeText(activity, "Starting download for Lviv...", Toast.LENGTH_SHORT).show()

    // Start the download in the background
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

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = currentTileSourceName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tile Source") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                availableTileSources.forEach { tileSource ->
                    if (tileSource.name() != "MBTiles") { // Exclude MBTiles source from online selection
                        DropdownMenuItem(
                            text = { Text(tileSource.name()) },
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
