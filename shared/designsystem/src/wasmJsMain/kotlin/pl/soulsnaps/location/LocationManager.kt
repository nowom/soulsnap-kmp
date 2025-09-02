package pl.soulsnaps.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import pl.soulsnaps.location.PhotoLocation
import pl.soulsnaps.location.PlaceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
actual fun rememberLocationManager(): LocationManager {
    return remember { LocationManager() }
}

actual class LocationManager {
    actual suspend fun getCurrentLocation(): PhotoLocation? {
        // Location services are not available in WebAssembly
        return null
    }
    
    actual fun getLocationUpdates(intervalMs: Long): Flow<PhotoLocation> {
        // Location services are not available in WebAssembly
        return flowOf()
    }
    
    actual suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): PlaceInfo? {
        // Location services are not available in WebAssembly
        return null
    }
    
    actual suspend fun searchPlaces(query: String): List<PlaceInfo> {
        // Location services are not available in WebAssembly
        return emptyList()
    }
    
    actual suspend fun getNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double): List<PlaceInfo> {
        // Location services are not available in WebAssembly
        return emptyList()
    }
    
    actual fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        // Location services are not available in WebAssembly
        return 0f
    }
}
