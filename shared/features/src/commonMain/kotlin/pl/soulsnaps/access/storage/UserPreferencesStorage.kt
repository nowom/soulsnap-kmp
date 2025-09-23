package pl.soulsnaps.access.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesStorage(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val PLAN = stringPreferencesKey("plan_name")
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val NOTIF_DECIDED = booleanPreferencesKey("notification_permission_decided")
        val NOTIF_GRANTED = booleanPreferencesKey("notification_permission_granted")

    }

    suspend fun saveUserPlan(planName: String) {
        dataStore.edit { it[Keys.PLAN] = planName }
    }

    suspend fun getUserPlan(): String? =
        dataStore.data.map { it[Keys.PLAN] }.first()

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING] = completed }
    }

    suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.map { it[Keys.ONBOARDING] ?: false }.first()

    suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }

    suspend fun saveNotificationPermissionDecided(decided: Boolean) {
        dataStore.edit { it[Keys.NOTIF_DECIDED] = decided }
    }

    suspend fun isNotificationPermissionDecided(): Boolean =
        dataStore.data.map { it[Keys.NOTIF_DECIDED] ?: false }.first()

    suspend fun saveNotificationPermissionGranted(granted: Boolean) {
        dataStore.edit { it[Keys.NOTIF_GRANTED] = granted }
    }

    suspend fun isNotificationPermissionGranted(): Boolean =
        dataStore.data.map { it[Keys.NOTIF_GRANTED] ?: false }.first()

    suspend fun hasStoredData(): Boolean =
        dataStore.data.first().asMap().isNotEmpty()
}
