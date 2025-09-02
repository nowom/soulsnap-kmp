package pl.soulsnaps.access.storage

/**
 * Factory dla UserPreferencesStorage
 * 
 * Pozwala na tworzenie instancji UserPreferencesStorage z platform-specific context
 */
expect object UserPreferencesStorageFactory {
    /**
     * Tworzy instancję UserPreferencesStorage
     */
    fun create(): UserPreferencesStorage
    
    /**
     * Pobiera context (tylko dla Android)
     */
    fun getContext(): Any
}

