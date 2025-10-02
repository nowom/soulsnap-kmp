# 🔍 Sync Debug Guide

## Dodane logi do debugowania synchronizacji

### **1. MemoryRepositoryImpl.addMemory()**

```
DEBUG: MemoryRepositoryImpl.addMemory() - starting offline-first save
DEBUG: MemoryRepositoryImpl.addMemory() - saved locally with ID: X
DEBUG: MemoryRepositoryImpl.addMemory() - checking if should sync to server
DEBUG: MemoryRepositoryImpl.addMemory() - isAuthenticated: true/false
DEBUG: MemoryRepositoryImpl.addMemory() - isOnline: true/false
DEBUG: MemoryRepositoryImpl.addMemory() - currentUser: userId, email: email
DEBUG: MemoryRepositoryImpl.addMemory() - creating sync task
DEBUG: MemoryRepositoryImpl.addMemory() - localId: X
DEBUG: MemoryRepositoryImpl.addMemory() - plannedRemotePhotoPath: path
DEBUG: MemoryRepositoryImpl.addMemory() - plannedRemoteAudioPath: path
DEBUG: MemoryRepositoryImpl.addMemory() - calling syncManager.enqueue()
DEBUG: MemoryRepositoryImpl.addMemory() - ✅ enqueued sync task for memory: X
DEBUG: MemoryRepositoryImpl.addMemory() - sync task ID: task_id

WARNING: MemoryRepositoryImpl.addMemory() - no current user, skipping sync
WARNING: MemoryRepositoryImpl.addMemory() - user not authenticated, skipping sync
```

### **2. AdvancedSyncManager.enqueue()**

```
DEBUG: AdvancedSyncManager.enqueue() - ✅ enqueueing task: task_id
DEBUG: AdvancedSyncManager.enqueue() - task type: CreateMemory
DEBUG: AdvancedSyncManager.enqueue() - isOnline: true/false
DEBUG: AdvancedSyncManager.enqueue() - queue size after enqueue: X
DEBUG: AdvancedSyncManager.enqueue() - triggering immediate sync (online)

ERROR: AdvancedSyncManager.enqueue() - ❌ not running, ignoring task: task_id
ERROR: AdvancedSyncManager.enqueue() - ❌ You need to call syncManager.start() first!
WARNING: AdvancedSyncManager.enqueue() - offline, sync will happen when connection is restored
```

### **3. AdvancedSyncManager.processSyncQueue()**

```
DEBUG: AdvancedSyncManager.processSyncQueue() - 🔄 starting sync queue processing
DEBUG: AdvancedSyncManager.processSyncQueue() - found X due tasks
DEBUG: AdvancedSyncManager.processSyncQueue() - processing X tasks

WARNING: AdvancedSyncManager.processSyncQueue() - ⚠️ offline, skipping processing
DEBUG: AdvancedSyncManager.processSyncQueue() - no due tasks, returning
```

### **4. SyncProcessor.processCreateMemory()**

```
DEBUG: SyncProcessor.processCreateMemory() - 🚀 starting CreateMemory task
DEBUG: SyncProcessor.processCreateMemory() - task.localId: X
DEBUG: SyncProcessor.processCreateMemory() - task.plannedRemotePhotoPath: path
DEBUG: SyncProcessor.processCreateMemory() - task.plannedRemoteAudioPath: path
DEBUG: SyncProcessor.processCreateMemory() - ✅ User authenticated: userId
DEBUG: SyncProcessor.processCreateMemory() - ✅ Found memory in database: title
DEBUG: SyncProcessor.processCreateMemory() - ✅ Converted to domain model: title
DEBUG: SyncProcessor.processCreateMemory() - memory.photoUri: uri
DEBUG: SyncProcessor.processCreateMemory() - memory.audioUri: uri
DEBUG: SyncProcessor.processCreateMemory() - 📸 uploading photo...
DEBUG: SyncProcessor.processCreateMemory() - localUri: uri
DEBUG: SyncProcessor.processCreateMemory() - plannedPath: path
DEBUG: SyncProcessor.processCreateMemory() - photoResult.success: true/false
DEBUG: SyncProcessor.processCreateMemory() - ✅ photo uploaded: path
DEBUG: SyncProcessor.processCreateMemory() - no photo to upload
DEBUG: SyncProcessor.processCreateMemory() - 💾 inserting to remote database...
DEBUG: SyncProcessor.processCreateMemory() - userId: userId
DEBUG: SyncProcessor.processCreateMemory() - memory title: title
DEBUG: SyncProcessor.processCreateMemory() - remoteId result: remoteId
DEBUG: SyncProcessor.processCreateMemory() - ✅ memory inserted to remote database, remoteId: X

ERROR: SyncProcessor.processCreateMemory() - ❌ User not authenticated
ERROR: SyncProcessor.processCreateMemory() - ❌ Memory not found in database: X
ERROR: SyncProcessor.processCreateMemory() - ❌ photo upload failed: error
ERROR: SyncProcessor.processCreateMemory() - ❌ Failed to insert memory to remote database
```

### **5. SupabaseMemoryDataSource.insertMemory()**

```
DEBUG: SupabaseMemoryDataSource.insertMemory() - uploading files...
DEBUG: SupabaseMemoryDataSource.insertMemory() - photo uploaded to: url
DEBUG: SupabaseMemoryDataSource.insertMemory() - audio uploaded to: url
DEBUG: SupabaseMemoryDataSource.insertMemory() - inserting to database...
DEBUG: SupabaseMemoryDataSource.insertMemory() - ✅ inserted successfully, remoteId: X
```

## 🔍 Jak debugować problemy:

### **Problem 1: Brak synchronizacji**

**Sprawdź w logach:**
1. `MemoryRepositoryImpl.init() - ✅ SyncManager started` - czy SyncManager został uruchomiony?
2. `isAuthenticated: true/false` - czy użytkownik jest zalogowany?
3. `isOnline: true/false` - czy jest połączenie z internetem?
4. `currentUser: userId, email: email` - czy currentUser istnieje?

**Możliwe przyczyny:**
- ❌ SyncManager nie został uruchomiony
- ❌ Użytkownik nie jest zalogowany
- ❌ Brak połączenia z internetem
- ❌ currentUser = null

### **Problem 2: SyncManager nie działa**

**Sprawdź w logach:**
1. `ERROR: AdvancedSyncManager.enqueue() - ❌ not running` - SyncManager nie został uruchomiony
2. `WARNING: AdvancedSyncManager.enqueue() - offline` - brak połączenia

**Rozwiązanie:**
- Upewnij się, że `syncManager.start()` jest wywołany w `MemoryRepositoryImpl.init()`
- Sprawdź status połączenia z internetem

### **Problem 3: Upload photo/audio failed**

**Sprawdź w logach:**
1. `photoResult.success: false` - błąd uploadu
2. `ERROR: SyncProcessor.processCreateMemory() - ❌ photo upload failed` - szczegóły błędu

**Możliwe przyczyny:**
- ❌ Plik nie istnieje lokalnie
- ❌ Błąd połączenia z Supabase Storage
- ❌ Nieprawidłowe uprawnienia

### **Problem 4: Insert to database failed**

**Sprawdź w logach:**
1. `remoteId result: null` - błąd insertu
2. `ERROR: SyncProcessor.processCreateMemory() - ❌ Failed to insert` - błąd bazy danych

**Możliwe przyczyny:**
- ❌ Błąd Supabase API
- ❌ Nieprawidłowe dane
- ❌ Błąd autoryzacji

## 📊 Monitorowanie sync status:

```kotlin
// Sprawdź status synchronizacji
val syncStatus = syncManager.status.value
println("Sync status: running=${syncStatus.running}, pending=${syncStatus.pendingCount}")
```

## 🎯 Kluczowe punkty kontrolne:

1. **✅ SyncManager.start()** - czy został wywołany w init?
2. **✅ User authenticated** - czy użytkownik jest zalogowany?
3. **✅ Online status** - czy jest połączenie z internetem?
4. **✅ Task enqueued** - czy task został dodany do kolejki?
5. **✅ Task processing** - czy task jest przetwarzany?
6. **✅ Upload success** - czy pliki zostały wgrane?
7. **✅ Database insert** - czy rekord został dodany do bazy?

## 🚀 Oczekiwany flow logów:

```
1. MemoryRepositoryImpl.init() - ✅ SyncManager started
2. MemoryRepositoryImpl.addMemory() - starting offline-first save
3. MemoryRepositoryImpl.addMemory() - saved locally with ID: 1
4. MemoryRepositoryImpl.addMemory() - isAuthenticated: true
5. MemoryRepositoryImpl.addMemory() - isOnline: true
6. MemoryRepositoryImpl.addMemory() - currentUser: user_123, email: test@test.com
7. MemoryRepositoryImpl.addMemory() - calling syncManager.enqueue()
8. AdvancedSyncManager.enqueue() - ✅ enqueueing task: CREATE_1
9. AdvancedSyncManager.enqueue() - task type: CreateMemory
10. AdvancedSyncManager.enqueue() - triggering immediate sync (online)
11. AdvancedSyncManager.processSyncQueue() - 🔄 starting sync queue processing
12. AdvancedSyncManager.processSyncQueue() - found 1 due tasks
13. SyncProcessor.processCreateMemory() - 🚀 starting CreateMemory task
14. SyncProcessor.processCreateMemory() - ✅ User authenticated: user_123
15. SyncProcessor.processCreateMemory() - ✅ Found memory in database: My Memory
16. SyncProcessor.processCreateMemory() - 📸 uploading photo...
17. SyncProcessor.processCreateMemory() - ✅ photo uploaded: path
18. SyncProcessor.processCreateMemory() - 💾 inserting to remote database...
19. SyncProcessor.processCreateMemory() - ✅ memory inserted to remote database
20. SyncProcessor.run() - task completed successfully: CREATE_1
```

## ⚠️ Typowe błędy:

### **Błąd: "not running, ignoring task"**
```
ERROR: AdvancedSyncManager.enqueue() - ❌ not running, ignoring task
```
**Rozwiązanie:** SyncManager.start() nie został wywołany. Dodano automatyczne wywołanie w `MemoryRepositoryImpl.init()`.

### **Błąd: "User not authenticated"**
```
ERROR: SyncProcessor.processCreateMemory() - ❌ User not authenticated
```
**Rozwiązanie:** Użytkownik musi być zalogowany. Sprawdź `userSessionManager.isAuthenticated()`.

### **Błąd: "offline, skipping processing"**
```
WARNING: AdvancedSyncManager.processSyncQueue() - ⚠️ offline, skipping processing
```
**Rozwiązanie:** Brak połączenia z internetem. Sync zostanie wznowiony gdy połączenie wróci.

## 🎯 Następne kroki debugowania:

Jeśli sync nadal nie działa, uruchom aplikację i sprawdź logi w takiej kolejności:

1. Czy widzisz `MemoryRepositoryImpl.init() - ✅ SyncManager started`?
2. Czy widzisz `isAuthenticated: true`?
3. Czy widzisz `isOnline: true`?
4. Czy widzisz `✅ enqueued sync task`?
5. Czy widzisz `🔄 starting sync queue processing`?
6. Czy widzisz `🚀 starting CreateMemory task`?
7. Czy widzisz `✅ memory inserted to remote database`?

Jeśli któryś z tych kroków nie pojawia się w logach, to tam jest problem!
