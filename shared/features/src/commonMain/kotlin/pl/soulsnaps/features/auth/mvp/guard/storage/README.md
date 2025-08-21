# UserPreferencesStorage - Dokumentacja użycia

## 📋 **Przegląd**

System `UserPreferencesStorage` zapewnia persistent storage dla planów użytkowników i statusu onboarding w aplikacji SoulSnaps. Działa na obu platformach (Android i iOS) z automatycznym zarządzaniem context.

## 🏗️ **Architektura**

### **Komponenty:**
- `UserPreferencesStorage` - expect/actual class dla persistent storage
- `UserPreferencesStorageFactory` - factory do tworzenia instancji z platform-specific context
- `UserPlanManager` - high-level manager używający storage

### **Platformy:**
- **Android**: Używa `SharedPreferences` z `Context`
- **iOS**: Używa `NSUserDefaults` (nie wymaga context)

## 🚀 **Inicjalizacja**

### **Android:**
W `Application.onCreate()`:

```kotlin
class SoulSnapsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicjalizuj UserPreferencesStorageFactory
        UserPreferencesStorageFactory.initialize(this)
    }
}
```

**Ważne:** Bez tej inicjalizacji aplikacja wyrzuci `IllegalStateException` przy próbie użycia storage.

### **iOS:**
Nie wymaga dodatkowej inicjalizacji - działa automatycznie.

## 💻 **Użycie w kodzie**

### **Podstawowe użycie:**
```kotlin
// UserPlanManager automatycznie używa factory
val userPlanManager = UserPlanManager()

// Ustaw plan użytkownika
userPlanManager.setUserPlan("FREE_USER")

// Sprawdź plan
val currentPlan = userPlanManager.getUserPlan()

// Sprawdź onboarding
val hasCompleted = userPlanManager.isOnboardingCompleted()
```

### **Bezpośrednie użycie storage:**
```kotlin
// Pobierz instancję przez factory
val storage = UserPreferencesStorageFactory.create()

// Zapisz plan
storage.saveUserPlan("PREMIUM_USER")

// Pobierz plan
val plan = storage.getUserPlan()

// Zapisz status onboarding
storage.saveOnboardingCompleted(true)

// Sprawdź status onboarding
val completed = storage.isOnboardingCompleted()
```

## 🔧 **API Reference**

### **UserPreferencesStorageFactory:**
```kotlin
expect object UserPreferencesStorageFactory {
    fun create(): UserPreferencesStorage
}
```

### **UserPreferencesStorage:**
```kotlin
expect class UserPreferencesStorage {
    suspend fun saveUserPlan(planName: String)
    suspend fun getUserPlan(): String?
    suspend fun saveOnboardingCompleted(completed: Boolean)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun clearAllData()
    suspend fun hasStoredData(): Boolean
}
```

### **UserPlanManager:**
```kotlin
class UserPlanManager(
    private val storage: UserPreferencesStorage = UserPreferencesStorageFactory.create(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    val currentPlan: Flow<String?>
    val hasCompletedOnboarding: Flow<Boolean>
    
    fun setUserPlan(planName: String)
    fun getUserPlan(): String?
    fun isOnboardingCompleted(): Boolean
    fun setDefaultPlanIfNeeded()
    fun resetUserPlan()
    fun hasStoredData(): Boolean
    fun refreshFromStorage()
}
```

## 🎯 **Przykłady użycia**

### **1. Pierwsze uruchomienie aplikacji:**
```kotlin
val userPlanManager = UserPlanManager()

// Sprawdź czy użytkownik ma zapisany plan
if (userPlanManager.hasStoredData()) {
    // Użytkownik już przeszedł onboarding
    val plan = userPlanManager.getUserPlan()
    showDashboard(plan)
} else {
    // Pierwsze uruchomienie - pokaż onboarding
    showOnboarding()
}
```

### **2. Po wyborze planu w onboarding:**
```kotlin
fun onPlanSelected(planName: String) {
    userPlanManager.setUserPlan(planName)
    // Plan zostanie automatycznie zapisany na dysk
    navigateToDashboard()
}
```

### **3. Wylogowanie użytkownika:**
```kotlin
fun logout() {
    userPlanManager.resetUserPlan()
    // Wszystkie dane zostaną wyczyszczone
    navigateToOnboarding()
}
```

### **4. Sprawdzanie planu w różnych częściach aplikacji:**
```kotlin
// W ViewModel
class SomeViewModel : ViewModel() {
    private val userPlanManager = UserPlanManager()
    
    fun someAction() {
        val plan = userPlanManager.getUserPlan()
        when (plan) {
            "GUEST" -> showLimitedFeatures()
            "FREE_USER" -> showStandardFeatures()
            "PREMIUM_USER" -> showAllFeatures()
        }
    }
}
```

## 🔄 **Flow aplikacji**

### **Pierwsze uruchomienie:**
```
1. Aplikacja startuje
2. UserPlanManager sprawdza hasStoredData()
3. Brak danych → pokaż onboarding
4. Użytkownik wybiera plan
5. setUserPlan() → zapisuje na dysk
6. Przejdź do dashboard
```

### **Kolejne uruchomienia:**
```
1. Aplikacja startuje
2. UserPlanManager sprawdza hasStoredData()
3. Dane istnieją → załaduj z dysku
4. Pokaż dashboard z zapamiętanym planem
```

## 🧪 **Testowanie**

### **Unit tests:**
```kotlin
@Test
fun `should save and retrieve user plan`() = runTest {
    val storage = UserPreferencesStorageFactory.create()
    
    storage.saveUserPlan("FREE_USER")
    val retrieved = storage.getUserPlan()
    
    assertEquals("FREE_USER", retrieved)
}
```

### **Integration tests:**
```kotlin
@Test
fun `should persist plan across app restarts`() = runTest {
    val manager1 = UserPlanManager()
    manager1.setUserPlan("PREMIUM_USER")
    
    val manager2 = UserPlanManager()
    val plan = manager2.getUserPlan()
    
    assertEquals("PREMIUM_USER", plan)
}
```

## ⚠️ **Ważne uwagi**

### **Android:**
- **Wymagana inicjalizacja** w `Application.onCreate()`
- **Context injection** przez `UserPreferencesStorageFactory.initialize()`
- **Application context** jest używany (nie Activity context)

### **iOS:**
- **Automatyczna inicjalizacja** - nie wymaga dodatkowych kroków
- **NSUserDefaults** jest używany automatycznie

### **Ogólne:**
- **Suspend functions** - wszystkie operacje I/O są suspend
- **Coroutine scope** - UserPlanManager używa własnego scope
- **Thread safety** - SharedPreferences/NSUserDefaults są thread-safe
- **Error handling** - błędy są rzucane jako exceptions

## 🔧 **Troubleshooting**

### **Problem: "UserPreferencesStorageFactory not initialized"**
**Rozwiązanie:** Dodaj inicjalizację w `Application.onCreate()`:
```kotlin
UserPreferencesStorageFactory.initialize(this)
```

**Lub** jeśli używasz w testach, możesz mockować factory.

### **Problem: Dane nie są zapisywane**
**Rozwiązanie:** Sprawdź czy używasz suspend functions:
```kotlin
// ❌ Błędne
storage.saveUserPlan("FREE_USER")

// ✅ Poprawne
storage.saveUserPlan("FREE_USER") // w coroutine scope
```

### **Problem: Plan nie jest zapamiętywany**
**Rozwiązanie:** Użyj `UserPlanManager` zamiast bezpośredniego storage:
```kotlin
// ❌ Błędne
storage.saveUserPlan("FREE_USER")

// ✅ Poprawne
userPlanManager.setUserPlan("FREE_USER")
```

## 📱 **Integracja z UI**

### **MainAppViewModel:**
```kotlin
class MainAppViewModel : ViewModel() {
    private val userPlanManager = UserPlanManager()
    
    init {
        checkAppState()
    }
    
    private fun checkAppState() {
        viewModelScope.launch {
            if (userPlanManager.hasStoredData()) {
                // Pokaż dashboard
                _uiState.value = _uiState.value.copy(
                    currentScreen = AppScreen.DASHBOARD,
                    userPlan = userPlanManager.getUserPlan()
                )
            } else {
                // Pokaż onboarding
                _uiState.value = _uiState.value.copy(
                    currentScreen = AppScreen.ONBOARDING
                )
            }
        }
    }
}
```

### **Onboarding:**
```kotlin
fun onPlanSelected(planName: String) {
    viewModelScope.launch {
        userPlanManager.setUserPlan(planName)
        // Automatycznie przejdzie do dashboard
    }
}
```

### **Dashboard:**
```kotlin
val userPlan by userPlanManager.currentPlan.collectAsState()
```

---

**System jest gotowy do użycia! Wszystkie komponenty są zintegrowane i przetestowane.**
