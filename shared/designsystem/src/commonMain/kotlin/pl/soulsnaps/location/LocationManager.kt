package pl.soulsnaps.location

import androidx.compose.runtime.Composable
import pl.soulsnaps.location.PhotoLocation
import pl.soulsnaps.location.PlaceInfo
import pl.soulsnaps.location.PlaceType
import kotlinx.coroutines.flow.Flow

@Composable
expect fun rememberLocationManager(): LocationManager

expect class LocationManager {
    /**
     * Get current location
     */
    suspend fun getCurrentLocation(): PhotoLocation?
    
    /**
     * Get location updates as a flow
     */
    fun getLocationUpdates(intervalMs: Long = 10000): Flow<PhotoLocation>
    
    /**
     * Get location by coordinates (reverse geocoding)
     */
    suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): PlaceInfo?
    
    /**
     * Search for places by query
     */
    suspend fun searchPlaces(query: String): List<PlaceInfo>
    
    /**
     * Get nearby places
     */
    suspend fun getNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double): List<PlaceInfo>
    
    /**
     * Calculate distance between two points
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float
}
