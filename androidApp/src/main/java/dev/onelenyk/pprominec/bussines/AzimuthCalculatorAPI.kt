package dev.onelenyk.pprominec.bussines

data class GeoCoordinate(val lat: Double, val lon: Double)

data class AzimuthCalculationResult(
    val target: GeoCoordinate,
    val azimuthFromB: Double,
)

object AzimuthCalculatorAPI {
    /**
     * Calculate target coordinates and azimuth from B to target.
     * @param pointA GeoCoordinate of point A
     * @param azimuthFromA Azimuth from A (degrees, from North, clockwise)
     * @param distanceKm Distance from A to target (km)
     * @param pointB GeoCoordinate of point B
     * @return AzimuthCalculationResult with target coordinates and azimuth from B
     */
    fun calculate(
        pointA: GeoCoordinate,
        azimuthFromA: Double,
        distanceKm: Double,
        pointB: GeoCoordinate,
    ): AzimuthCalculationResult {
        val (latTarget, lonTarget) = GeodesyUtils.directGeodesic(pointA.lat, pointA.lon, azimuthFromA, distanceKm)
        val azimuthFromB = GeodesyUtils.inverseGeodesic(pointB.lat, pointB.lon, latTarget, lonTarget)
        return AzimuthCalculationResult(GeoCoordinate(latTarget, lonTarget), azimuthFromB)
    }
}

/**
 * Utility for parsing and normalizing raw user input for coordinates, azimuth, and distance.
 */
object AzimuthInputNormalizer {
    /**
     * Parses and normalizes raw input values (strings) to Double, replacing comma with dot and trimming whitespace.
     * Returns null if any value is invalid.
     */
    fun parseCoordinate(
        latRaw: String,
        lonRaw: String,
    ): GeoCoordinate? {
        val lat = latRaw.replace(',', '.').trim().toDoubleOrNull()
        val lon = lonRaw.replace(',', '.').trim().toDoubleOrNull()
        return if (lat != null && lon != null) GeoCoordinate(lat, lon) else null
    }

    fun parseAzimuth(azimuthRaw: String): Double? = azimuthRaw.replace(',', '.').trim().toDoubleOrNull()

    fun parseDistance(distanceRaw: String): Double? = distanceRaw.replace(',', '.').trim().toDoubleOrNull()
}
