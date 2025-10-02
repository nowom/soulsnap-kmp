# ✅ Test Sync Checklist

## Sprawdź te logi po dodaniu nowego snap'a:

### **1. 🚀 SyncManager Initialization**
```
========================================
🚀 MemoryRepositoryImpl.init() - STARTING SYNC MANAGER
========================================
📡 Starting connectivity monitor...
✅ Connectivity monitor started
🔄 Starting processing loop...
✅ Processing loop started
⏰ Scheduling periodic wake-ups...
✅ Scheduler configured
========================================
✅ AdvancedSyncManager.start() - SYNC MANAGER RUNNING
========================================
```

**❌ Jeśli NIE widzisz tego:** SyncManager nie został uruchomiony!

---

### **2. 💾 Memory Save**
```
DEBUG: MemoryRepositoryImpl.addMemory() - starting offline-first save
DEBUG: MemoryRepositoryImpl.addMemory() - saved locally with ID: X
```

**✅ To powinno zawsze działać** (offline-first)

---

### **3. 🔄 Sync Conditions Check**
```
========================================
🔄 MemoryRepositoryImpl.addMemory() - CHECKING SYNC CONDITIONS
========================================
📊 isAuthenticated: true
📊 isOnline: true
```

**❌ Jeśli `isAuthenticated: false`:**
- Użytkownik NIE jest zalogowany
- Tylko tryb offline
- Dane są tylko lokalnie

**❌ Jeśli `isOnline: false`:**
- Brak internetu
- Sync zostanie wykonany później gdy internet wróci

---

### **4. 👤 User Check**
```
DEBUG: MemoryRepositoryImpl.addMemory() - currentUser: user_123, email: test@test.com
```

**❌ Jeśli widzisz:**
```
WARNING: MemoryRepositoryImpl.addMemory() - no current user, skipping sync
```
- Problem z sesją użytkownika
- Sprawdź `UserSessionManager`

---

### **5. 📤 Enqueue Sync Task**
```
========================================
📤 MemoryRepositoryImpl.addMemory() - ENQUEUEING SYNC TASK
========================================
========================================
✅ MemoryRepositoryImpl.addMemory() - SYNC TASK ENQUEUED!
📋 Task ID: CREATE_MEMORY_1_123456789
========================================
```

**❌ Jeśli NIE widzisz tego:** Sync task nie został dodany do kolejki!

---

### **6. 🔄 SyncManager Processing**
```
DEBUG: AdvancedSyncManager.enqueue() - ✅ enqueueing task: CREATE_MEMORY_1
DEBUG: AdvancedSyncManager.enqueue() - task type: CreateMemory
DEBUG: AdvancedSyncManager.enqueue() - isOnline: true
DEBUG: AdvancedSyncManager.enqueue() - queue size after enqueue: 1
DEBUG: AdvancedSyncManager.enqueue() - triggering immediate sync (online)
```

**❌ Jeśli widzisz:**
```
ERROR: AdvancedSyncManager.enqueue() - ❌ not running, ignoring task
```
- SyncManager nie został uruchomiony
- Problem z inicjalizacją

---

### **7. 🔄 Queue Processing**
```
DEBUG: AdvancedSyncManager.processSyncQueue() - 🔄 starting sync queue processing
DEBUG: AdvancedSyncManager.processSyncQueue() - found 1 due tasks
DEBUG: AdvancedSyncManager.processSyncQueue() - processing 1 tasks
```

**❌ Jeśli widzisz:**
```
WARNING: AdvancedSyncManager.processSyncQueue() - ⚠️ offline, skipping processing
```
- Brak internetu, sync czeka

---

### **8. 🚀 Task Processing**
```
DEBUG: SyncProcessor.processCreateMemory() - 🚀 starting CreateMemory task
DEBUG: SyncProcessor.processCreateMemory() - task.localId: 1
DEBUG: SyncProcessor.processCreateMemory() - ✅ User authenticated: user_123
DEBUG: SyncProcessor.processCreateMemory() - ✅ Found memory in database: My Memory
DEBUG: SyncProcessor.processCreateMemory() - ✅ Converted to domain model: My Memory
```

---

### **9. 📸 Photo Upload**
```
DEBUG: SyncProcessor.processCreateMemory() - 📸 uploading photo...
DEBUG: SyncProcessor.processCreateMemory() - localUri: photo_123.jpg
DEBUG: SyncProcessor.processCreateMemory() - plannedPath: photos/user_123/1
DEBUG: SyncProcessor.processCreateMemory() - photoResult.success: true
DEBUG: SyncProcessor.processCreateMemory() - ✅ photo uploaded: path
```

**❌ Jeśli widzisz:**
```
ERROR: SyncProcessor.processCreateMemory() - ❌ photo upload failed
```
- Problem z Supabase Storage
- Sprawdź bucket permissions

---

### **10. 💾 Database Insert**
```
DEBUG: SyncProcessor.processCreateMemory() - 💾 inserting to remote database...
DEBUG: SyncProcessor.processCreateMemory() - userId: user_123
DEBUG: SyncProcessor.processCreateMemory() - memory title: My Memory
DEBUG: SyncProcessor.processCreateMemory() - remoteId result: 456
DEBUG: SyncProcessor.processCreateMemory() - ✅ memory inserted to remote database, remoteId: 456
```

**❌ Jeśli widzisz:**
```
ERROR: SyncProcessor.processCreateMemory() - ❌ Failed to insert memory to remote database
```
- Problem z Supabase database
- Sprawdź permissions i schema

---

### **11. ✅ Success**
```
DEBUG: SyncProcessor.run() - task completed successfully: CREATE_MEMORY_1
```

**🎉 Jeśli widzisz ten log:** Synchronizacja zakończona sukcesem!

---

## 🔍 Diagnostic Commands:

### **Uruchom aplikację i wykonaj:**
1. Zaloguj się jako użytkownik (nie gość!)
2. Dodaj nowy snap
3. Sprawdź logi w logcat
4. Znajdź sekcje z `========================================`

### **Filtruj logi:**
```bash
# Android Studio Logcat filter:
tag:System.out package:pl.soulsnaps
```

### **Szukaj kluczowych komunikatów:**
```
🚀 STARTING SYNC MANAGER
✅ SYNC MANAGER RUNNING
🔄 CHECKING SYNC CONDITIONS
📊 isAuthenticated: true
📊 isOnline: true
✅ SYNC TASK ENQUEUED!
🚀 starting CreateMemory task
✅ memory inserted to remote database
```

## ⚠️ Typowe problemy:

### **Problem: Brak logów synchronizacji**
**Możliwe przyczyny:**
1. MemoryRepositoryImpl nie został zainicjalizowany
2. Logi są filtrowane
3. SyncManager crashuje przy start()

**Rozwiązanie:** Sprawdź czy widzisz `🚀 STARTING SYNC MANAGER` w logach

### **Problem: "isAuthenticated: false"**
**Przyczyna:** Użytkownik nie jest zalogowany

**Rozwiązanie:** 
1. Zaloguj się przez ekran logowania
2. Sprawdź czy `UserSessionManager` ma sesję
3. Sprawdź `currentUser` w settings

### **Problem: "isOnline: false"**
**Przyczyna:** Brak połączenia z internetem

**Rozwiązanie:**
1. Sprawdź połączenie Wi-Fi/mobile data
2. Sync wykona się automatycznie gdy internet wróci

### **Problem: "not running, ignoring task"**
**Przyczyna:** SyncManager nie został uruchomiony

**Rozwiązanie:** 
- Naprawione! `syncManager.start()` jest teraz w `MemoryRepositoryImpl.init()`
- Sprawdź czy widzisz logi inicjalizacji

## 🎯 Quick Check:

Uruchom app i sprawdź czy widzisz te 3 kluczowe logi:

1. ✅ `🚀 STARTING SYNC MANAGER`
2. ✅ `✅ SYNC MANAGER RUNNING`
3. ✅ `📊 isAuthenticated: true` (po dodaniu snap'a)

Jeśli tak - synchronizacja powinna działać!
Jeśli nie - problem jest w inicjalizacji lub autentykacji.
