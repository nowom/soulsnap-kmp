package pl.soulsnaps.features.location

import android.content.Context
import org.koin.java.KoinJavaComponent.inject
import pl.soulsnaps.data.network.SoulSnapApi

/**
 * Android factory for LocationService
 * Injects Android Context from Koin
 */
actual object LocationServiceFactory {
    actual fun create(soulSnapApi: SoulSnapApi): LocationService {
        val context: Context by inject(Context::class.java)
        val permissionManager = LocationPermissionManagerFactory.create()
        return LocationService(context, soulSnapApi, permissionManager)
    }
}

