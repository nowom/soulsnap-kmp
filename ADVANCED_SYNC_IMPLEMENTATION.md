# SoulSnaps Advanced Sync Implementation

## 🎯 **Cel:**
Spójna, idempotentna synchronizacja wpisów memories między lokalną bazą (SQLDelight) a Supabase (Postgres + Storage).

## 📋 **Paradygmat:**
- **Single source of truth** lokalnie
- **Synchronizacja w tle** (background)
- **UI działa natychmiast** (optimistic)
- **Idempotencja** i odporność na brak sieci
- **Prosty model konfliktów** (LWW - Last Write Wins)
- **Bezpieczeństwo** (RLS, signed URLs)
- **Obserwowalność** (logi, metryki)

## 🏗️ **Architektura komponentów:**

### **1. SyncQueue (SQLDelight)**
```sql
CREATE TABLE sync_queue (
    id TEXT NOT NULL PRIMARY KEY,
    type TEXT NOT NULL,
    payload TEXT NOT NULL, -- JSON
    state TEXT NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_run_at INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    local_id INTEGER NOT NULL
);
```

**Funkcjonalności:**
- `enqueue(task)` - idempotentne dodawanie zadań
- `getDueTasks(limit)` - pobieranie zadań do wykonania
- `markRunning/markCompleted/markFailed` - aktualizacja stanu
- `cancelTasksForLocalId` - anulowanie zadań dla entity
- Exponential backoff + jitter

### **2. SyncProcessor (Logika zadań)**
```kotlin
interface SyncProcessor {
    suspend fun run(task: SyncTask): Result<Unit>
}
```

**Obsługiwane zadania:**
- `CreateMemory` - tworzenie + upload plików
- `UpdateMemory` - aktualizacja + re-upload plików
- `ToggleFavorite` - zmiana statusu ulubionego
- `DeleteMemory` - usuwanie + cleanup plików
- `PullAll` - pobieranie wszystkich danych

### **3. AdvancedSyncManager (Pętla przetwarzania)**
```kotlin
interface SyncManager {
    fun start()
    fun stop()
    suspend fun triggerNow()
    suspend fun enqueue(task: SyncTask)
    val status: StateFlow<SyncStatus>
}
```

**Funkcjonalności:**
- Pętla przetwarzania zadań
- Per-entity mutex (unikanie wyścigów)
- Równoległe wykonanie (2-4 zadania)
- Backoff z jitterem
- Obserwowalność (status, eventy)

### **4. StorageClient (Supabase Storage)**
```kotlin
interface StorageClient {
    suspend fun upload(bucket: String, key: String, filePath: String, upsert: Boolean): StorageResult
    suspend fun delete(bucket: String, key: String): StorageResult
    suspend fun getSignedUrl(bucket: String, key: String): String?
}
```

**Ścieżki Storage (deterministyczne):**
- `soulsnaps/{userId}/{id}/photo.jpg`
- `soulsnaps/{userId}/{id}/audio.m4a`

### **5. ConnectivityMonitor**
```kotlin
interface ConnectivityMonitor {
    val connected: StateFlow<Boolean>
    fun start()
    fun stop()
}
```

### **6. EventBus (Domain Events)**
```kotlin
sealed class AppEvent {
    data class SnapSynced(val localId: Long) : AppEvent()
    data class SnapSyncFailed(val localId: Long, val error: String) : AppEvent()
    data class SnapDeleted(val localId: Long) : AppEvent()
    data class SyncStarted(val taskCount: Int) : AppEvent()
    data class SyncCompleted(val successCount: Int, val failureCount: Int) : AppEvent()
}
```

## 🔄 **Przepływy E2E:**

### **A) Dodanie nowego Snap'a**
```
1. Repository.addMemory(new) 
   → MemoryDao.insert(..., sync_state=PENDING, isSynced=false)

2. SyncManager.enqueue(CreateMemory(localId, plannedRemotePhotoPath))

3. (Online) SyncProcessor:
   - markSyncing(localId)
   - Storage.upload(upsert) photo + audio
   - OnlineDataSource.insertMemory(upsert) (DB)
   - MemoryDao.updateAfterSync(..., remote*, sync_state=SYNCED)
   - EventBus.emit(SnapSynced(localId))
```

### **B) Aktualizacja Snap'a**
```
1. Lokalny update → enqueue UpdateMemory(localId, reuploadPhoto?, reuploadAudio?)

2. Processor: (ew. reupload plików) → OnlineDataSource.updateMemory → markSynced
```

### **C) Oznaczenie ulubionego**
```
1. Lokalna zmiana natychmiastowa (optimistic)
2. Enqueue ToggleFavorite(localId, isFavorite) → best-effort na zdalnym
```

### **D) Usunięcie**
```
1. Jeśli isSynced=false: usuń lokalnie, anuluj zadania
2. Jeśli isSynced=true: deleted=true + enqueue DeleteMemory(localId, remote*)
3. Processor: Storage.delete(...) → OnlineDataSource.deleteMemory → MemoryDao.deleteById
```

### **E) PullAll (po logowaniu/starcie)**
```
1. OnlineDataSource.getAllMemories(userId) 
2. Merge upsert lokalnie (preferuj nowszy updated_at)
3. Nie nadpisuj rekordów w trakcie lokalnej edycji (PENDING/FAILED)
```

## 🔒 **Zasady idempotencji i porządku:**

### **Tożsamość obiektu:**
- `localId` (Long) = `local_id` (remote)
- Ścieżki Storage deterministyczne
- DB upsert: `on conflict (user_id, local_id) do update`

### **Kolejka:**
- `insertIfAbsent(task.id)` (nie duplikuj zadań)
- Per-entity mutex: nigdy nie wykonuj dwóch tasków na tym samym localId równocześnie

### **Strategie konfliktów:**
- **LWW (Last-Write-Wins)** po `updated_at` (ms)
- Merge podczas PullAll nie nadpisuje lokalnych PENDING/FAILED

## ⚡ **Retry i wydajność:**

### **Backoff:**
- Wykładniczy + jitter: 15s → 30s → 1m → 2m → ... max 1h
- Równoległość: 2-4 taski równolegle, ale jeden localId naraz

### **Budzenie:**
- `PlatformScheduler.ensureScheduled()` dla okresowych prób
- `triggerNow()` po enqueue lub gdy wraca sieć

## 🔐 **Bezpieczeństwo i prywatność:**

### **Supabase RLS:**
- Polityki `select/insert/update/delete` z `auth.uid() = user_id`
- Storage: przechowuj w DB tylko klucze (`remotePhotoPath`)
- URL-e generuj podpisane na żądanie

### **Dane wrażliwe:**
- Kompresuj i przetwarzaj lokalnie
- Nie wysyłaj nieużywanych pól

## 📊 **Obserwowalność:**

### **Metryki:**
- `sync_queue_pending`, `sync_task_duration_ms` per typ
- `sync_failures_total`, `backoff_level`, `upload_bytes_total`

### **Logi zdarzeń:**
- `enqueue`, `start/stop tasku`, `próba/wyjątek`, `reschedule`, `done`

### **Zdarzenia domenowe:**
- `SnapSynced(localId)` (do UI/afirmacji/analytics)

## ⚙️ **Konfiguracja (feature flags):**
```kotlin
data class SyncConfig(
    val maxParallelTasks: Int = 3,
    val backoffBaseMs: Long = 15000,
    val backoffMaxMs: Long = 3600000,
    val uploadCompression: Boolean = true,
    val pullOnStartup: Boolean = true,
    val retryOnMetered: Boolean = false
)
```

## 🚀 **Implementacja krok po kroku:**

### **✅ Zaimplementowane:**
1. **Modele danych** - `SyncTask`, `SyncStatus`, `SyncConfig`
2. **SyncQueue** - SQLDelight z backoff logic
3. **StorageClient** - Supabase Storage interface
4. **ConnectivityMonitor** - network status monitoring
5. **EventBus** - domain events
6. **SyncProcessor** - task execution logic
7. **AdvancedSyncManager** - queue processing loop
8. **PlatformScheduler** - WorkManager integration
9. **AdvancedSyncWorker** - background worker

### **🔄 Następne kroki:**
1. **Dependency Injection** - dodać wszystkie komponenty do DI
2. **iOS Implementation** - Background Tasks + Network framework
3. **Testing** - unit testy dla wszystkich komponentów
4. **Error Handling** - lepsze obsługiwanie błędów
5. **Metrics** - dodanie metryk i analytics
6. **Configuration** - feature flags i konfiguracja

## 📱 **Platform-specific:**

### **Android:**
- `WorkManager` + `AdvancedSyncWorker`
- `AndroidPlatformScheduler`
- `ConnectivityManager` dla network monitoring

### **iOS:**
- `Background Tasks` + `BGAppRefreshTask`
- `Network framework` + `NWPathMonitor`
- `URLSession` dla networking

## 🎯 **Korzyści tego podejścia:**

### **✅ Idempotencja:**
- Każde zadanie ma unikalny ID
- Upsert operations w bazie danych
- Deterministic storage paths

### **✅ Odporność na brak sieci:**
- Offline-first approach
- Queue z retry logic
- Graceful degradation

### **✅ Obserwowalność:**
- Comprehensive logging
- Domain events
- Metrics i analytics

### **✅ Bezpieczeństwo:**
- RLS policies
- Signed URLs
- Data privacy

### **✅ Wydajność:**
- Parallel processing
- Smart backoff
- Efficient storage

System jest **gotowy do integracji** i zapewnia enterprise-grade synchronizację! 🚀
