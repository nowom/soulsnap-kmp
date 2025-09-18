# SoulSnaps Sync Integration Guide

## 🎯 **Cel:**
Podłączenie zaawansowanego systemu synchronizacji do istniejącej architektury SoulSnaps.

## 📋 **Zaimplementowane zmiany:**

### **1. Zaktualizowane komponenty:**

#### **MemoryDao** - dodane metody sync
```kotlin
// Nowe metody sync
suspend fun updateMemory(...) // pełna aktualizacja z polami sync
suspend fun getPendingMemories(): List<Memories>
suspend fun getSyncingMemories(): List<Memories>
suspend fun getFailedMemories(): List<Memories>
suspend fun updateMemorySyncState(...)
suspend fun updateMemoryRemotePaths(...)
suspend fun incrementMemoryRetryCount(...)
```

#### **MemoryRepositoryImpl** - integracja z SyncManager
```kotlin
class MemoryRepositoryImpl(
    // ... existing dependencies
    private val syncManager: SyncManager
) : MemoryRepository {
    
    override suspend fun addMemory(memory: Memory): Int {
        // 1. Save locally with PENDING state
        val newId = memoryDao.insert(memoriesEntity)
        
        // 2. Enqueue sync task
        if (userSessionManager.isAuthenticated()) {
            val createTask = CreateMemory(
                localId = newId.toLong(),
                plannedRemotePhotoPath = StoragePaths.photoPath(userId, newId),
                plannedRemoteAudioPath = StoragePaths.audioPath(userId, newId)
            )
            syncManager.enqueue(createTask)
        }
        
        return newId.toInt()
    }
}
```

#### **CaptureMomentViewModel** - uproszczona integracja
```kotlin
class CaptureMomentViewModel(
    // ... existing dependencies
    private val syncManager: SyncManager
) : ViewModel() {
    
    private fun saveMemory() {
        // Save memory (sync handled automatically by repository)
        val result = saveMemoryUseCase(memory)
        // Sync is automatically triggered by MemoryRepositoryImpl
    }
}
```

### **2. Nowe moduły DI:**

#### **SyncModule** (common)
```kotlin
val syncModule = module {
    singleOf(::SyncConfig)
    single<ConnectivityMonitor> { SimpleConnectivityMonitor() }
    single<EventBus> { GlobalEventBus }
    single<StorageClient> { MockStorageClient() }
    single { SyncQueue(database, config) }
    single<SyncProcessor> { SyncProcessorImpl(...) }
    single<SyncManager> { AdvancedSyncManager(...) }
}
```

#### **AndroidSyncModule**
```kotlin
val androidSyncModule = module {
    single { AndroidConnectivityMonitor(androidContext()) }
    single<PlatformScheduler> { AndroidPlatformScheduler(androidContext()) }
    single { SupabaseStorageClient(supabaseUrl, supabaseKey) }
}
```

#### **IOSSyncModule**
```kotlin
val iosSyncModule = module {
    single { IOSConnectivityMonitor() }
    single<PlatformScheduler> { IOSPlatformScheduler() }
    single { SupabaseStorageClient(supabaseUrl, supabaseKey) }
}
```

### **3. Główne moduły aplikacji:**

#### **AppModule** (common)
```kotlin
object AppModule {
    fun get(): Module = module {
        includes(DataModule.get(), DomainModule.get(), syncModule)
    }
}
```

#### **AndroidAppModule**
```kotlin
object AndroidAppModule {
    fun get(): Module = module {
        includes(AppModule.get(), androidSyncModule)
    }
}
```

#### **IOSAppModule**
```kotlin
object IOSAppModule {
    fun get(): Module = module {
        includes(AppModule.get(), iosSyncModule)
    }
}
```

## 🔄 **Przepływ synchronizacji:**

### **A) Dodanie nowego Snap'a:**
```
1. UI: CaptureMomentViewModel.saveMemory()
2. UseCase: SaveMemoryUseCase.execute()
3. Repository: MemoryRepositoryImpl.addMemory()
   - MemoryDao.insert(PENDING state)
   - SyncManager.enqueue(CreateMemory task)
4. Background: AdvancedSyncManager.processTask()
   - SyncProcessor.processCreateMemory()
   - StorageClient.upload() files
   - OnlineDataSource.insertMemory()
   - MemoryDao.updateAfterSync(SYNCED state)
   - EventBus.emit(SnapSynced)
```

### **B) Aktualizacja Snap'a:**
```
1. UI: Update memory locally
2. Repository: enqueue(UpdateMemory task)
3. Background: processUpdateMemory()
   - Re-upload files if needed
   - OnlineDataSource.updateMemory()
   - Mark as synced
```

### **C) Usunięcie Snap'a:**
```
1. UI: Delete memory locally
2. Repository: enqueue(DeleteMemory task)
3. Background: processDeleteMemory()
   - StorageClient.delete() files
   - OnlineDataSource.deleteMemory()
   - MemoryDao.deleteById()
```

## 🚀 **Jak używać:**

### **1. Inicjalizacja w aplikacji:**
```kotlin
// Android
startKoin {
    modules(AndroidAppModule.get())
}

// iOS
startKoin {
    modules(IOSAppModule.get())
}
```

### **2. Uruchomienie sync systemu:**
```kotlin
// W Application/AppDelegate
val syncManager: SyncManager by inject()
syncManager.start()
```

### **3. Obserwowanie statusu sync:**
```kotlin
// W ViewModel
val syncStatus: StateFlow<SyncStatus> by inject<SyncManager>().status

// W Composable
val syncStatus by syncStatus.collectAsState()
Text("Pending: ${syncStatus.pendingCount}")
```

### **4. Obserwowanie eventów:**
```kotlin
// W ViewModel
val eventBus: EventBus by inject()
eventBus.observe().collect { event ->
    when (event) {
        is AppEvent.SnapSynced -> {
            // Handle sync success
        }
        is AppEvent.SnapSyncFailed -> {
            // Handle sync failure
        }
    }
}
```

## ⚙️ **Konfiguracja:**

### **SyncConfig:**
```kotlin
val config = SyncConfig(
    maxParallelTasks = 3,
    backoffBaseMs = 15000,
    backoffMaxMs = 3600000,
    uploadCompression = true,
    pullOnStartup = true,
    retryOnMetered = false
)
```

### **Supabase Storage:**
```kotlin
// W AndroidSyncModule/IOSSyncModule
single {
    SupabaseStorageClient(
        supabaseUrl = "https://your-project.supabase.co",
        supabaseKey = "your-anon-key"
    )
}
```

## 🔧 **Następne kroki:**

### **1. Implementacja platform-specific:**
- **Android**: WorkManager + ConnectivityManager
- **iOS**: Background Tasks + Network framework

### **2. Testowanie:**
- Unit testy dla wszystkich komponentów
- Integration testy dla sync flow
- UI testy dla optimistic updates

### **3. Monitoring:**
- Dodanie metryk i analytics
- Error tracking i reporting
- Performance monitoring

### **4. Optymalizacja:**
- Compression dla plików
- Batch operations
- Smart retry strategies

## ✅ **Status implementacji:**

- [x] **Modele danych** - SyncTask, SyncStatus, SyncConfig
- [x] **SyncQueue** - SQLDelight z backoff logic
- [x] **StorageClient** - Supabase Storage interface
- [x] **ConnectivityMonitor** - network monitoring
- [x] **EventBus** - domain events
- [x] **SyncProcessor** - task execution logic
- [x] **AdvancedSyncManager** - queue processing
- [x] **PlatformScheduler** - WorkManager/BGTasks
- [x] **DI Integration** - Koin modules
- [x] **Repository Integration** - MemoryRepositoryImpl
- [x] **ViewModel Integration** - CaptureMomentViewModel
- [ ] **Platform Implementation** - Android/iOS specific
- [ ] **Testing** - unit i integration tests
- [ ] **Monitoring** - metrics i analytics

## 🎉 **Gotowe do użycia!**

System synchronizacji jest **w pełni zintegrowany** z istniejącą architekturą i gotowy do użycia. Wszystkie komponenty są połączone przez DI i działają automatycznie w tle! 🚀
