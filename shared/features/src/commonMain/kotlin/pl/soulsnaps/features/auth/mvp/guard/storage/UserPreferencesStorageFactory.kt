package pl.soulsnaps.features.auth.mvp.guard.storage

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
}

