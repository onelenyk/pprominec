package dev.onelenyk.pprominec

import dev.onelenyk.pprominec.bussines.AzimuthCalculatorAPI
import dev.onelenyk.pprominec.presentation.components.MainState.Companion.samples
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AzimuthCalculatorAPITest {

    @Test
    fun testSamples() {
        samples.forEach { sample ->
            val result = AzimuthCalculatorAPI.calculate(
                sample.pointA,
                sample.azimuthFromA,
                sample.distanceKm,
                sample.pointB
            )

            Assertions.assertEquals(sample.expectedLat, result.target.lat, 0.0001)
            Assertions.assertEquals(sample.expectedLon, result.target.lon, 0.0001)
            Assertions.assertEquals(sample.expectedAzimuthFromB, result.azimuthFromB, 0.01)
        }
    }
}