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
import dev.onelenyk.pprominec.presentation.ui.MapMarker
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

enum class InputSource { MANUAL, MARKER }

data class InputData(
    val pointALat: String = "",
    val pointALon: String = "",
    val pointBLat: String = "",
    val pointBLon: String = "",
    val azimuthFromA: String = "",
    val distanceKm: String = "",
    val pointAInputSource: InputSource = InputSource.MANUAL,
    val pointAMapMarker: MapMarker? = null,
    val pointBInputSource: InputSource = InputSource.MANUAL,
    val pointBMapMarker: MapMarker? = null,
) {
    val pointA: GeoCoordinate?
        get() = pointALat.toDoubleOrNull()?.let { lat ->
            pointALon.toDoubleOrNull()?.let { lon ->
                GeoCoordinate(lat, lon)
            }
        }
    val pointB: GeoCoordinate?
        get() = pointBLat.toDoubleOrNull()?.let { lat ->
            pointBLon.toDoubleOrNull()?.let { lon ->
                GeoCoordinate(lat, lon)
            }
        }
}

data class OutputData(
    val targetPosition: GeoCoordinate? = null,
    val azimuthFromB: Double? = null,
    val distanceFromB: Double? = null,
)

data class MainState(
    val inputData: InputData = InputData(),
    val outputData: OutputData = OutputData(),
    val isRenderModeB: Boolean = false,
    val isRenderModeC: Boolean = false,
) : State

sealed class MainIntent : Intent {
    data class OnPointALatChange(val lat: String) : MainIntent()
    data class OnPointALonChange(val lon: String) : MainIntent()
    data class OnAzimuthFromAChange(val value: String) : MainIntent()
    data class OnDistanceKmChange(val value: String) : MainIntent()
    data class OnPointBChange(
        val coordinate: GeoCoordinate?,
        val source: InputSource = InputSource.MANUAL,
    ) : MainIntent()

    data class OnPointBLatChange(val lat: String) : MainIntent()
    data class OnPointBLonChange(val lon: String) : MainIntent()
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
                                    if (marker == null) {
                                        updateState(
                                            _state.value.copy(
                                                inputData = _state.value.inputData.copy(
                                                    pointALat = "",
                                                    pointALon = "",
                                                    pointAInputSource = InputSource.MANUAL,
                                                    pointAMapMarker = null,
                                                ),
                                            ),
                                        )
                                    } else {
                                        updateState(
                                            _state.value.copy(
                                                inputData = _state.value.inputData.copy(
                                                    pointALat = marker.latitude.toString(),
                                                    pointALon = marker.longitude.toString(),
                                                    pointAInputSource = InputSource.MARKER,
                                                    pointAMapMarker = marker,
                                                ),
                                            ),
                                        )
                                    }
                                    updateCalculations()
                                    dialogNavigation.dismiss()
                                    return@DefaultUsersMarkersComponent
                                }

                                LocationButtonType.POINT_B -> {
                                    if (marker == null) {
                                        updateState(
                                            _state.value.copy(
                                                inputData = _state.value.inputData.copy(
                                                    pointBLat = "",
                                                    pointBLon = "",
                                                    pointBInputSource = InputSource.MANUAL,
                                                    pointBMapMarker = null,
                                                ),
                                            ),
                                        )
                                    } else {
                                        updateState(
                                            _state.value.copy(
                                                inputData = _state.value.inputData.copy(
                                                    pointBLat = marker.latitude.toString(),
                                                    pointBLon = marker.longitude.toString(),
                                                    pointBInputSource = InputSource.MARKER,
                                                    pointBMapMarker = marker,
                                                ),
                                            ),
                                        )
                                    }
                                    updateCalculations()
                                    dialogNavigation.dismiss()
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
            is MainIntent.OnPointALatChange -> {
                val newLat = intent.lat
                val marker = _state.value.inputData.pointAMapMarker
                val shouldClearMarker = marker != null && newLat != marker.latitude.toString()
                updateState(
                    _state.value.copy(
                        inputData = _state.value.inputData.copy(
                            pointALat = newLat,
                            pointAInputSource = if (shouldClearMarker) InputSource.MANUAL else _state.value.inputData.pointAInputSource,
                            pointAMapMarker = if (shouldClearMarker) null else marker,
                        ),
                    ),
                )
                updateCalculations()
            }

            is MainIntent.OnPointALonChange -> {
                val newLon = intent.lon
                val marker = _state.value.inputData.pointAMapMarker
                val shouldClearMarker = marker != null && newLon != marker.longitude.toString()
                updateState(
                    _state.value.copy(
                        inputData = _state.value.inputData.copy(
                            pointALon = newLon,
                            pointAInputSource = if (shouldClearMarker) InputSource.MANUAL else _state.value.inputData.pointAInputSource,
                            pointAMapMarker = if (shouldClearMarker) null else marker,
                        ),
                    ),
                )

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
                updateState(
                    _state.value.copy(
                        inputData = _state.value.inputData.copy(
                            pointBLat = intent.coordinate?.lat?.toString() ?: "",
                            pointBLon = intent.coordinate?.lon?.toString() ?: "",
                            pointBInputSource = intent.source,
                            pointBMapMarker = if (intent.source == InputSource.MANUAL) null else _state.value.inputData.pointBMapMarker,
                        ),
                    ),
                )
                updateCalculations()
            }

            is MainIntent.OnPointBLatChange -> {
                val newLat = intent.lat
                val marker = _state.value.inputData.pointBMapMarker
                val shouldClearMarker = marker != null && newLat != marker.latitude.toString()
                updateState(
                    _state.value.copy(
                        inputData = _state.value.inputData.copy(
                            pointBLat = newLat,
                            pointBInputSource = if (shouldClearMarker) InputSource.MANUAL else _state.value.inputData.pointBInputSource,
                            pointBMapMarker = if (shouldClearMarker) null else marker,
                        ),
                    ),
                )
                updateCalculations()
            }

            is MainIntent.OnPointBLonChange -> {
                val newLon = intent.lon
                val marker = _state.value.inputData.pointBMapMarker
                val shouldClearMarker = marker != null && newLon != marker.longitude.toString()
                updateState(
                    _state.value.copy(
                        inputData = _state.value.inputData.copy(
                            pointBLon = newLon,
                            pointBInputSource = if (shouldClearMarker) InputSource.MANUAL else _state.value.inputData.pointBInputSource,
                            pointBMapMarker = if (shouldClearMarker) null else marker,
                        ),
                    ),
                )
                updateCalculations()
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
                            pointALat = intent.sample.pointA.lat.toString(),
                            pointALon = intent.sample.pointA.lon.toString(),
                            azimuthFromA = intent.sample.azimuthFromA.toString(),
                            distanceKm = intent.sample.distanceKm.toString(),
                            pointBLat = intent.sample.pointB.lat.toString(),
                            pointBLon = intent.sample.pointB.lon.toString(),
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
                if (intent.type == LocationButtonType.TARGET) {
                    // TODO open map
                } else {
                    processIntent(MainIntent.ShowUserMarkerDialog(intent.type))
                }
            }
        }
    }

    private fun updateCalculations() {
        val currentState = _state.value
        val input = currentState.inputData

        val pointA = input.pointA
        val pointB = input.pointB
        val azimuth = AzimuthInputNormalizer.parseAzimuth(input.azimuthFromA)
        val distance = AzimuthInputNormalizer.parseDistance(input.distanceKm)

        if (pointA == null || azimuth == null || distance == null) {
            updateState(
                currentState.copy(
                    outputData = OutputData(),
                ),
            )
            return
        }
        val targetPosition = try {
            AzimuthCalculatorAPI.calculateTargetPosition(pointA, azimuth, distance)
        } catch (e: Exception) {
            null
        }
        if (targetPosition == null) {
            return
        }
        var azimuthFromB: Double?
        var distanceFromB: Double?

        if (pointB == null) {
            azimuthFromB = null
            distanceFromB = null
        } else {
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
