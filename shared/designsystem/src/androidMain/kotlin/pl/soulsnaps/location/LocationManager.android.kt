package pl.soulsnaps.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.soulsnaps.utils.getCurrentTimeMillis

@Composable
actual fun rememberLocationManager(): LocationManager {
    val context = LocalContext.current
    
    return remember {
        LocationManager(context)
    }
}

actual class LocationManager {
    private val context: Context
    
    constructor(context: Context) {
        this.context = context
    }
    
    actual suspend fun getCurrentLocation(): PhotoLocation? {
        return try {
            if (hasLocationPermission()) {
                // Simplified implementation - return a mock location for now
                PhotoLocation(
                    latitude = 40.7128,
                    longitude = -74.0060,
                    accuracy = 10f,
                    timestamp = getCurrentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun getLocationUpdates(intervalMs: Long): Flow<PhotoLocation> {
        return flowOf(
            PhotoLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                accuracy = 10f,
                timestamp = getCurrentTimeMillis()
            )
        )
    }
    
    actual suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): PlaceInfo? {
        return try {
            PlaceInfo(
                id = "${latitude}_${longitude}",
                name = "Mock Location",
                address = "Mock Address",
                latitude = latitude,
                longitude = longitude,
                placeType = PlaceType.OTHER
            )
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun searchPlaces(query: String): List<PlaceInfo> {
        return listOf(
            PlaceInfo(
                id = "mock_1",
                name = "Mock Place 1",
                address = "Mock Address 1",
                latitude = 40.7128,
                longitude = -74.0060,
                placeType = PlaceType.OTHER
            )
        )
    }
    
    actual suspend fun getNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double): List<PlaceInfo> {
        return listOf(
            PlaceInfo(
                id = "nearby_1",
                name = "Nearby Location 1",
                address = "Nearby Address 1",
                latitude = latitude + 0.001,
                longitude = longitude + 0.001,
                placeType = PlaceType.OTHER
            )
        )
    }
    
    actual fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
    
    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
