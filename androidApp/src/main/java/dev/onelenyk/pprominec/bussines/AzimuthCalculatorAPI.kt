package dev.onelenyk.pprominec.bussines

data class GeoCoordinate(val lat: Double, val lon: Double)

/**
 * API for calculating azimuth and target coordinates using geodesic calculations.
 */
object AzimuthCalculatorAPI {

    /**
     * @deprecated Use calculateTargetPosition and calculateAzimuthFromB separately instead
     */
    @Deprecated(
        "Use calculateTargetPosition and calculateAzimuthFromB separately instead",
        ReplaceWith("calculateTargetPosition(pointA, azimuthFromA, distanceKm) and calculateAzimuthFromB(pointB, targetPosition)"),
    )
    fun calculate(
        pointA: GeoCoordinate,
        azimuthFromA: Double,
        distanceKm: Double,
        pointB: GeoCoordinate,
    ): AzimuthCalculationResult {
        val targetPosition = calculateTargetPosition(pointA, azimuthFromA, distanceKm)
        val azimuthFromB = calculateAzimuthFromB(pointB, targetPosition)
        val distanceFromB = calculateDistanceFromB(pointB, targetPosition)

        return AzimuthCalculationResult(
            target = targetPosition,
            azimuthFromB = azimuthFromB,
            distanceFromB = distanceFromB,
        )
    }

    /**
     * Calculate target position based on observation point, azimuth, and distance.
     * @param observationPoint GeoCoordinate of the observation point (Point A)
     * @param azimuth Azimuth from observation point (degrees, from North, clockwise)
     * @param distanceKm Distance from observation point to target (km)
     * @return TargetPosition with latitude and longitude of the target
     */
    fun calculateTargetPosition(
        observationPoint: GeoCoordinate,
        azimuth: Double,
        distanceKm: Double,
    ): GeoCoordinate {
        val (latTarget, lonTarget) = GeodesyUtils.directGeodesic(
            observationPoint.lat,
            observationPoint.lon,
            azimuth,
            distanceKm,
        )
        return GeoCoordinate(latTarget, lonTarget)
    }

    /**
     * Calculate azimuth from Point B to target.
     * @param pointB GeoCoordinate of point B
     * @param targetPosition GeoCoordinate of the target
     * @return Azimuth in degrees
     */
    fun calculateAzimuthFromB(
        pointB: GeoCoordinate,
        targetPosition: GeoCoordinate,
    ): Double {
        return GeodesyUtils.inverseGeodesic(
            pointB.lat,
            pointB.lon,
            targetPosition.lat,
            targetPosition.lon,
        )
    }

    /**
     * Calculate distance from Point B to target.
     * @param pointB GeoCoordinate of point B
     * @param targetPosition GeoCoordinate of the target
     * @return Distance in kilometers
     */
    fun calculateDistanceFromB(
        pointB: GeoCoordinate,
        targetPosition: GeoCoordinate,
    ): Double {
        return GeodesyUtils.calculateDistance(
            pointB.lat,
            pointB.lon,
            targetPosition.lat,
            targetPosition.lon,
        )
    }
}

/**
 * @deprecated Use separate functions for target calculation and azimuth calculation instead
 */
@Deprecated("Use separate functions for target calculation and azimuth calculation instead")
data class AzimuthCalculationResult(
    val target: GeoCoordinate,
    val azimuthFromB: Double,
    val distanceFromB: Double,
)

/**
 * Utility for parsing and normalizing raw user input for coordinates, azimuth, and distance.
 */
object AzimuthInputNormalizer {
    /**
     * Parses and normalizes raw input values (strings) to Double, replacing comma with dot and trimming whitespace.
     * Returns null if any value is invalid or empty.
     */
    fun parseCoordinate(
        latRaw: String,
        lonRaw: String,
    ): GeoCoordinate? {
        if (latRaw.isBlank() || lonRaw.isBlank()) return null
        return try {
            val lat = latRaw.replace(',', '.').trim().toDouble()
            val lon = lonRaw.replace(',', '.').trim().toDouble()
            GeoCoordinate(lat, lon)
        } catch (e: Exception) {
            null
        }
    }

    fun parseAzimuth(azimuthRaw: String): Double? =
        azimuthRaw.replace(',', '.').trim().toDoubleOrNull()

    fun parseDistance(distanceRaw: String): Double? =
        distanceRaw.replace(',', '.').trim().toDoubleOrNull()
}
