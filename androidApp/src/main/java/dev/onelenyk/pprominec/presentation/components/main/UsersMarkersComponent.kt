package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import dev.onelenyk.pprominec.presentation.coroutineScope
import dev.onelenyk.pprominec.presentation.mvi.Effect
import dev.onelenyk.pprominec.presentation.mvi.Intent
import dev.onelenyk.pprominec.presentation.mvi.MviComponent
import dev.onelenyk.pprominec.presentation.mvi.State
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

sealed class UsersMarkersIntent : Intent {
    data class SelectMarker(val marker: MapMarker) : UsersMarkersIntent()
    data class EditMarker(val marker: MapMarker) : UsersMarkersIntent()
    data class DeleteMarker(val markerId: String) : UsersMarkersIntent()
    object CloseScreen : UsersMarkersIntent()
}

data class UsersMarkersState(
    val markers: List<MapMarker> = emptyList(),
) : State

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
    private val onSelectMarker: (MapMarker) -> Unit = {},
    initialMarkers: List<MapMarker> = emptyList(),
    private val repository: UsersMarkersRepository = getKoin().get(),
) : UsersMarkersComponent, ComponentContext by componentContext {
    override val _state = MutableStateFlow(UsersMarkersState(markers = initialMarkers))
    override val _effect = Channel<UsersMarkersEffect>(Channel.BUFFERED)

    init {
        coroutineScope.launch {
            repository.markersFlow.collect { markers ->
                _state.update { it.copy(markers = markers) }
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
