package pl.soulsnaps.location

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import platform.CoreLocation.*
import platform.Foundation.*
import pl.soulsnaps.utils.getCurrentTimeMillis

@Composable
actual fun rememberLocationManager(): LocationManager {
    return remember {
        LocationManager()
    }
}

actual class LocationManager {
    private val locationManager: CLLocationManager
    
    constructor() {
        this.locationManager = CLLocationManager()
        setupLocationManager()
    }
    
    private fun setupLocationManager() {
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 10.0
    }
    
    actual suspend fun getCurrentLocation(): PhotoLocation? {
        return try {
            if (hasLocationPermission()) {
                // For now, return a mock location
                // In a full implementation, this would use CLLocationManager's location
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
            // In a full implementation, this would use CLGeocoder
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
        val location1 = CLLocation(latitude = lat1, longitude = lon1)
        val location2 = CLLocation(latitude = lat2, longitude = lon2)
        return location1.distanceFromLocation(location2).toFloat()
    }
    
    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> true
            else -> false
        }
    }
    
    /**
     * Request location permission
     */
    fun requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
}
