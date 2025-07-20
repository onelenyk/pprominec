package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import dev.onelenyk.pprominec.presentation.coroutineScope
import dev.onelenyk.pprominec.presentation.mvi.Effect
import dev.onelenyk.pprominec.presentation.mvi.Intent
import dev.onelenyk.pprominec.presentation.mvi.MviComponent
import dev.onelenyk.pprominec.presentation.mvi.State
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

sealed class UsersMarkersIntent : Intent {
    data class SelectMarker(val marker: MapMarker?) : UsersMarkersIntent()
    data class EditMarker(val marker: MapMarker) : UsersMarkersIntent()
    data class DeleteMarker(val markerId: String) : UsersMarkersIntent()
    object CloseScreen : UsersMarkersIntent()
}

data class UsersMarkersState(
    val markers: List<MapMarker> = emptyList(),
    val currentLocation: MapMarker? = null,
) : State {
    val uiMarkers: List<MapMarker>
        get() = markers +
            (currentLocation?.let { listOf(it) } ?: emptyList())
}

sealed class UsersMarkersEffect : Effect {
    object CloseScreen : UsersMarkersEffect()
}

// Add enum for modes
enum class Mode {
    CHOOSE, MAINTAIN
}

interface UsersMarkersComponent :
    MviComponent<UsersMarkersIntent, UsersMarkersState, UsersMarkersEffect> {
    val mode: Mode
}

class DefaultUsersMarkersComponent(
    componentContext: ComponentContext,
    override val mode: Mode = Mode.MAINTAIN,
    private val onClose: () -> Unit,
    private val onSelectMarker: (MapMarker?) -> Unit = {},
    initialMarkers: List<MapMarker> = emptyList(),
    private val repository: UsersMarkersRepository = getKoin().get(),
    private val locationManager: LocationManager = getKoin().get(),
) : UsersMarkersComponent, ComponentContext by componentContext {
    override val _state = MutableStateFlow(UsersMarkersState(markers = initialMarkers))
    override val _effect = Channel<UsersMarkersEffect>(Channel.BUFFERED)

    init {
        coroutineScope.launch(Dispatchers.IO) {
            repository.markersFlow.collect { markers ->
                _state.update { it.copy(markers = markers) }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            locationManager?.getLastKnownLocation()?.let { location ->
                val currentLocationMarker = MapMarker.new(
                    latitude = location.lat,
                    longitude = location.lon,
                    lastIndex = -1,
                    lastSymbol = null,
                    title = "Current Location",
                )
                _state.update {
                    it.copy(currentLocation = currentLocationMarker)
                }
            }
        }
    }

    override suspend fun processIntent(intent: UsersMarkersIntent) {
        when (intent) {
            is UsersMarkersIntent.SelectMarker -> {
                onSelectMarker(intent.marker)
            }

            is UsersMarkersIntent.EditMarker -> {
                repository.updateMarker(intent.marker)
            }

            is UsersMarkersIntent.DeleteMarker -> {
                repository.deleteMarker(intent.markerId)
            }

            is UsersMarkersIntent.CloseScreen -> {
                onClose()
            }
        }
    }
}
