# Koin Multiplatform DI Guide

## 🎯 **Cel:**
Implementacja Dependency Injection z Koin Multiplatform zgodnie z najlepszymi praktykami - commonMain definiuje interfejsy, platform modules dostarczają implementacje.

## 📋 **Architektura DI:**

### **1. Struktura modułów:**

```
commonMain/
├── di/
│   ├── CommonModule.kt          # Interfejsy + wspólne implementacje
│   └── KoinInitializer.kt       # Helper do inicjalizacji

androidMain/
├── di/
│   ├── AndroidModule.kt         # Android implementacje
│   └── AndroidKoinInitializer.kt # Android initializer

iosMain/
├── di/
│   ├── IOSModule.kt             # iOS implementacje
│   ├── IOSKoinInitializer.kt    # iOS initializer
│   └── KoinHelper.kt            # Helper dla Swift
```

### **2. CommonModule (commonMain):**

```kotlin
val commonModule = module {
    // Sync configuration
    singleOf(::SyncConfig)
    
    // Event bus (singleton)
    single<EventBus> { GlobalEventBus }
    
    // Sync queue
    single { SyncQueue(database, config) }
    
    // Sync processor
    single<SyncProcessor> { SyncProcessorImpl(...) }
    
    // Sync manager
    single<SyncManager> { AdvancedSyncManager(...) }
    
    // Platform-specific dependencies (interfejsy)
    single<ConnectivityMonitor> { get() }
    single<PlatformScheduler> { get() }
    single<StorageClient> { get() }
}
```

### **3. AndroidModule (androidMain):**

```kotlin
fun androidModule(): Module = module {
    // Android connectivity monitor
    single<ConnectivityMonitor> {
        AndroidConnectivityMonitor(androidContext())
    }
    
    // Android platform scheduler
    single<PlatformScheduler> {
        AndroidPlatformScheduler(androidContext())
    }
    
    // Supabase storage client
    single<StorageClient> {
        SupabaseStorageClient(supabaseUrl, supabaseKey)
    }
}
```

### **4. IOSModule (iosMain):**

```kotlin
fun iosModule(): Module = module {
    // iOS connectivity monitor
    single<ConnectivityMonitor> {
        IOSConnectivityMonitor()
    }
    
    // iOS platform scheduler
    single<PlatformScheduler> {
        IOSPlatformScheduler()
    }
    
    // Supabase storage client
    single<StorageClient> {
        SupabaseStorageClient(supabaseUrl, supabaseKey)
    }
}
```

## 🚀 **Inicjalizacja w aplikacjach:**

### **Android:**

```kotlin
// AndroidApplication.kt
class AndroidApplication : Application() {
    
    private val syncManager: SyncManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin with platform module
        AndroidKoinInitializer.initialize()
        
        // Start sync manager
        syncManager.start()
    }
}
```

### **iOS:**

```swift
// iOSApp.swift
@main
struct iOSApp: App {
    
    init() {
        // Initialize Koin with platform module
        IOSKoinInitializerKt.initialize()
        
        // Start sync manager
        let syncManager = KoinHelperKt.getSyncManager()
        syncManager.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## 🔧 **Użycie w kodzie:**

### **Android (Kotlin):**

```kotlin
class MyViewModel : ViewModel() {
    
    private val syncManager: SyncManager by inject()
    private val eventBus: EventBus by inject()
    
    fun startSync() {
        syncManager.start()
    }
    
    fun observeEvents() {
        eventBus.observe().collect { event ->
            // Handle events
        }
    }
}
```

### **iOS (Swift):**

```swift
class MyViewController: UIViewController {
    
    private let syncManager = KoinHelperKt.getSyncManager()
    private let eventBus = KoinHelperKt.getEventBus()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Start sync
        syncManager.start()
        
        // Observe events
        // (Event observation would need additional Swift/Kotlin interop)
    }
}
```

## ✅ **Zalety tego podejścia:**

### **1. Automatyczne wstrzykiwanie:**
- Koin automatycznie rozwiązuje zależności
- Type-safe dependency injection
- Lazy initialization

### **2. Platform-specific implementacje:**
- CommonMain definiuje interfejsy
- Platform modules dostarczają implementacje
- Czysta separacja odpowiedzialności

### **3. Łatwość testowania:**
- Mock implementations przez DI
- Test modules z fake dependencies
- Izolacja testów

### **4. Skalowalność:**
- Modularna architektura
- Łatwe dodawanie nowych dependencies
- Centralne zarządzanie konfiguracją

## 🧪 **Testowanie:**

### **Test Module:**

```kotlin
val testModule = module {
    single<ConnectivityMonitor> { MockConnectivityMonitor() }
    single<PlatformScheduler> { MockPlatformScheduler() }
    single<StorageClient> { MockStorageClient() }
}

// W teście
startKoin {
    modules(commonModule, testModule)
}
```

## 📱 **Platform-specific implementacje:**

### **Android:**
- `AndroidConnectivityMonitor` - ConnectivityManager
- `AndroidPlatformScheduler` - WorkManager
- `SupabaseStorageClient` - HTTP client

### **iOS:**
- `IOSConnectivityMonitor` - Network framework
- `IOSPlatformScheduler` - Background Tasks
- `SupabaseStorageClient` - URLSession

## 🔄 **Przepływ inicjalizacji:**

```
1. App starts
2. Platform initializer called
3. Koin starts with commonModule + platformModule
4. Dependencies resolved automatically
5. SyncManager started
6. App ready
```

## 🎉 **Gotowe do użycia!**

System DI jest **w pełni zaimplementowany** zgodnie z najlepszymi praktykami Koin Multiplatform. Wszystkie komponenty są automatycznie wstrzykiwane i gotowe do użycia! 🚀

## 📚 **Dodatkowe zasoby:**

- [Koin Multiplatform Documentation](https://insert-koin.io/docs/reference/koin-multiplatform/)
- [Koin Android](https://insert-koin.io/docs/reference/koin-android/)
- [Dependency Injection Best Practices](https://insert-koin.io/docs/reference/koin-core/dependency-injection/)
