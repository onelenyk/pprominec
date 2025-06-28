package dev.onelenyk.pprominec.bussines

import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import java.util.Locale

object GeodesyUtils {
    /**
     * Direct geodetic calculation: from (latA, lonA) with azimuth and distance, find target coordinates.
     * @param latA Latitude of point A (degrees)
     * @param lonA Longitude of point A (degrees)
     * @param azimuthFromA Azimuth from A (degrees, from North, clockwise)
     * @param distanceKm Distance from A to target (km)
     * @return Pair(latTarget, lonTarget)
     */
    fun directGeodesic(
        latA: Double,
        lonA: Double,
        azimuthFromA: Double,
        distanceKm: Double,
    ): Pair<Double, Double> {
        val result: GeodesicData = Geodesic.WGS84.Direct(latA, lonA, azimuthFromA, distanceKm * 1000)
        return Pair(result.lat2, result.lon2)
    }

    /**
     * Inverse geodetic calculation: from (latB, lonB) to (latTarget, lonTarget), find azimuth from B.
     * @return Azimuth from B to target (degrees, [0, 360))
     */
    fun inverseGeodesic(
        latB: Double,
        lonB: Double,
        latTarget: Double,
        lonTarget: Double,
    ): Double {
        val result: GeodesicData = Geodesic.WGS84.Inverse(latB, lonB, latTarget, lonTarget)
        // azi1 is the azimuth from B to target
        var azimuth = result.azi1
        if (azimuth < 0) azimuth += 360.0
        return String.format(Locale.US, "%.2f", azimuth).toDouble()
    }
}
