package pl.soulsnaps.features.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.features.auth.SessionRefreshService
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.sync.manager.SyncManager
import pl.soulsnaps.domain.MemoryRepository
import kotlinx.atomicfu.atomic // Dodano import dla AtomicBoolean
import kotlinx.coroutines.IO

/**
 * Centralized app initialization service.
 * Manages startup sequence for all background services.
 */
class AppInitializer(
    private val syncManager: SyncManager,
    private val sessionRefreshService: SessionRefreshService,
    private val memoryRepository: MemoryRepository,
    private val userSessionManager: UserSessionManager,
    private val crashlyticsManager: CrashlyticsManager
) {

    companion object {
        private const val TAG = "AppInitializer"
        private const val INITIALIZATION_DELAY_MS = 2000L // 2 seconds
    }

    // Zmieniono Dispatchers.Default na Dispatchers.IO, aby obsłużyć operacje I/O (sieć/baza danych)
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Zmieniono na AtomicBoolean dla bezpiecznego, wielowątkowego zarządzania flagą stanu
    private val isInitialized = atomic(false)

    /**
     * Initialize all app services.
     * Should be called from SoulSnapsApp.
     */
    fun initialize() {
        // Użycie compareAndSet (CAS) gwarantuje, że tylko jeden wątek wykona inicjalizację.
        // Jeśli aktualna wartość to 'false', ustawia na 'true' i zwraca 'true'.
        val wasNotInitialized = isInitialized.compareAndSet(expect = false, update = true)

        if (!wasNotInitialized) {
            println("⚠️ $TAG - already initialized, skipping")
            return
        }

        println("========================================")
        println("🚀 $TAG - STARTING APP INITIALIZATION")
        println("========================================")

        initScope.launch {
            try {
                // Krok 1: Validate and refresh user session
                println("👤 Step 1: Validating user session...")
                userSessionManager.validateAndRefreshSession()
                println("✅ User session validated")

                // Krok 2: Start session refresh service
                println("📱 Step 2: Starting session refresh service...")
                sessionRefreshService.start()
                println("✅ Session refresh service started")

                // Krok 3: Start sync manager
                println("🔄 Step 3: Starting sync manager...")
                syncManager.start()
                println("✅ Sync manager started")

                // Krok 4: Wait for app to fully initialize
                // Pamiętaj, aby rozważyć usunięcie tego delayu, jeśli ma on wymuszać gotowość,
                // i zastąpienie go bardziej asynchronicznym mechanizmem oczekiwania.
                println("⏳ Step 4: Waiting for app initialization...")
                delay(INITIALIZATION_DELAY_MS)

                // Krok 5: Auto-sync existing unsynced memories
                println("📤 Step 5: Auto-syncing pending memories...")
                memoryRepository.enqueuePendingMemories()
                println("✅ Pending memories enqueued")

                // isInitialized jest już ustawione na 'true' w CAS, więc nie trzeba go tu ustawiać.

                println("========================================")
                println("✅ $TAG - APP INITIALIZATION COMPLETED")
                println("========================================")

            } catch (e: Exception) {
                // W przypadku błędu, rejestrujemy go i wypisujemy stack trace
                println("========================================")
                println("❌ $TAG - INITIALIZATION FAILED: ${e.message}")
                println("========================================")

                // Ważne: Jeśli inicjalizacja się nie powiedzie, musimy zresetować flagę,
                // aby umożliwić ponowną próbę.
                isInitialized.value = false

                crashlyticsManager.recordException(e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Get initialization status
     */
    fun isInitialized(): Boolean = isInitialized.value

    /**
     * Force re-initialization (for testing)
     */
    fun reset() {
        isInitialized.value = false
        println("🔄 $TAG - Reset initialization status")
    }
}
