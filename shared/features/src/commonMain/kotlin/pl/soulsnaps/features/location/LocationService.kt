package pl.soulsnaps.features.location

/**
 * Platform-specific GPS location service
 * Provides current device location using native GPS APIs
 */
expect class LocationService {
    /**
     * Get current device location
     * @return LocationResult with coordinates or error
     */
    suspend fun getCurrentLocation(): LocationResult
    
    /**
     * Check if location permissions are granted
     * @return true if permissions are available
     */
    suspend fun hasLocationPermission(): Boolean
    
    /**
     * Request location permissions (if needed)
     * @return true if permissions were granted
     */
    suspend fun requestLocationPermission(): Boolean
}

/**
 * Result of location request
 */
sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float? = null,
        val address: String? = null
    ) : LocationResult()
    
    sealed class Error : LocationResult() {
        data object PermissionDenied : Error()
        data object LocationDisabled : Error()
        data object Timeout : Error()
        data class Unknown(val message: String) : Error()
    }
}

/**
 * Location permission status
 */
enum class LocationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
    DENIED_FOREVER
}


