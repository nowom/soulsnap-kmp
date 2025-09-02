package pl.soulsnaps.access.storage

import platform.Foundation.NSUserDefaults

/**
 * iOS implementacja UserPreferencesStorage używająca UserDefaults
 */
actual class UserPreferencesStorage {
    
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    
    actual suspend fun saveUserPlan(planName: String) {
        userDefaults.setObject(planName, KEY_USER_PLAN)
        userDefaults.synchronize()
    }
    
    actual suspend fun getUserPlan(): String? {
        return userDefaults.stringForKey(KEY_USER_PLAN)
    }
    
    actual suspend fun saveOnboardingCompleted(completed: Boolean) {
        userDefaults.setBool(completed, KEY_ONBOARDING_COMPLETED)
        userDefaults.synchronize()
    }
    
    actual suspend fun isOnboardingCompleted(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING_COMPLETED)
    }
    
    actual suspend fun clearAllData() {
        userDefaults.removeObjectForKey(KEY_USER_PLAN)
        userDefaults.removeObjectForKey(KEY_ONBOARDING_COMPLETED)
        userDefaults.synchronize()
    }
    
    actual suspend fun hasStoredData(): Boolean {
        return userDefaults.objectForKey(KEY_USER_PLAN) != null || 
               userDefaults.objectForKey(KEY_ONBOARDING_COMPLETED) != null
    }
    
    companion object {
        private const val KEY_USER_PLAN = "user_plan"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}



