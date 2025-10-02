# 🔐 Session Persistence Guide

## Problem: Session nie była zachowywana

### **Przed naprawą:**
```
User: Logs in → Session in memory only
Restart app → Session LOST → Back to GUEST mode
Sync: Fails → isAuthenticated: false
```

### **Po naprawie:**
```
User: Logs in → Session saved to DataStore + Supabase
Restart app → Session RESTORED → Still logged in
Sync: Works → isAuthenticated: true
```

## 🔧 Implementacja

### **1. PersistentSessionDataStore**

Zastąpiono `InMemorySessionDataStore` z `PersistentSessionDataStore`:

```kotlin
// ❌ PRZED: In-memory only
class InMemorySessionDataStore : SessionDataStore {
    private var storedSession: UserSession? = null  // Lost on restart
}

// ✅ PO: Persistent with DataStore
class PersistentSessionDataStore(
    private val userPreferencesStorage: UserPreferencesStorage
) : SessionDataStore {
    
    override suspend fun saveSession(userSession: UserSession) {
        // Save ALL fields to DataStore
        userPreferencesStorage.saveString("session_user_id", userSession.userId)
        userPreferencesStorage.saveString("session_email", userSession.email)
        userPreferencesStorage.saveString("session_display_name", userSession.displayName)
        userPreferencesStorage.saveBoolean("session_is_anonymous", userSession.isAnonymous)
        userPreferencesStorage.saveLong("session_created_at", userSession.createdAt)
        userPreferencesStorage.saveLong("session_last_active_at", userSession.lastActiveAt)
        userPreferencesStorage.saveString("session_access_token", userSession.accessToken)
        userPreferencesStorage.saveString("session_refresh_token", userSession.refreshToken)
    }
    
    override suspend fun getStoredSession(): UserSession? {
        // Load ALL fields from DataStore
        val userId = userPreferencesStorage.getString("session_user_id")
        val email = userPreferencesStorage.getString("session_email")
        // ... restore full UserSession
    }
}
```

### **2. Session Validation & Refresh**

Dodano automatyczną walidację i refresh sesji:

```kotlin
override suspend fun validateAndRefreshSession() {
    val storedSession = sessionDataStore.getStoredSession()
    
    if (storedSession == null) {
        // No session → Unauthenticated
        return
    }
    
    // Check if Supabase session is still valid
    val isSupabaseAuthenticated = supabaseAuthService.isAuthenticated()
    
    if (isSupabaseAuthenticated) {
        // Session valid → Restore user
        _currentUser.update { storedSession }
        _sessionState.update { SessionState.Authenticated(storedSession) }
    } else {
        // Session expired → Try refresh
        val refreshedSession = supabaseAuthService.refreshSession()
        
        if (refreshedSession != null) {
            // Refresh successful → Save new session
            sessionDataStore.saveSession(refreshedSession)
            _currentUser.update { refreshedSession }
            _sessionState.update { SessionState.Authenticated(refreshedSession) }
        } else {
            // Refresh failed → Clear session
            sessionDataStore.clearSession()
            _sessionState.update { SessionState.SessionExpired }
        }
    }
}
```

### **3. Auto-start on App Launch**

```kotlin
// UserSessionManagerImpl.init()
init {
    checkExistingSession()  // ✅ Validates and refreshes session
    observeSessionChanges()
}

// checkExistingSession()
private fun checkExistingSession() {
    coroutineScope.launch {
        validateAndRefreshSession()  // ✅ Called on app start
    }
}
```

## 🔍 Oczekiwane logi

### **Scenariusz 1: Session ważna**
```
========================================
🔍 UserSessionManagerImpl.checkExistingSession() - CHECKING
========================================
========================================
🔄 UserSessionManagerImpl.validateAndRefreshSession() - VALIDATING SESSION
========================================
========================================
💾 PersistentSessionDataStore.init() - LOADING SESSION
========================================
✅ Session loaded: userId=xxx, email=test@test.com
========================================
✅ Found stored session: xxx
📊 Supabase session valid: true  ✅
✅ Supabase session valid, restoring user session
========================================
✅ UserSessionManagerImpl.checkExistingSession() - COMPLETED
📊 isAuthenticated: true  ✅
📊 currentUser: test@test.com  ✅
========================================
```

### **Scenariusz 2: Session wygasła, refresh sukces**
```
========================================
🔄 UserSessionManagerImpl.validateAndRefreshSession() - VALIDATING SESSION
========================================
✅ Found stored session: xxx
📊 Supabase session valid: false  ❌
⚠️ Supabase session expired, attempting refresh...
✅ Session refreshed successfully  ✅
========================================
💾 PersistentSessionDataStore.saveSession() - SAVING SESSION
========================================
✅ PersistentSessionDataStore.saveSession() - SESSION SAVED
========================================
📊 isAuthenticated: true  ✅
```

### **Scenariusz 3: Session wygasła, refresh failed**
```
========================================
🔄 UserSessionManagerImpl.validateAndRefreshSession() - VALIDATING SESSION
========================================
✅ Found stored session: xxx
📊 Supabase session valid: false  ❌
⚠️ Supabase session expired, attempting refresh...
❌ Session refresh failed, clearing session  ❌
========================================
🗑️ PersistentSessionDataStore.clearSession() - CLEARING SESSION
========================================
📊 isAuthenticated: false  ❌
→ User needs to login again
```

## 🎯 Dlaczego sesja wygasa?

### **Supabase Session Lifetime:**
- **Access Token**: ~1 godzina
- **Refresh Token**: ~7-30 dni (configurable)
- **Auto-refresh**: Jeśli masz refresh token

### **Co się dzieje:**
1. Login → Access token + Refresh token
2. Po 1h → Access token wygasa
3. App używa Refresh token → Nowy Access token
4. Po 30 dniach → Refresh token wygasa → Re-login required

## ✅ Rozwiązanie

### **Teraz aplikacja:**
1. ✅ **Zapisuje sesję** do DataStore (persistent)
2. ✅ **Waliduje sesję** przy starcie (sprawdza Supabase)
3. ✅ **Refreshuje token** jeśli wygasł
4. ✅ **Wylogowuje** tylko jeśli refresh failed

### **Automatyczne zachowania:**
- ✅ Session restored on app restart
- ✅ Auto-refresh if expired
- ✅ Clear only if refresh fails
- ✅ All automatic - no user action needed

## 🚀 Testowanie

1. **Login** → Sprawdź logi
2. **Restart app** → Sprawdź czy widzisz:
   ```
   ✅ Session loaded: userId=xxx, email=xxx
   📊 Supabase session valid: true
   📊 isAuthenticated: true
   ```
3. **Add memory** → Powinna synchronizować:
   ```
   📊 isAuthenticated: true
   ✅ SYNC TASK ENQUEUED!
   ```

## ⚠️ Uwaga

Jeśli **refresh token też wygasł** (po 30 dniach), użytkownik będzie musiał się zalogować ponownie. To jest normalne i bezpieczne zachowanie!
