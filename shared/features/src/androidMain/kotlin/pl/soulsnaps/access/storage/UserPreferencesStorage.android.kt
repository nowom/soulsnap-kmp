package pl.soulsnaps.access.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementacja UserPreferencesStorage używająca SharedPreferences
 */
actual class UserPreferencesStorage {
    
    private val context: Context = UserPreferencesStorageFactory.getContext() as Context
    
    private val sharedPreferences: SharedPreferences by lazy {
        println("DEBUG: Creating SharedPreferences with context: ${context.packageName}")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    actual suspend fun saveUserPlan(planName: String) {
        println("DEBUG: Saving user plan: $planName")
        val success = sharedPreferences.edit()
            .putString(KEY_USER_PLAN, planName)
            .commit()
        println("DEBUG: Save user plan result: $success")
    }
    
    actual suspend fun getUserPlan(): String? {
        val plan = sharedPreferences.getString(KEY_USER_PLAN, null)
        println("DEBUG: Getting user plan: $plan")
        return plan
    }
    
    actual suspend fun saveOnboardingCompleted(completed: Boolean) {
        println("DEBUG: Saving onboarding completed: $completed")
        val success = sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .commit()
        println("DEBUG: Save onboarding result: $success")
    }
    
    actual suspend fun isOnboardingCompleted(): Boolean {
        val completed = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        println("DEBUG: Getting onboarding completed: $completed")
        return completed
    }
    
    actual suspend fun clearAllData() {
        sharedPreferences.edit()
            .remove(KEY_USER_PLAN)
            .remove(KEY_ONBOARDING_COMPLETED)
            .commit()
    }
    
    actual suspend fun hasStoredData(): Boolean {
        return sharedPreferences.contains(KEY_USER_PLAN) || 
               sharedPreferences.contains(KEY_ONBOARDING_COMPLETED)
    }
    
    companion object {
        private const val PREFS_NAME = "soulsnaps_user_preferences"
        private const val KEY_USER_PLAN = "user_plan"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
