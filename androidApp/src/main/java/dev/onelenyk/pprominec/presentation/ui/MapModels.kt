package dev.onelenyk.pprominec.presentation.ui

import dev.onelenyk.pprominec.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

// Data class for map markers
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val icon: Int? = org.osmdroid.library.R.drawable.ic_menu_compass, // Resource ID for custom icon
)

// Enum for map modes
enum class MapMode {
    ONLINE, OFFLINE,
}

// Data class for map state
data class MapViewState(
    val center: GeoPoint = GeoPoint(50.4501, 30.5234), // Kyiv, Ukraine
    val zoomLevel: Double = 10.0,
    val tileSource: String = TileSourceFactory.MAPNIK.name(),
    val showMyLocation: Boolean = true,
    val showZoomControls: Boolean = true,
    val showCrosshair: Boolean = false,
    val mapMode: MapMode = MapMode.ONLINE,
)
