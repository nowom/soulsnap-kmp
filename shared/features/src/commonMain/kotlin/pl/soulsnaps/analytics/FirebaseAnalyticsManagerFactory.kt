package pl.soulsnaps.analytics

/**
 * Factory for creating platform-specific FirebaseAnalyticsManager instances
 */
expect object FirebaseAnalyticsManagerFactory {
    fun create(): FirebaseAnalyticsManager
}

