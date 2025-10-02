# 🔍 Sync Troubleshooting

## Problem: "found 0 due tasks" ale są wspomnienia w bazie

### **Diagnoza:**

Widzisz w logach:
```
DEBUG: AdvancedSyncManager.processSyncQueue() - 🔄 starting sync queue processing
DEBUG: AdvancedSyncManager.processSyncQueue() - found 0 due tasks
DEBUG: AdvancedSyncManager.processSyncQueue() - no due tasks, returning
```

### **Dlaczego to się dzieje:**

1. **Stare wspomnienia** - dodane przed implementacją nowych logów
2. **Brak tasków w SyncQueue** - stare memories nie mają tasków
3. **syncState = PENDING** - są w bazie, ale nie w kolejce

### **Rozwiązanie:**

#### **Opcja 1: Dodaj NOWE wspomnienie (REKOMENDOWANE)**

1. ✅ **Uruchom aplikację**
2. ✅ **Zaloguj się** (upewnij się że nie jesteś gościem)
3. ✅ **Dodaj NOWY snap** (z aparatu)
4. ✅ **Sprawdź logi** - powinno być:

```
========================================
🔄 MemoryRepositoryImpl.addMemory() - CHECKING SYNC CONDITIONS
========================================
📊 isAuthenticated: true
📊 isOnline: true
DEBUG: MemoryRepositoryImpl.addMemory() - currentUser: xxx, email: xxx
========================================
📤 MemoryRepositoryImpl.addMemory() - ENQUEUEING SYNC TASK
========================================
DEBUG: AdvancedSyncManager.enqueue() - ✅ enqueueing task: CREATE:1
DEBUG: AdvancedSyncManager.enqueue() - task type: CreateMemory
DEBUG: AdvancedSyncManager.enqueue() - queue size after enqueue: 1  👈 POWINNO BYĆ > 0
DEBUG: AdvancedSyncManager.enqueue() - triggering immediate sync (online)
DEBUG: AdvancedSyncManager.processSyncQueue() - found 1 due tasks  👈 POWINNO BYĆ > 0
```

#### **Opcja 2: Ręczna migracja istniejących danych**

Stwórz funkcję do re-enqueue istniejących memories:

```kotlin
suspend fun requeueExistingMemories() {
    val allMemories = memoryDao.getAll().first()
    val currentUser = userSessionManager.getCurrentUser() ?: return
    
    allMemories.forEach { dbMemory ->
        if (dbMemory.syncState == "PENDING" && dbMemory.remoteId == null) {
            val createTask = CreateMemory(
                localId = dbMemory.id,
                plannedRemotePhotoPath = StoragePaths.photoPath(currentUser.userId, dbMemory.id),
                plannedRemoteAudioPath = dbMemory.audioUri?.let { 
                    StoragePaths.audioPath(currentUser.userId, dbMemory.id) 
                }
            )
            syncManager.enqueue(createTask)
        }
    }
}
```

#### **Opcja 3: Wymuś synchronizację przez UI**

Dodaj przycisk w Settings:
```kotlin
Button(onClick = { 
    viewModel.forceSyncAll() 
}) {
    Text("🔄 Synchronizuj dane")
}
```

### **Kluczowe spostrzeżenia:**

1. **✅ SyncManager działa** - processing loop co sekundę
2. **✅ Kompilacja OK** - serialization naprawiona
3. **❌ Brak tasków** - stare wspomnienia nie mają tasków w kolejce
4. **✅ Nowe wspomnienia** - będą synchronizowane automatycznie

### **Quick Test:**

1. Usuń aplikację (clear data)
2. Zainstaluj na nowo
3. Zaloguj się
4. Dodaj nowy snap
5. Sprawdź logi

**Powinieneś zobaczyć pełny flow synchronizacji!** 🚀

### **Monitoring SyncQueue:**

Dodaj do DashboardScreen lub SettingsScreen:

```kotlin
LaunchedEffect(Unit) {
    syncManager.status.collect { status ->
        println("📊 Sync Status: running=${status.running}, pending=${status.pendingCount}")
    }
}
```

To pokaże ile tasków jest w kolejce w czasie rzeczywistym!
