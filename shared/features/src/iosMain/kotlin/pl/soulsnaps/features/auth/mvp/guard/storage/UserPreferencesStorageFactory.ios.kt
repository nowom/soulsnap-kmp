package pl.soulsnaps.features.auth.mvp.guard.storage

/**
 * iOS implementacja UserPreferencesStorageFactory
 * 
 * Na iOS nie potrzebujemy context, więc tworzymy instancję bez parametrów
 */
actual object UserPreferencesStorageFactory {
    
    /**
     * Tworzy instancję UserPreferencesStorage
     */
    actual fun create(): UserPreferencesStorage {
        return UserPreferencesStorage()
    }
}
