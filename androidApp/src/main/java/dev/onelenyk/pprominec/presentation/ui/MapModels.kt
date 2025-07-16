package dev.onelenyk.pprominec.presentation.ui

import dev.onelenyk.pprominec.bussines.GeoCoordinate
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

// Data class for map markers
enum class MapMarkerType {
    DEFAULT,
    CACHE_CORNER,
}

// Extend MapMarker to include a type property (default to DEFAULT)
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String = "",
    val description: String = "",
    val type: MapMarkerType = MapMarkerType.DEFAULT,
    val iconResId: Int? = null,
) {
    fun geo() = GeoCoordinate(latitude, longitude)
}

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
