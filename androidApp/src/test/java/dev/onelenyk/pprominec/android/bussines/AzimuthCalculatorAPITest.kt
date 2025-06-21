package dev.onelenyk.pprominec.bussines

import org.junit.jupiter.api.Assertions.assertEquals
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

            assertEquals(sample.expectedLat, result.target.lat, 0.0001)
            assertEquals(sample.expectedLon, result.target.lon, 0.0001)
            assertEquals(sample.expectedAzimuthFromB, result.azimuthFromB, 0.01)
        }
    }
}
