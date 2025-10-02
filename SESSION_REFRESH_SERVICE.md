# Session Refresh Service

## Przegląd

`SessionRefreshService` to automatyczny serwis który odświeża sesję Supabase co 5 minut, zapewniając że użytkownik nigdy nie zostanie wylogowany z powodu wygasłej sesji.

## Funkcjonalności

### 🔄 Automatyczne odświeżanie
- **Interwał**: Co 5 minut (300 sekund)
- **Warunek**: Tylko gdy użytkownik jest zalogowany
- **Monitorowanie**: Nasłuchuje zmian stanu sesji

### 🎯 Ręczne odświeżanie
- Przycisk w Settings do natychmiastowego odświeżenia
- Feedback wizualny (sukces/błąd)
- Dostępne tylko dla zalogowanych użytkowników

### 📊 Monitoring
- Szczegółowe logi wszystkich operacji
- Integracja z Crashlytics
- Status serwisu dostępny przez `getStatus()`

## Architektura

```kotlin
SessionRefreshService(
    userSessionManager: UserSessionManager,
    supabaseAuthService: SupabaseAuthService,
    sessionDataStore: SessionDataStore,
    crashlyticsManager: CrashlyticsManager
)
```

## Cykl życia

### 1. Uruchomienie
```kotlin
// W SoulSnapsApp.kt
LaunchedEffect(Unit) {
    sessionRefreshService.start()
}
```

### 2. Monitoring sesji
- Nasłuchuje `userSessionManager.sessionState`
- Gdy `SessionState.Authenticated` → uruchamia pętlę odświeżania
- Gdy `SessionState.Unauthenticated` → zatrzymuje pętlę

### 3. Pętla odświeżania
```kotlin
while (isRunning && userSessionManager.isAuthenticated()) {
    delay(REFRESH_INTERVAL_MS) // 5 minut
    
    val refreshedSession = supabaseAuthService.refreshSession()
    if (refreshedSession != null) {
        sessionDataStore.saveSession(refreshedSession)
        userSessionManager.onUserAuthenticated(refreshedSession)
    }
}
```

## Logi

### Uruchomienie serwisu
```
========================================
🔄 SessionRefreshService - STARTING AUTO-REFRESH SERVICE
========================================
⏰ Refresh interval: 5 minutes
========================================
```

### Automatyczne odświeżanie
```
========================================
🔄 SessionRefreshService - AUTO-REFRESHING SESSION
========================================
✅ SessionRefreshService - Session refreshed successfully
📊 userId: user123
📊 email: user@example.com
========================================
```

### Ręczne odświeżanie
```
========================================
🔄 SessionRefreshService - FORCE REFRESH NOW
========================================
✅ SessionRefreshService - Force refresh successful
========================================
```

## UI w Settings

### Karta zarządzania sesją
- **Tytuł**: "Zarządzanie sesją"
- **Opis**: "Sesja jest automatycznie odświeżana co 5 minut"
- **Przycisk**: "Odśwież sesję teraz"
- **Feedback**: 
  - "Odświeżanie sesji..." (podczas operacji)
  - "✅ Sesja odświeżona pomyślnie" (sukces)
  - "❌ Błąd odświeżania: [błąd]" (błąd)

## Konfiguracja

### Interwał odświeżania
```kotlin
companion object {
    private const val REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minut
}
```

### Warunki uruchomienia
- Użytkownik musi być zalogowany
- Serwis musi być uruchomiony (`isRunning = true`)
- Aplikacja musi być aktywna

## Obsługa błędów

### Błąd odświeżania
1. Loguje błąd do Crashlytics
2. Wywołuje `userSessionManager.validateAndRefreshSession()`
3. Kontynuuje pętlę po opóźnieniu

### Sesja wygasła
1. Zatrzymuje pętlę odświeżania
2. Loguje informację
3. Czeka na ponowne zalogowanie

## Integracja z Koin

```kotlin
// FeatureModule.kt
single {
    SessionRefreshService(
        userSessionManager = get(),
        supabaseAuthService = get(),
        sessionDataStore = get(),
        crashlyticsManager = get()
    )
}
```

## Korzyści

### 🚀 Dla użytkownika
- **Nigdy nie zostanie wylogowany** - sesja jest automatycznie odświeżana
- **Przezroczystość** - może ręcznie odświeżyć w Settings
- **Feedback** - widzi status operacji

### 🔧 Dla developera
- **Automatyzacja** - nie trzeba pamiętać o odświeżaniu
- **Monitoring** - szczegółowe logi i metryki
- **Elastyczność** - można zmienić interwał lub dodać warunki

## Testowanie

### Scenariusze testowe
1. **Automatyczne odświeżanie** - sprawdź logi co 5 minut
2. **Ręczne odświeżanie** - kliknij przycisk w Settings
3. **Wylogowanie** - sprawdź czy pętla się zatrzymuje
4. **Błąd sieci** - sprawdź obsługę błędów

### Logi do sprawdzenia
```bash
# Uruchomienie serwisu
grep "SessionRefreshService - STARTING" logs

# Automatyczne odświeżanie
grep "AUTO-REFRESHING SESSION" logs

# Ręczne odświeżanie
grep "FORCE REFRESH NOW" logs
```

## Przyszłe ulepszenia

### 🎯 Możliwe rozszerzenia
- **Adaptacyjny interwał** - krótszy gdy użytkownik aktywny
- **Background refresh** - odświeżanie w tle na iOS
- **Push notifications** - powiadomienia o problemach z sesją
- **Analytics** - metryki odświeżania sesji

### ⚙️ Konfiguracja
- **Interwał przez Settings** - użytkownik może zmienić
- **Wyłączenie** - opcja wyłączenia auto-refresh
- **Tryb debug** - więcej logów dla developera
