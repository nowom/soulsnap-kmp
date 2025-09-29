package pl.soulsnaps.crashlytics

/**
 * Factory for creating platform-specific CrashlyticsManager instances
 */
expect object CrashlyticsManagerFactory {
    fun create(): CrashlyticsManager
}

