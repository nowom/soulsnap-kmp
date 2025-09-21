package pl.soulsnaps.features.location

import pl.soulsnaps.data.network.SoulSnapApi

/**
 * Platform-specific factory for LocationService
 * Handles platform dependencies like Context (Android) and permissions
 */
expect object LocationServiceFactory {
    fun create(soulSnapApi: SoulSnapApi): LocationService
}


