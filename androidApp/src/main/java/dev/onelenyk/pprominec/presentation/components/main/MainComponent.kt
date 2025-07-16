package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import dev.onelenyk.pprominec.bussines.AzimuthCalculatorAPI
import dev.onelenyk.pprominec.bussines.AzimuthInputNormalizer
import dev.onelenyk.pprominec.bussines.GeoCoordinate
import dev.onelenyk.pprominec.presentation.mvi.Effect
import dev.onelenyk.pprominec.presentation.mvi.Intent
import dev.onelenyk.pprominec.presentation.mvi.MviComponent
import dev.onelenyk.pprominec.presentation.mvi.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

data class Sample(
    val name: String,
    val pointA: GeoCoordinate,
    val azimuthFromA: Double,
    val distanceKm: Double,
    val pointB: GeoCoordinate,
    val expectedLat: Double,
    val expectedLon: Double,
    val expectedAzimuthFromB: Double,
    val expectedDistanceFromB: Double,
)

data class InputData(
    val pointA: GeoCoordinate? = null,
    val azimuthFromA: String = "",
    val distanceKm: String = "",
    val pointB: GeoCoordinate? = null,
)

data class OutputData(
    val targetPosition: GeoCoordinate? = null,
    val azimuthFromB: Double? = null,
    val distanceFromB: Double? = null,
)

data class MainState(
    val inputData: InputData = InputData(),
    val outputData: OutputData = OutputData(),
    val isRenderModeA: Boolean = false,
    val isRenderModeB: Boolean = false,
    val isRenderModeC: Boolean = false,
) : State

sealed class MainIntent : Intent {
    data class OnPointAChange(val coordinate: GeoCoordinate?) : MainIntent()
    data class OnAzimuthFromAChange(val value: String) : MainIntent()
    data class OnDistanceKmChange(val value: String) : MainIntent()
    data class OnPointBChange(val coordinate: GeoCoordinate?) : MainIntent()
    data class SetRenderModeA(val enabled: Boolean) : MainIntent()
    data class SetRenderModeB(val enabled: Boolean) : MainIntent()
    data class SetRenderModeC(val enabled: Boolean) : MainIntent()
    data class LoadSample(val sample: Sample) : MainIntent()
    data class ShowUserMarkerDialog(val requestLocationType: LocationButtonType) : MainIntent()
    object HideUserMarkerDialog : MainIntent()
    data class OnLocationButtonClick(val type: LocationButtonType) : MainIntent()
    // Add more as needed
}

enum class LocationButtonType { POINT_A, POINT_B, TARGET }

sealed class MainEffect : Effect {
    data class ShowToast(val message: String) : MainEffect()
    data class CopyToClipboard(val text: String) : MainEffect()
    // Add more as needed
}

interface MainComponent : MviComponent<MainIntent, MainState, MainEffect> {
    val dialog: Value<ChildSlot<DialogConfig, Dialog>>

    // Remove old mutation methods
    @Serializable
    sealed class DialogConfig {
        @Serializable
        data class UserMarker(
            val requestLocationType: LocationButtonType,
        ) : DialogConfig()
    }

    sealed class Dialog {
        data class UserMarkers(val usersMarkersComponent: UsersMarkersComponent) : Dialog()
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
) : MainComponent, ComponentContext by componentContext {
    override val _state = MutableStateFlow(MainState())
    override val _effect = Channel<MainEffect>(Channel.BUFFERED)

    private val dialogNavigation = SlotNavigation<MainComponent.DialogConfig>()
    override val dialog: Value<ChildSlot<MainComponent.DialogConfig, MainComponent.Dialog>> =
        childSlot(
            source = dialogNavigation,
            serializer = MainComponent.DialogConfig.serializer(),
            handleBackButton = true,
        ) { config, componentContext ->
            when (config) {
                is MainComponent.DialogConfig.UserMarker -> MainComponent.Dialog.UserMarkers(
                    DefaultUsersMarkersComponent(
                        componentContext = componentContext,
                        mode = Mode.CHOOSE, // Pass CHOOSE mode when opened from main page
                        onClose = { dialogNavigation.dismiss() },
                        onSelectMarker = { marker ->
                            when (config.requestLocationType) {
                                LocationButtonType.POINT_A -> {
                                    updateState(
                                        _state.value.copy(
                                            inputData = _state.value.inputData.copy(pointA = marker.geo()),
                                        ),
                                    )
                                    updateCalculations()
                                }

                                LocationButtonType.POINT_B -> {
                                    updateState(
                                        _state.value.copy(
                                            inputData = _state.value.inputData.copy(pointB = marker.geo()),
                                        ),
                                    )
                                    updateCalculations()
                                }

                                LocationButtonType.TARGET -> TODO()
                            }
                        },
                    ),
                )
            }
        }

    override suspend fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.OnPointAChange -> {
                updateState(_state.value.copy(inputData = _state.value.inputData.copy(pointA = intent.coordinate)))
                updateCalculations()
            }

            is MainIntent.OnAzimuthFromAChange -> {
                updateState(_state.value.copy(inputData = _state.value.inputData.copy(azimuthFromA = intent.value)))
                updateCalculations()
            }

            is MainIntent.OnDistanceKmChange -> {
                updateState(_state.value.copy(inputData = _state.value.inputData.copy(distanceKm = intent.value)))
                updateCalculations()
            }

            is MainIntent.OnPointBChange -> {
                updateState(_state.value.copy(inputData = _state.value.inputData.copy(pointB = intent.coordinate)))
                updateCalculations()
            }

            is MainIntent.SetRenderModeA -> {
                updateState(_state.value.copy(isRenderModeA = intent.enabled))
            }

            is MainIntent.SetRenderModeB -> {
                updateState(_state.value.copy(isRenderModeB = intent.enabled))
            }

            is MainIntent.SetRenderModeC -> {
                updateState(_state.value.copy(isRenderModeC = intent.enabled))
            }

            is MainIntent.LoadSample -> {
                updateState(
                    _state.value.copy(
                        inputData = InputData(
                            pointA = intent.sample.pointA,
                            azimuthFromA = intent.sample.azimuthFromA.toString(),
                            distanceKm = intent.sample.distanceKm.toString(),
                            pointB = intent.sample.pointB,
                        ),
                    ),
                )
                updateCalculations()
            }

            is MainIntent.ShowUserMarkerDialog -> {
                dialogNavigation.activate(MainComponent.DialogConfig.UserMarker(intent.requestLocationType))
            }

            is MainIntent.HideUserMarkerDialog -> {
                dialogNavigation.dismiss()
            }

            is MainIntent.OnLocationButtonClick -> {
                // For now, just emit a toast effect for demonstration
                val label = when (intent.type) {
                    LocationButtonType.POINT_A -> "Location A"
                    LocationButtonType.POINT_B -> "Location B"
                    LocationButtonType.TARGET -> "Target"
                }

                processIntent(MainIntent.ShowUserMarkerDialog(intent.type))
                //  _effect.send(MainEffect.ShowToast("Location button clicked: $label"))
            }
        }
    }

    private fun updateCalculations() {
        val currentState = _state.value
        val input = currentState.inputData
        if (input.pointA == null || input.azimuthFromA.isBlank() || input.distanceKm.isBlank()) {
            updateState(currentState.copy(outputData = OutputData()))
            return
        }
        val pointA = input.pointA
        val azimuth = AzimuthInputNormalizer.parseAzimuth(input.azimuthFromA)
        val distance = AzimuthInputNormalizer.parseDistance(input.distanceKm)
        if (pointA == null || azimuth == null || distance == null) {
            updateState(currentState.copy(outputData = OutputData()))
            return
        }
        val targetPosition = try {
            AzimuthCalculatorAPI.calculateTargetPosition(pointA, azimuth, distance)
        } catch (e: Exception) {
            null
        }
        if (targetPosition == null) {
            updateState(currentState.copy(outputData = OutputData()))
            return
        }
        var azimuthFromB: Double?
        var distanceFromB: Double?
        if (input.pointB == null) {
            azimuthFromB = null
            distanceFromB = null
        } else {
            val pointB = input.pointB
            try {
                azimuthFromB = AzimuthCalculatorAPI.calculateAzimuthFromB(pointB, targetPosition)
                distanceFromB = AzimuthCalculatorAPI.calculateDistanceFromB(pointB, targetPosition)
            } catch (e: Exception) {
                azimuthFromB = null
                distanceFromB = null
            }
        }
        updateState(
            currentState.copy(
                outputData = OutputData(
                    targetPosition = targetPosition,
                    azimuthFromB = azimuthFromB,
                    distanceFromB = distanceFromB,
                ),
            ),
        )
    }
}
