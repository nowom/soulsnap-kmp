package pl.soulsnaps.access.storage

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
    
    /**
     * Pobiera context (tylko dla Android)
     */
    actual fun getContext(): Any {
        throw UnsupportedOperationException("getContext() is not supported on iOS")
    }
}



