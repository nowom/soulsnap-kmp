# Updated Koin Multiplatform Pattern

## 🎯 **Cel:**
Zaktualizowanie systemu DI do używania `expect/actual` pattern zgodnie z istniejącym kodem w projekcie.

## 📋 **Nowa architektura DI:**

### **1. CommonModule (commonMain):**
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
}

/**
 * Platform-specific module declaration
 * Will be provided by platform-specific modules
 */
expect val platformModule: Module
```

### **2. AndroidModule (androidMain):**
```kotlin
actual val platformModule: Module = module {
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

### **3. IOSModule (iosMain):**
```kotlin
actual val platformModule: Module = module {
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

## 🚀 **Inicjalizacja:**

### **KoinInitializer (commonMain):**
```kotlin
object KoinInitializer {
    fun initialize() {
        startKoin {
            modules(
                commonModule,
                platformModule
            )
        }
    }
}
```

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

## ✅ **Zalety nowego patternu:**

### **1. Zgodność z istniejącym kodem:**
- Używa `expect/actual` pattern jak reszta projektu
- `platformModule` jest deklarowany w commonMain
- Platform-specific implementacje w androidMain/iosMain

### **2. Prostsza inicjalizacja:**
- Jeden `KoinInitializer.initialize()` dla wszystkich platform
- Automatyczne rozwiązywanie `platformModule`
- Mniej boilerplate code

### **3. Type-safe:**
- Compile-time checking dla platform modules
- Automatyczne wstrzykiwanie dependencies
- Lazy initialization

### **4. Skalowalność:**
- Łatwe dodawanie nowych platform-specific dependencies
- Centralne zarządzanie konfiguracją
- Modularna architektura

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

## 🎉 **Gotowe do użycia!**

System DI jest **w pełni zaktualizowany** i zgodny z istniejącym patternem w projekcie. Wszystkie komponenty są automatycznie wstrzykiwane i gotowe do użycia! 🚀

## 📚 **Porównanie z poprzednim podejściem:**

| Aspekt | Poprzednie | Nowe |
|--------|------------|------|
| **Pattern** | `fun androidModule(): Module` | `actual val platformModule: Module` |
| **Inicjalizacja** | `initialize(androidModule())` | `initialize()` |
| **Zgodność** | Osobne funkcje | `expect/actual` pattern |
| **Boilerplate** | Więcej kodu | Mniej kodu |
| **Type Safety** | Runtime | Compile-time |

Nowe podejście jest **bardziej eleganckie** i **zgodne z najlepszymi praktykami** Koin Multiplatform! ✨
