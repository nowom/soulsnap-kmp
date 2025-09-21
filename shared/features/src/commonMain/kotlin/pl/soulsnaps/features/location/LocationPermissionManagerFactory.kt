package pl.soulsnaps.features.location

/**
 * Platform-specific factory for LocationPermissionManager
 */
expect object LocationPermissionManagerFactory {
    fun create(): LocationPermissionManager
}

