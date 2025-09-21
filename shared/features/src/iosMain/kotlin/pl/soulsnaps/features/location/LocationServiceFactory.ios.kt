package pl.soulsnaps.features.location

import pl.soulsnaps.data.network.SoulSnapApi

/**
 * iOS factory for LocationService
 * No additional dependencies needed for iOS
 */
actual object LocationServiceFactory {
    actual fun create(soulSnapApi: SoulSnapApi): LocationService {
        val permissionManager = LocationPermissionManagerFactory.create()
        return LocationService(soulSnapApi, permissionManager)
    }
}

