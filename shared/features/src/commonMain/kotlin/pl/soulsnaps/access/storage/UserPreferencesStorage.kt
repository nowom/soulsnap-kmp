package pl.soulsnaps.access.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesStorageImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesStorage {
    private object Keys {
        val PLAN = stringPreferencesKey("plan_name")
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val NOTIF_DECIDED = booleanPreferencesKey("notification_permission_decided")
        val NOTIF_GRANTED = booleanPreferencesKey("notification_permission_granted")

    }

    override suspend fun saveUserPlan(planName: String) {
        dataStore.edit { it[Keys.PLAN] = planName }
    }

    override suspend fun getUserPlan(): String? =
        dataStore.data.map { it[Keys.PLAN] }.first()

    override suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING] = completed }
    }

    override suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.map { it[Keys.ONBOARDING] ?: false }.first()

    override suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }

    override suspend fun saveNotificationPermissionDecided(decided: Boolean) {
        dataStore.edit { it[Keys.NOTIF_DECIDED] = decided }
    }

    override suspend fun isNotificationPermissionDecided(): Boolean =
        dataStore.data.map { it[Keys.NOTIF_DECIDED] ?: false }.first()

    override suspend fun saveNotificationPermissionGranted(granted: Boolean) {
        dataStore.edit { it[Keys.NOTIF_GRANTED] = granted }
    }

    override suspend fun isNotificationPermissionGranted(): Boolean =
        dataStore.data.map { it[Keys.NOTIF_GRANTED] ?: false }.first()

    override suspend fun hasStoredData(): Boolean =
        dataStore.data.first().asMap().isNotEmpty()
    
    // Generic string/boolean operations
    override suspend fun saveString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }
    
    override suspend fun getString(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { it[prefKey] }.first()
    }
    
    override suspend fun removeString(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { it.remove(prefKey) }
    }
    
    override suspend fun saveBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }
    
    override suspend fun getBoolean(key: String): Boolean? {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { it[prefKey] }.first()
    }
    
    override suspend fun removeBoolean(key: String) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { it.remove(prefKey) }
    }
    
    override suspend fun saveLong(key: String, value: Long) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }
    
    override suspend fun getLong(key: String): Long? {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { it[prefKey] }.first()
    }
    
    override suspend fun removeLong(key: String) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit { it.remove(prefKey) }
    }
}

interface UserPreferencesStorage{
    suspend fun saveUserPlan(planName: String)
    suspend fun getUserPlan(): String?
    suspend fun saveOnboardingCompleted(completed: Boolean)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun clearAllData()
    suspend fun saveNotificationPermissionDecided(decided: Boolean)
    suspend fun isNotificationPermissionDecided(): Boolean
    suspend fun saveNotificationPermissionGranted(granted: Boolean)
    suspend fun isNotificationPermissionGranted(): Boolean
    suspend fun hasStoredData(): Boolean
    
    // Generic string/boolean/long operations for session storage
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun removeString(key: String)
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun removeBoolean(key: String)
    suspend fun saveLong(key: String, value: Long)
    suspend fun getLong(key: String): Long?
    suspend fun removeLong(key: String)
}
