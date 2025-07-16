package dev.onelenyk.pprominec.presentation.components.main

import dev.onelenyk.pprominec.presentation.ui.MapMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UsersMarkersRepository {
    private val markers = mutableListOf<MapMarker>()
    private val _markersFlow = MutableStateFlow<List<MapMarker>>(emptyList())
    val markersFlow: StateFlow<List<MapMarker>> get() = _markersFlow

    fun getMarkers(): List<MapMarker> = markers.toList()

    fun addMarker(marker: MapMarker) {
        markers.add(marker)
        _markersFlow.value = markers.toList()
    }

    fun updateMarker(marker: MapMarker) {
        val index = markers.indexOfFirst { it.id == marker.id }
        if (index != -1) {
            markers[index] = marker
            _markersFlow.value = markers.toList()
        }
    }

    fun deleteMarker(markerId: String) {
        markers.removeAll { it.id == markerId }
        _markersFlow.value = markers.toList()
    }

    fun clear() {
        markers.clear()
        _markersFlow.value = emptyList()
    }
} 
