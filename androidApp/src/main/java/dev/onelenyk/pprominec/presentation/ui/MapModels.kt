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
    val code: Char = 'A',
) {
    fun geo() = GeoCoordinate(latitude, longitude)
    fun icon() = GeoCoordinate(latitude, longitude)

    companion object {
        fun new(
            latitude: Double,
            longitude: Double,
            lastIndex: Int = 0,
            lastSymbol: Char?,
            title: String = "Marker #${lastIndex + 1}",
        ): MapMarker {
            val newMarker = MapMarker(
                id = "marker_${System.currentTimeMillis()}",
                latitude = latitude,
                longitude = longitude,
                title = title,
                description = "Added at map position",
                code = MapMarker.nextAlphabetSymbol(lastSymbol),
            )
            return newMarker
        }

        fun nextAlphabetSymbol(lastSymbol: Char?): Char {
            return when {
                lastSymbol == null -> 'A'
                lastSymbol in 'A'..'Y' -> lastSymbol + 1
                lastSymbol == 'Z' -> 'A'
                else -> throw IllegalArgumentException("Input must be A-Z or null")
            }
        }
    }
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
