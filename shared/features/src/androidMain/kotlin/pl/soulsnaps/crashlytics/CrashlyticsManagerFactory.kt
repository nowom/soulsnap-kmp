package pl.soulsnaps.crashlytics

/**
 * Android implementation of CrashlyticsManagerFactory
 */
actual object CrashlyticsManagerFactory {
    actual fun create(): CrashlyticsManager {
        return AndroidCrashlyticsManager()
    }
}

