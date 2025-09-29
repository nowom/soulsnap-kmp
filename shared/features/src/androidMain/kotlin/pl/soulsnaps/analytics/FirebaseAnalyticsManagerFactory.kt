package pl.soulsnaps.analytics

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of FirebaseAnalyticsManagerFactory
 */
actual object FirebaseAnalyticsManagerFactory : KoinComponent {
    actual fun create(): FirebaseAnalyticsManager {
        val context: android.content.Context by inject()
        return AndroidFirebaseAnalyticsManager(context)
    }
}
