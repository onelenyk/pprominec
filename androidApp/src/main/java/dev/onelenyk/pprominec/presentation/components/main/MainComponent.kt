package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import dev.onelenyk.pprominec.bussines.AzimuthCalculatorAPI
import dev.onelenyk.pprominec.bussines.AzimuthInputNormalizer
import dev.onelenyk.pprominec.bussines.GeoCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val latA: String = "",
    val lonA: String = "",
    val azimuthFromA: String = "",
    val distanceKm: String = "",
    val latB: String = "",
    val lonB: String = "",
)

data class OutputData(
    val targetPosition: GeoCoordinate? = null,
    val azimuthFromB: Double? = null,
    val distanceFromB: Double? = null,
)

data class MainState(
    val inputData: InputData = InputData(),
    val outputData: OutputData = OutputData(),
)

interface MainComponent {
    val state: StateFlow<MainState>
    fun onLatAChange(value: String)
    fun onLonAChange(value: String)
    fun onAzimuthFromAChange(value: String)
    fun onDistanceKmChange(value: String)
    fun onLatBChange(value: String)
    fun onLonBChange(value: String)
    fun loadSample(sample: Sample)
}

class DefaultMainComponent(
    componentContext: ComponentContext,
) : MainComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(MainState())
    override val state: StateFlow<MainState> = _state.asStateFlow()

    override fun onLatAChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(latA = value)
        )
        updateCalculations()
    }

    override fun onLonAChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(lonA = value)
        )
        updateCalculations()
    }

    override fun onAzimuthFromAChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(azimuthFromA = value)
        )
        updateCalculations()
    }

    override fun onDistanceKmChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(distanceKm = value)
        )
        updateCalculations()
    }

    override fun onLatBChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(latB = value)
        )
        updateCalculations()
    }

    override fun onLonBChange(value: String) {
        _state.value = _state.value.copy(
            inputData = _state.value.inputData.copy(lonB = value)
        )
        updateCalculations()
    }

    private fun updateCalculations() {
        val currentState = _state.value
        val input = currentState.inputData

        // Check for empty or invalid input for Point A
        if (input.latA.isBlank() || input.lonA.isBlank() || input.azimuthFromA.isBlank() || input.distanceKm.isBlank()) {
            _state.value = currentState.copy(outputData = OutputData())
            return
        }

        val pointA = AzimuthInputNormalizer.parseCoordinate(input.latA, input.lonA)
        val azimuth = AzimuthInputNormalizer.parseAzimuth(input.azimuthFromA)
        val distance = AzimuthInputNormalizer.parseDistance(input.distanceKm)

        if (pointA == null || azimuth == null || distance == null) {
            _state.value = currentState.copy(outputData = OutputData())
            return
        }

        // Step 1: Calculate target position from Point A data
        val targetPosition = try {
            AzimuthCalculatorAPI.calculateTargetPosition(pointA, azimuth, distance)
        } catch (e: Exception) {
            null
        }

        if (targetPosition == null) {
            _state.value = currentState.copy(outputData = OutputData())
            return
        }

        // Step 2: Calculate azimuth and distance from Point B only if target is valid and Point B input is valid
        var azimuthFromB: Double?
        var distanceFromB: Double?
        if (input.latB.isBlank() || input.lonB.isBlank()) {
            azimuthFromB = null
            distanceFromB = null
        } else {
            val pointB = AzimuthInputNormalizer.parseCoordinate(input.latB, input.lonB)
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
        }

        _state.value = currentState.copy(
            outputData = OutputData(
                targetPosition = targetPosition,
                azimuthFromB = azimuthFromB,
                distanceFromB = distanceFromB
            )
        )
    }

    override fun loadSample(sample: Sample) {
        _state.value = _state.value.copy(
            inputData = InputData(
                latA = sample.pointA.lat.toString(),
                lonA = sample.pointA.lon.toString(),
                azimuthFromA = sample.azimuthFromA.toString(),
                distanceKm = sample.distanceKm.toString(),
                latB = sample.pointB.lat.toString(),
                lonB = sample.pointB.lon.toString(),
            )
        )
        updateCalculations()
    }
}
