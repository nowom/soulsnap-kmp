package pl.soulsnaps.features.location

/**
 * iOS factory for LocationPermissionManager
 * No additional dependencies needed
 */
actual object LocationPermissionManagerFactory {
    actual fun create(): LocationPermissionManager {
        return LocationPermissionManager()
    }
}

