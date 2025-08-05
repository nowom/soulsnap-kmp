package pl.soulsnaps.features.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

class AndroidOnboardingDataStore(
    private val context: Context
) : OnboardingDataStore {
    
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val ONBOARDING_COMPLETED_AT = longPreferencesKey("onboarding_completed_at")
        private val USER_FOCUS = stringPreferencesKey("user_focus")
        private val AUTH_TYPE = stringPreferencesKey("auth_type")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val TOUR_COMPLETED = booleanPreferencesKey("tour_completed")
    }
    
    override val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun clearOnboardingData() {
        context.dataStore.edit { preferences ->
            preferences.remove(ONBOARDING_COMPLETED)
            preferences.remove(ONBOARDING_COMPLETED_AT)
            preferences.remove(USER_FOCUS)
            preferences.remove(AUTH_TYPE)
            preferences.remove(USER_EMAIL)
            preferences.remove(TOUR_COMPLETED)
        }
    }
    
    override suspend fun markOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
            preferences[ONBOARDING_COMPLETED_AT] = System.currentTimeMillis()
        }
    }
} 