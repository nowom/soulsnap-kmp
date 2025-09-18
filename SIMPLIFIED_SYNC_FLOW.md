# SoulSnaps Simplified Sync Flow

## Nowy uproszczony przepływ synchronizacji (bez draftów)

### 🔄 **Przepływ:**

```
UI (FAB "+") → zbiera opis, emocję, plik(i)
    ↓
Repository.addMemory(newSnap)
    ↓
1. Generuje id (Long)
2. Wylicza remote_photo_path = "soulsnaps/{userId}/{id}/photo.jpg"
3. memoryDao.insert(...) z:
   - local_photo_uri = ścieżka pliku
   - remote_photo_path = null (będzie ustawiony po upload)
   - sync_state = PENDING
    ↓
4. enqueueSync(id)
    ↓
SyncEngine (gdy online):
    ↓
5. mark SYNCING
6. Storage.upload z local_photo_uri → do remote_photo_path
7. Supabase DB upsert (memories, konflikt po user_id, local_id)
8. memoryDao.update(id, remote_photo_path, sync_state=SYNCED)
    ↓
Afirmacja:
- Albo od razu lokalnie (UX → natychmiastowy efekt)
- Albo po SYNCED emitujesz event i generujesz
```

## 📊 **Struktura danych**

### **Memory Model (zaktualizowany)**
```kotlin
data class Memory(
    val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val mood: MoodType?,
    val photoUri: String?, // Local file path
    val audioUri: String?, // Local file path
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val affirmation: String? = null,
    val isFavorite: Boolean = false,
    val isSynced: Boolean = false,
    // New sync fields
    val remotePhotoPath: String? = null, // Supabase Storage path
    val remoteAudioPath: String? = null, // Supabase Storage path
    val remoteId: String? = null, // Remote database ID
    val syncState: SyncState = SyncState.PENDING,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)

enum class SyncState {
    PENDING,    // Waiting to be synced
    SYNCING,    // Currently being synced
    SYNCED,     // Successfully synced
    FAILED      // Sync failed, will retry
}
```

### **Database Schema (zaktualizowany)**
```sql
CREATE TABLE memories (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    mood TEXT,
    photoUri TEXT, -- Local file path
    audioUri TEXT, -- Local file path
    locationName TEXT,
    latitude REAL,
    longitude REAL,
    affirmation TEXT,
    isFavorite INTEGER AS Boolean NOT NULL,
    isSynced INTEGER AS Boolean NOT NULL DEFAULT 0,
    -- New sync fields
    remotePhotoPath TEXT, -- Supabase Storage path
    remoteAudioPath TEXT, -- Supabase Storage path
    remoteId TEXT, -- Remote database ID
    syncState TEXT NOT NULL DEFAULT 'PENDING',
    retryCount INTEGER NOT NULL DEFAULT 0,
    errorMessage TEXT
);
```

## 🏗️ **Komponenty**

### **1. SimplifiedMemoryRepository**
```kotlin
class SimplifiedMemoryRepository(
    private val database: SoulSnapDatabase,
    private val syncEngine: SyncEngine,
    private val userId: String
) {
    
    suspend fun addMemory(
        title: String,
        description: String,
        mood: MoodType?,
        photoUri: String?,
        audioUri: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?,
        affirmation: String? = null
    ): Memory {
        // 1. Generate ID and remote paths
        val memoryId = generateMemoryId()
        val remotePhotoPath = photoUri?.let { generateRemotePhotoPath(memoryId) }
        val remoteAudioPath = audioUri?.let { generateRemoteAudioPath(memoryId) }
        
        // 2. Create memory with PENDING sync state
        val memory = Memory(
            id = memoryId,
            // ... other fields
            photoUri = photoUri, // Local path
            remotePhotoPath = remotePhotoPath, // Will be uploaded
            syncState = SyncState.PENDING
        )
        
        // 3. Save to local database
        database.memoryQueries.insertMemory(...)
        
        // 4. Enqueue sync
        syncEngine.enqueueSync(memoryId.toString())
        
        return memory
    }
}
```

### **2. SimplifiedSyncEngine**
```kotlin
class SimplifiedSyncEngine(
    private val memoryRepository: SimplifiedMemoryRepository,
    private val storageService: StorageService,
    private val remoteApiService: RemoteApiService,
    private val networkMonitor: NetworkMonitor
) : SyncEngine {
    
    private suspend fun syncMemory(memory: Memory): SyncResult {
        // 1. Mark as syncing
        memoryRepository.updateMemorySyncState(
            id = memory.id,
            syncState = SyncState.SYNCING
        )
        
        // 2. Upload files to storage
        val remotePhotoPath = memory.photoUri?.let { 
            storageService.uploadPhoto(memory.id.toString(), it).path 
        }
        val remoteAudioPath = memory.audioUri?.let { 
            storageService.uploadAudio(memory.id.toString(), it).path 
        }
        
        // 3. Create remote snap
        val remoteSnap = RemoteSnap(
            title = memory.title,
            description = memory.description,
            mood_type = memory.mood?.name,
            photo_uri = remotePhotoPath,
            audio_uri = remoteAudioPath,
            // ... other fields
        )
        
        // 4. Send to server
        val createResult = remoteApiService.createSnap(remoteSnap)
        
        // 5. Update local memory as synced
        memoryRepository.updateMemoryRemotePaths(
            id = memory.id,
            remotePhotoPath = remotePhotoPath,
            remoteAudioPath = remoteAudioPath,
            syncState = SyncState.SYNCED,
            remoteId = createResult.remoteId
        )
        
        return SyncResult(success = true, remoteId = createResult.remoteId)
    }
}
```

### **3. AddMemoryUseCase**
```kotlin
class AddMemoryUseCase(
    private val memoryRepository: SimplifiedMemoryRepository,
    private val affirmationService: AffirmationService
) {
    
    suspend operator fun invoke(
        title: String,
        description: String,
        mood: MoodType?,
        photoUri: String?,
        audioUri: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?
    ): AddMemoryResult {
        // 1. Add memory to local database
        val memory = memoryRepository.addMemory(...)
        
        // 2. Generate affirmation immediately (UX effect)
        val affirmation = generateAffirmationIfNeeded(description, mood)
        
        return AddMemoryResult(
            success = true,
            memory = memory,
            affirmation = affirmation
        )
    }
}
```

## 🎯 **Kluczowe zalety nowego przepływu:**

### **1. Uproszczenie**
- ❌ Usunięto warstwę draftów
- ✅ Bezpośrednia praca z Memory
- ✅ Mniej kodu do utrzymania

### **2. Lepsze UX**
- ✅ Natychmiastowe zapisanie lokalnie
- ✅ Afirmacja generowana od razu
- ✅ Synchronizacja w tle

### **3. Wydajność**
- ✅ Mniej operacji na bazie danych
- ✅ Prostsze zapytania SQL
- ✅ Mniej pamięci RAM

### **4. Debugowanie**
- ✅ Łatwiejsze śledzenie stanu
- ✅ Mniej warstw abstrakcji
- ✅ Prostsze logi

## 📱 **Przepływ w UI:**

### **CaptureMomentViewModel**
```kotlin
private fun saveMemory() {
    viewModelScope.launch {
        // 1. Check capacity limits
        val capacityResult = checkCapacityBeforeSave()
        if (!capacityResult.allowed) {
            showPaywall()
            return@launch
        }
        
        // 2. Add memory with immediate local save
        val result = addMemoryUseCase(
            title = state.value.title,
            description = state.value.description,
            mood = state.value.selectedMood,
            photoUri = state.value.photoUri,
            audioUri = state.value.audioUri,
            locationName = state.value.location,
            latitude = null,
            longitude = null
        )
        
        if (result.success) {
            // 3. Show success message
            _state.update { 
                it.copy(
                    successMessage = "Pamięć została zapisana lokalnie i będzie zsynchronizowana w tle!",
                    savedMemoryId = result.memory?.id
                ) 
            }
            
            // 4. Show affirmation if generated
            if (result.affirmation != null) {
                _state.update { 
                    it.copy(
                        generatedAffirmation = result.affirmation,
                        showAffirmationSnackbar = true
                    ) 
                }
            }
        }
    }
}
```

## 🔄 **Synchronizacja:**

### **WorkManager (Android)**
```kotlin
class SyncWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        val result = syncEngine.syncPendingDrafts()
        return if (result.success) Result.success() else Result.retry()
    }
}
```

### **Background Tasks (iOS)**
```swift
class BackgroundTaskHandler {
    private func handleBackgroundTask(task: BGAppRefreshTask) {
        syncManager.handleBackgroundTask { success in
            task.setTaskCompleted(success: success)
        }
    }
}
```

## 📊 **Monitoring:**

### **Sync Events**
```kotlin
sealed class SyncEvent {
    data class SnapCreated(val remoteId: String) : SyncEvent()
    data class SnapUpdated(val remoteId: String) : SyncEvent()
    data class SnapDeleted(val remoteId: String) : SyncEvent()
    data class SyncFailed(val memoryId: String, val error: String) : SyncEvent()
    data class SyncProgress(val memoryId: String, val progress: Int) : SyncEvent()
}
```

### **Analytics**
```kotlin
// Track sync events
analytics.track("memory_created", mapOf("memory_id" to memory.id))
analytics.track("sync_started", mapOf("memory_id" to memory.id))
analytics.track("sync_completed", mapOf("memory_id" to memory.id, "remote_id" to remoteId))
analytics.track("sync_failed", mapOf("memory_id" to memory.id, "error" to error))
```

## 🚀 **Deployment:**

### **Database Migration**
```sql
-- Add new columns to existing memories table
ALTER TABLE memories ADD COLUMN remotePhotoPath TEXT;
ALTER TABLE memories ADD COLUMN remoteAudioPath TEXT;
ALTER TABLE memories ADD COLUMN remoteId TEXT;
ALTER TABLE memories ADD COLUMN syncState TEXT NOT NULL DEFAULT 'PENDING';
ALTER TABLE memories ADD COLUMN retryCount INTEGER NOT NULL DEFAULT 0;
ALTER TABLE memories ADD COLUMN errorMessage TEXT;
```

### **Supabase Schema Update**
```sql
-- Update memories table to match new structure
ALTER TABLE memories ADD COLUMN IF NOT EXISTS remote_photo_path TEXT;
ALTER TABLE memories ADD COLUMN IF NOT EXISTS remote_audio_path TEXT;
ALTER TABLE memories ADD COLUMN IF NOT EXISTS remote_id TEXT;
ALTER TABLE memories ADD COLUMN IF NOT EXISTS sync_state TEXT NOT NULL DEFAULT 'PENDING';
ALTER TABLE memories ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE memories ADD COLUMN IF NOT EXISTS error_message TEXT;
```

## ✅ **Podsumowanie:**

Nowy uproszczony przepływ eliminuje warstwę draftów i zapewnia:

1. **Szybsze działanie** - mniej operacji na bazie danych
2. **Lepsze UX** - natychmiastowe zapisanie i afirmacja
3. **Prostszy kod** - mniej abstrakcji i warstw
4. **Łatwiejsze debugowanie** - prostsze śledzenie stanu
5. **Zachowanie funkcjonalności** - pełna synchronizacja w tle

Przepływ jest gotowy do implementacji i testowania! 🎉
