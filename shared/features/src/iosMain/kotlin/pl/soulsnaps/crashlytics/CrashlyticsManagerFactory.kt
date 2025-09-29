package pl.soulsnaps.crashlytics

/**
 * iOS implementation of CrashlyticsManagerFactory
 */
actual object CrashlyticsManagerFactory {
    actual fun create(): CrashlyticsManager {
        return IosCrashlyticsManager()
    }
}

