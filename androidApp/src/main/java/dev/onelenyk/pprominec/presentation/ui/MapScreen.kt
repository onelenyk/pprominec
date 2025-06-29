package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Data class for map markers
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val icon: Int? = null, // Resource ID for custom icon
)

// Data class for map state
data class MapViewState(
    val center: GeoPoint = GeoPoint(50.4501, 30.5234), // Kyiv, Ukraine
    val zoomLevel: Double = 10.0,
    val tileSource: String = "MAPNIK",
    val showMyLocation: Boolean = true,
    val showZoomControls: Boolean = true,
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
        MarkersCard(
            markers = markers,
            onAddSampleMarkers = onAddSampleMarkers,
            onClearMarkers = onClearMarkers,
            onRemoveMarker = onRemoveMarker
        )

        Spacer(modifier = Modifier.height(8.dp))

        MapControlsCard(
            mapState = mapState,
            onMapStateChange = onMapStateChange
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    component: MapComponent,
) {
    val state by component.state.collectAsState()
    var mapViewState by remember { mutableStateOf(MapViewState()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls container
        MapControlsContainer(
            markers = state.markers,
            mapState = mapViewState,
            onAddSampleMarkers = {
                // Add some sample markers
                component.addMarker(
                    MapMarker(
                        id = "kyiv",
                        latitude = 50.4501,
                        longitude = 30.5234,
                        title = "Kyiv, Ukraine",
                        description = "Capital of Ukraine"
                    )
                )
                component.addMarker(
                    MapMarker(
                        id = "lviv",
                        latitude = 49.8397,
                        longitude = 24.0297,
                        title = "Lviv, Ukraine",
                        description = "Cultural capital of Ukraine"
                    )
                )
                component.addMarker(
                    MapMarker(
                        id = "kharkiv",
                        latitude = 49.9935,
                        longitude = 36.2304,
                        title = "Kharkiv, Ukraine",
                        description = "Second largest city"
                    )
                )
            },
            onClearMarkers = { component.clearMarkers() },
            onRemoveMarker = { markerId -> component.removeMarker(markerId) },
            onMapStateChange = { mapViewState = it },
            modifier = Modifier
                .fillMaxHeight(0.3f)
                .background(Color.Red)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        )

        // Map container with proper bounds
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
                .clipToBounds(), // Ensure the map doesn't overflow
        ) {
            MapContainer(
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
                                    "%.4f", latitude
                                )
                            }, ${String.format("%.4f", longitude)}",
                            description = "Added at map center"
                        )
                    )
                },
                mapState = mapViewState,
                onMapStateChange = { mapViewState = it }
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
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Показувати мою локацію: ${mapState.showMyLocation}")
            }
            Button(
                onClick = { onMapStateChange(mapState.copy(showZoomControls = !mapState.showZoomControls)) },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Показувати керування масштабом: ${mapState.showZoomControls}")
            }
        }
    }
}

@Composable
fun MapContainer(
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    onAddMarkerAtCenter: ((Double, Double) -> Unit)? = null,
    mapState: MapViewState = MapViewState(),
    onMapStateChange: ((MapViewState) -> Unit)? = null,
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
        // Set additional configurations to prevent overlap issues
        Configuration.getInstance().apply {
            // Prevent tile cache overflow
            tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L // 50MB
            // Set tile download threads
            tileDownloadThreads = 2
            // Set tile download max queue size
            tileDownloadMaxQueueSize = 8
            // Set tile file system thread max queue size
            //tileFileSystemThreadMaxQueueSize = 16
        }
    }

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView?.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView?.onPause()
                }
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
                    mapState.center.longitude.toInt()
                )
            }

            if (mapState.tileSource != currentMapState.tileSource) {
                val tileSource = when (mapState.tileSource) {
                    "MAPNIK" -> TileSourceFactory.MAPNIK
                    "SATELLITE" -> TileSourceFactory.PUBLIC_TRANSPORT
                    "TERRAIN" -> TileSourceFactory.USGS_SAT
                    else -> TileSourceFactory.MAPNIK
                }
                map.setTileSource(tileSource)
            }

            currentMapState = mapState
        }
    }

    // Update markers when the list changes
    LaunchedEffect(markers) {
        mapView?.let { map ->
            // Remove existing markers
            val existingMarkers = map.overlays.filterIsInstance<Marker>()
            map.overlays.removeAll(existingMarkers)

            // Add new markers
            markers.forEach { marker ->
                val mapMarker = Marker(map).apply {
                    position = GeoPoint(marker.latitude, marker.longitude)
                    title = marker.title
                    snippet = marker.description

                    // Set custom icon if provided
                    marker.icon?.let { iconResId ->
                        setIcon(context.getDrawable(iconResId))
                    }

                    // Set click listener
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(marker)
                        true
                    }
                }
                map.overlays.add(mapMarker)
            }

            // Refresh the map
            map.invalidate()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // Only initialize once
                    if (!isMapInitialized) {
                        setMultiTouchControls(true)
                        controller.setZoom(mapState.zoomLevel)
                        controller.setCenter(mapState.center)

                        // Add view-specific configurations to prevent overlap
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        setWillNotDraw(false)

                        // Set view bounds to prevent overflow
                        clipToOutline = true
                        clipChildren = true

                        // Add location overlay if enabled
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
                .clipToBounds(), // Add clipping to prevent overflow
            update = { map ->
                // Handle any additional updates here
            }
        )

        // Overlay controls
        MapOverlayContainer(
            mapView = mapView,
            mapState = mapState,
            onAddMarkerAtCenter = onAddMarkerAtCenter,
            onMapStateChange = onMapStateChange,
        )
    }
}

@Composable
fun MapOverlayContainer(
    mapView: MapView?,
    mapState: MapViewState,
    onAddMarkerAtCenter: ((Double, Double) -> Unit)? = null,
    onMapStateChange: ((MapViewState) -> Unit)? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Zoom controls
        if (mapState.showZoomControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        mapView?.controller?.zoomIn()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("+")
                }
                FloatingActionButton(
                    onClick = {
                        mapView?.controller?.zoomOut()
                    }
                ) {
                    Text("-")
                }
            }
        }

        // Locate button
        FloatingActionButton(
            onClick = {
                mapView?.let { map ->
                    val locationOverlay =
                        map.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    locationOverlay?.myLocation?.let { location ->
                        map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                        map.controller.setZoom(16.0)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Text("📍")
        }

        // Add marker at center button
        if (onAddMarkerAtCenter != null) {
            FloatingActionButton(
                onClick = {
                    mapView?.let { map ->
                        val center = map.mapCenter
                        onAddMarkerAtCenter(center.latitude, center.longitude)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                Text("➕")
            }
        }
    }
}
