package dev.onelenyk.pprominec.presentation.components.main

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dev.onelenyk.pprominec.bussines.GeoCoordinate
import kotlinx.coroutines.tasks.await

class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): GeoCoordinate? {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                GeoCoordinate(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 
