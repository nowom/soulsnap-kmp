package pl.soulsnaps.features.location

/**
 * Platform-specific location permission manager
 * Handles system permission dialogs and checks
 */
expect class LocationPermissionManager {
    /**
     * Check if location permissions are currently granted
     */
    suspend fun hasLocationPermission(): Boolean
    
    /**
     * Request location permissions with system dialog
     * @return true if permissions were granted, false if denied
     */
    suspend fun requestLocationPermission(): Boolean
    
    /**
     * Check if we should show permission rationale
     * (Android only - always false on iOS)
     */
    suspend fun shouldShowPermissionRationale(): Boolean
    
    /**
     * Open app settings for manual permission grant
     * (when user denies permissions permanently)
     */
    suspend fun openAppSettings(): Boolean
}

/**
 * Result of permission request
 */
enum class PermissionResult {
    GRANTED,
    DENIED,
    DENIED_PERMANENTLY,
    UNAVAILABLE
}

