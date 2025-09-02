package pl.soulsnaps.features.capturemoment

import pl.soulsnaps.permissions.PermissionManager
import pl.soulsnaps.permissions.PermissionType
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Enhanced location service that integrates with permission system
 */
interface LocationService {
    /**
     * Get current location
     */
    suspend fun getCurrentLocation(): PhotoLocation?
    
    /**
     * Get location updates stream
     */
    suspend fun getLocationUpdates(): List<PhotoLocation>
    
    /**
     * Search for locations by query
     */
    suspend fun searchLocations(query: String): List<PlaceInfo>
    
    /**
     * Get location by coordinates
     */
    suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): PlaceInfo?
    
    /**
     * Get nearby places
     */
    suspend fun getNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double = 10.0): List<PlaceInfo>
    
    /**
     * Check if location permission is granted
     */
    suspend fun hasLocationPermission(): Boolean
    
    /**
     * Request location permission
     */
    suspend fun requestLocationPermission(): Boolean
    
    /**
     * Check if location services are available
     */
    suspend fun isLocationAvailable(): Boolean
    
    /**
     * Get last known location
     */
    suspend fun getLastKnownLocation(): PhotoLocation?
}

/**
 * Place information
 */
data class PlaceInfo(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val placeType: PlaceType,
    val rating: Float? = null,
    val photos: List<String> = emptyList()
)

/**
 * Location service implementation
 */
class LocationServiceImpl(
    private val permissionManager: PermissionManager
) : LocationService {
    
    override suspend fun getCurrentLocation(): PhotoLocation? {
        if (!hasLocationPermission()) {
            val granted = requestLocationPermission()
            if (!granted) return null
        }
        
        // Implementation would depend on platform-specific location services
        return null
    }
    
    override suspend fun getLocationUpdates(): List<PhotoLocation> {
        if (!hasLocationPermission()) return emptyList()
        
        // Implementation would depend on platform-specific location services
        return emptyList()
    }
    
    override suspend fun searchLocations(query: String): List<PlaceInfo> {
        if (!hasLocationPermission()) return emptyList()
        
        // Implementation would depend on platform-specific location services
        return emptyList()
    }
    
    override suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): PlaceInfo? {
        if (!hasLocationPermission()) return null
        
        // Implementation would depend on platform-specific location services
        return null
    }
    
    override suspend fun getNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double): List<PlaceInfo> {
        if (!hasLocationPermission()) return emptyList()
        
        // Implementation would depend on platform-specific location services
        return emptyList()
    }
    
    override suspend fun hasLocationPermission(): Boolean {
        return permissionManager.isPermissionGranted(PermissionType.LOCATION)
    }
    
    override suspend fun requestLocationPermission(): Boolean {
        return permissionManager.requestPermission(PermissionType.LOCATION)
    }
    
    override suspend fun isLocationAvailable(): Boolean {
        return hasLocationPermission()
    }
    
    override suspend fun getLastKnownLocation(): PhotoLocation? {
        if (!hasLocationPermission()) return null
        
        // Implementation would depend on platform-specific location services
        return null
    }
}

/**
 * Location data
 */
data class LocationData(
    val id: String? = null,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postalCode: String?,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = getCurrentTimeMillis(),
    val placeId: String? = null,
    val placeType: PlaceType? = null
)

/**
 * Place data from location services
 */
data class PlaceData(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val placeType: PlaceType,
    val rating: Float? = null,
    val photos: List<String> = emptyList(),
    val phoneNumber: String? = null,
    val website: String? = null,
    val openingHours: String? = null
)

/**
 * Place types
 */
enum class PlaceType {
    RESTAURANT, CAFE, SHOP, PARK, MUSEUM, THEATER, HOSPITAL, SCHOOL, UNIVERSITY,
    OFFICE, HOME, HOTEL, AIRPORT, STATION, PARKING, GAS_STATION, BANK, PHARMACY,
    OTHER
}

/**
 * Location statistics
 */
data class LocationStats(
    val totalLocations: Int,
    val countriesVisited: Int,
    val citiesVisited: Int,
    val mostVisitedCity: String?,
    val mostVisitedCountry: String?,
    val averageAccuracy: Float?,
    val lastLocationDate: Long?
)

/**
 * Location search options
 */
data class LocationSearchOptions(
    val query: String,
    val location: LocationData? = null,
    val radiusKm: Double = 50.0,
    val placeTypes: List<PlaceType> = emptyList(),
    val maxResults: Int = 20
)

/**
 * Location accuracy levels
 */
enum class LocationAccuracy {
    LOW, MEDIUM, HIGH, BEST
}
