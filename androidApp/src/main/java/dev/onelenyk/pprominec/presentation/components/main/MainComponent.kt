package dev.onelenyk.pprominec.presentation.components.main

import com.arkivanov.decompose.ComponentContext
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
)

data class MainState(
    val latA: String = "50.2040236",
    val lonA: String = "24.3845744",
    val azimuthFromA: String = "70",
    val distanceKm: String = "50",
    val latB: String = "50.1802326",
    val lonB: String = "24.4102277",
    val samples: List<Sample> = MainState.samples,
    val hideSamples: Boolean = true,
) {
    companion object {
        val samples =
            listOf(
                Sample(
                    "Sample 1",
                    GeoCoordinate(50.0, 10.0),
                    90.0,
                    100.0,
                    GeoCoordinate(50.0, 11.0),
                    49.991618,
                    11.394621,
                    91.74,
                ),
                Sample(
                    "Sample 2",
                    GeoCoordinate(48.0, 30.0),
                    45.0,
                    50.0,
                    GeoCoordinate(48.2, 30.2),
                    48.316978,
                    30.476699,
                    57.56,
                ),
                Sample(
                    "Sample 3",
                    GeoCoordinate(49.0, 23.0),
                    180.0,
                    120.0,
                    GeoCoordinate(47.9, 23.1),
                    47.920856,
                    23.0,
                    287.27,
                ),
                Sample(
                    "Sample 4",
                    GeoCoordinate(46.5, 32.0),
                    270.0,
                    80.0,
                    GeoCoordinate(46.6, 31.0),
                    46.495253,
                    30.957886,
                    195.52,
                ),
                Sample(
                    "Sample 5",
                    GeoCoordinate(51.0, 24.0),
                    135.0,
                    60.0,
                    GeoCoordinate(50.7, 24.3),
                    50.617073,
                    24.599466,
                    113.42,
                ),
                Sample(
                    "Sample 6",
                    GeoCoordinate(50.45, 30.523),
                    0.0,
                    10.0,
                    GeoCoordinate(50.454, 30.530),
                    50.539897,
                    30.523,
                    357.03,
                ),
                Sample(
                    "Sample 7",
                    GeoCoordinate(50.45, 30.523),
                    90.0,
                    20.0,
                    GeoCoordinate(50.454, 30.530),
                    50.449659,
                    30.804592,
                    91.31,
                ),
                Sample(
                    "Sample 8",
                    GeoCoordinate(50.45, 30.523),
                    135.0,
                    30.0,
                    GeoCoordinate(50.454, 30.530),
                    50.258914,
                    30.820479,
                    136.28,
                ),
                Sample(
                    "Sample 9",
                    GeoCoordinate(50.45, 30.523),
                    225.0,
                    40.0,
                    GeoCoordinate(50.454, 30.530),
                    50.195049,
                    30.12689,
                    225.06,
                ),
                Sample(
                    "Sample 10",
                    GeoCoordinate(50.45, 30.523),
                    315.0,
                    50.0,
                    GeoCoordinate(50.454, 30.530),
                    50.766754,
                    30.021853,
                    314.24,
                ),
            )
    }
}

interface MainComponent {
    val state: StateFlow<MainState>

    fun onLatAChange(value: String)

    fun onLonAChange(value: String)

    fun onAzimuthFromAChange(value: String)

    fun onDistanceKmChange(value: String)

    fun onLatBChange(value: String)

    fun onLonBChange(value: String)

    fun applySample(sample: Sample)

    fun hideSamples()
}

class DefaultMainComponent(
    componentContext: ComponentContext,
) : MainComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow(MainState())
    override val state: StateFlow<MainState>
        get() = _state.asStateFlow()

    override fun onLatAChange(value: String) {
        _state.value = _state.value.copy(latA = value)
    }

    override fun onLonAChange(value: String) {
        _state.value = _state.value.copy(lonA = value)
    }

    override fun onAzimuthFromAChange(value: String) {
        _state.value = _state.value.copy(azimuthFromA = value)
    }

    override fun onDistanceKmChange(value: String) {
        _state.value = _state.value.copy(distanceKm = value)
    }

    override fun onLatBChange(value: String) {
        _state.value = _state.value.copy(latB = value)
    }

    override fun onLonBChange(value: String) {
        _state.value = _state.value.copy(lonB = value)
    }

    override fun hideSamples() {
        _state.value = _state.value.copy(hideSamples = !_state.value.hideSamples)
    }

    override fun applySample(sample: Sample) {
        _state.value =
            _state.value.copy(
                latA = sample.pointA.lat.toString(),
                lonA = sample.pointA.lon.toString(),
                azimuthFromA = sample.azimuthFromA.toString(),
                distanceKm = sample.distanceKm.toString(),
                latB = sample.pointB.lat.toString(),
                lonB = sample.pointB.lon.toString(),
            )
    }
}
