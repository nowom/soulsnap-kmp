# SoulSnaps iOS Sync Documentation

## iOS Background Sync Implementation

### 1. **Architecture Overview**

iOS używa **Background Tasks** zamiast WorkManager:

```
[UI] → [IOSSyncManager] → [SyncEngine] → [BackgroundTaskHandler]
  ↓
[NetworkMonitor] → [IOSStorageService] → [Supabase Storage]
  ↓
[IOSRemoteApiService] → [Supabase API]
```

### 2. **Key Components**

#### **IOSSyncManager**
- Zarządza synchronizacją na iOS
- Używa `CoroutineScope` z `Dispatchers.Main`
- Integruje się z iOS Background Tasks

#### **BackgroundTaskHandler (Swift)**
- Rejestruje background task w iOS
- Obsługuje `BGAppRefreshTask`
- Automatycznie planuje następne zadania

#### **NetworkMonitor (Swift)**
- Używa `NWPathMonitor` z Network framework
- Automatycznie wyzwala sync przy powrocie online
- `@Published` property dla SwiftUI

#### **IOSStorageService**
- Upload plików przez `URLSession`
- Integracja z Supabase Storage
- Obsługa retry logic

### 3. **Background Task Configuration**

#### **Info.plist**
```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.soulsnaps.sync</string>
</array>
<key>UIBackgroundModes</key>
<array>
    <string>background-app-refresh</string>
    <string>background-processing</string>
</array>
```

#### **Background Task Registration**
```swift
BGTaskScheduler.shared.register(
    forTaskWithIdentifier: "com.soulsnaps.sync",
    using: nil
) { task in
    self.handleBackgroundTask(task: task as! BGAppRefreshTask)
}
```

### 4. **Sync Flow**

#### **Immediate Sync**
```kotlin
// 1. User creates snap
val result = createSnapUseCase(...)

// 2. Enqueue sync
syncManager.enqueueSync(draftId)

// 3. Background task executes
syncEngine.syncDraft(draftId)
```

#### **Periodic Sync**
```swift
// 1. Schedule background task
let request = BGAppRefreshTaskRequest(identifier: "com.soulsnaps.sync")
request.earliestBeginDate = Date(timeIntervalSinceNow: 30 * 60) // 30 min
try BGTaskScheduler.shared.submit(request)

// 2. iOS executes when appropriate
// 3. BackgroundTaskHandler calls syncManager.handleBackgroundTask()
```

### 5. **Network Monitoring**

#### **NetworkMonitor (Swift)**
```swift
class NetworkMonitor: ObservableObject {
    private let networkMonitor = NWPathMonitor()
    @Published var isOnline = false
    
    private func startMonitoring() {
        networkMonitor.pathUpdateHandler = { path in
            DispatchQueue.main.async {
                self.isOnline = path.status == .satisfied
                if self.isOnline {
                    self.syncManager?.enqueuePendingSync()
                }
            }
        }
        networkMonitor.start(queue: workerQueue)
    }
}
```

#### **Integration with SyncManager**
```kotlin
// NetworkMonitor calls syncManager when online
syncManager.enqueuePendingSync()
```

### 6. **Storage Service**

#### **URLSession Upload**
```kotlin
// TODO: Implement actual Supabase upload
val url = URL("$supabaseUrl/storage/v1/object/snap-images/$storagePath")
var request = URLRequest(url)
request.httpMethod = "POST"
request.setValue("Bearer $supabaseKey", "Authorization")
request.setValue("image/$extension", "Content-Type")

val (data, response) = URLSession.shared.upload(for: request, from: fileData)
```

#### **Storage Path Structure**
```
snap-images/
  └── {user_id}/
      └── images/
          └── {timestamp}/
              └── {draft_id}.{ext}
```

### 7. **API Service**

#### **Supabase REST API**
```kotlin
// Create snap
val url = URL("$supabaseUrl/rest/v1/memories")
var request = URLRequest(url)
request.httpMethod = "POST"
request.setValue("Bearer $supabaseKey", "Authorization")
request.setValue("application/json", "Content-Type")
request.setValue("return=representation", "Prefer")

val jsonData = Json.encodeToString(snap)
request.httpBody = jsonData.toByteArray()
```

### 8. **Error Handling**

#### **Background Task Expiration**
```swift
task.expirationHandler = {
    print("Background task expired")
    task.setTaskCompleted(success: false)
}
```

#### **Retry Logic**
```kotlin
// Exponential backoff
val retryDelay = minOf(
    config.retryDelayMs * (2.0.pow(retryCount)),
    config.maxRetryDelayMs
)
```

### 9. **Performance Considerations**

#### **Background Task Limits**
- iOS ogranicza background tasks do ~30 sekund
- Używaj `BGAppRefreshTask` dla krótkich operacji
- Dla długich operacji użyj `BGProcessingTask`

#### **Battery Optimization**
- iOS automatycznie ogranicza background tasks
- Użytkownik musi włączyć "Background App Refresh"
- Sync tylko gdy urządzenie jest na ładowaniu

#### **Network Efficiency**
- Batch sync (max 10 snapów na raz)
- Compressed uploads
- Smart retry logic

### 10. **Testing**

#### **Unit Tests**
```kotlin
// Test sync engine
val mockStorageService = IOSMockStorageService()
val mockNetworkMonitor = IOSMockNetworkMonitor()
val syncEngine = SyncEngineImpl(...)
```

#### **Integration Tests**
```swift
// Test background task
let expectation = XCTestExpectation(description: "Background sync")
syncManager.handleBackgroundTask { success in
    XCTAssertTrue(success)
    expectation.fulfill()
}
```

#### **UI Tests**
```swift
// Test offline/online scenarios
app.buttons["Add Snap"].tap()
// Simulate network loss
// Verify local save
// Simulate network restore
// Verify sync
```

### 11. **Deployment**

#### **App Store Requirements**
- Background tasks muszą być uzasadnione
- Opisz cel w App Store Connect
- Testuj na różnych urządzeniach

#### **User Permissions**
- "Background App Refresh" musi być włączone
- Użytkownik może wyłączyć dla całej aplikacji
- Graceful degradation gdy wyłączone

### 12. **Monitoring**

#### **Analytics**
```kotlin
// Track sync events
analytics.track("sync_started", mapOf("draft_count" to pendingCount))
analytics.track("sync_completed", mapOf("success" to success))
analytics.track("sync_failed", mapOf("error" to errorMessage))
```

#### **Debug Logging**
```kotlin
println("DEBUG: IOSSyncManager.enqueueSync() - enqueueing sync for draft: $draftId")
```

### 13. **Troubleshooting**

#### **Common Issues**
1. **Background tasks nie działają**
   - Sprawdź Info.plist
   - Sprawdź Background App Refresh
   - Testuj na urządzeniu (nie simulator)

2. **Sync nie działa offline**
   - Sprawdź NetworkMonitor
   - Sprawdź local storage
   - Sprawdź retry logic

3. **Upload fails**
   - Sprawdź Supabase credentials
   - Sprawdź network connectivity
   - Sprawdź file permissions

#### **Debug Tools**
- Xcode Console dla logów
- Network Inspector dla API calls
- Background Task Debugger

### 14. **Best Practices**

#### **iOS Specific**
- Używaj `@Published` dla SwiftUI
- Implementuj `ObservableObject` dla state
- Używaj `DispatchQueue.main.async` dla UI updates

#### **Background Tasks**
- Planuj następne zadanie po każdym wykonaniu
- Obsługuj expiration gracefully
- Minimalizuj czas wykonania

#### **Network**
- Sprawdzaj connectivity przed sync
- Implementuj exponential backoff
- Cache offline data

#### **Storage**
- Compress files before upload
- Use appropriate file formats
- Implement cleanup for failed uploads
