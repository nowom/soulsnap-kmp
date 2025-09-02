package pl.soulsnaps.access.storage

/**
 * Expect declaration dla UserPreferencesStorage
 * 
 * Każda platforma musi dostarczyć swoją implementację:
 * - Android: SharedPreferences z Context
 * - iOS: UserDefaults (bez Context)
 */
expect class UserPreferencesStorage {
    
    /**
     * Konstruktor - platform-specific
     */
    constructor()
    

    
    /**
     * Zapisz plan użytkownika
     */
    suspend fun saveUserPlan(planName: String)
    
    /**
     * Pobierz plan użytkownika
     */
    suspend fun getUserPlan(): String?
    
    /**
     * Zapisz stan ukończenia onboarding
     */
    suspend fun saveOnboardingCompleted(completed: Boolean)
    
    /**
     * Sprawdź czy onboarding został ukończony
     */
    suspend fun isOnboardingCompleted(): Boolean
    
    /**
     * Wyczyść wszystkie dane (przy wylogowaniu)
     */
    suspend fun clearAllData()
    
    /**
     * Sprawdź czy dane istnieją
     */
    suspend fun hasStoredData(): Boolean
}
