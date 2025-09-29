package pl.soulsnaps.analytics

/**
 * iOS implementation of FirebaseAnalyticsManagerFactory
 */
actual object FirebaseAnalyticsManagerFactory {
    actual fun create(): FirebaseAnalyticsManager {
        return IosFirebaseAnalyticsManager()
    }
}

