package pl.soulsnaps.features.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.network.SupabaseAuthService
import kotlin.coroutines.coroutineContext

/**
 * Background service that automatically refreshes Supabase session
 * to keep user logged in indefinitely
 */
class SessionRefreshService(
    private val userSessionManager: UserSessionManager,
    private val supabaseAuthService: SupabaseAuthService,
    private val sessionDataStore: SessionDataStore,
    private val crashlyticsManager: CrashlyticsManager
) {

    companion object {
        private const val REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
        private const val TAG = "SessionRefreshService"
    }

    // Używamy SupervisorJob, aby błędy w pod-zadaniach nie anulowały całego scope'u
    private var serviceScope: CoroutineScope? = null

    /**
     * Start the session refresh service.
     * Starts a new scope if one isn't running.
     */
    fun start() {
        if (serviceScope != null && serviceScope!!.isActive) {
            println("⚠️ $TAG - Service is already running.")
            return
        }

        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        println("========================================")
        println("🔄 $TAG - STARTING AUTO-REFRESH SERVICE")
        println("========================================")
        println("⏰ Refresh interval: ${REFRESH_INTERVAL_MS / 1000 / 60} minutes")
        println("========================================")

        serviceScope?.launch {
            userSessionManager.sessionState.collectLatest { state ->
                when (state) {
                    is SessionState.Authenticated -> {
                        println("✅ $TAG - User authenticated, starting refresh loop")
                        startRefreshLoop()
                    }
                    else -> {
                        println("ℹ️ $TAG - Session state is not authenticated, stopping loop.")
                    }
                }
            }
        }
    }

    /**
     * Stop the session refresh service by cancelling the scope.
     */
    fun stop() {
        serviceScope?.cancel()
        serviceScope = null
        println("========================================")
        println("🛑 $TAG - STOPPED")
        println("========================================")
    }

    private suspend fun startRefreshLoop() {
        // Nowa, wydajniejsza i bezpieczniejsza pętla
        while (coroutineContext.isActive && userSessionManager.isAuthenticated()) {
            delay(REFRESH_INTERVAL_MS)

            println("========================================")
            println("🔄 $TAG - AUTO-REFRESHING SESSION")
            println("========================================")

            try {
                val refreshedSession = supabaseAuthService.refreshSession()

                if (refreshedSession != null) {
                    println("✅ $TAG - Session refreshed successfully")
                    sessionDataStore.saveSession(refreshedSession)
                    userSessionManager.onUserAuthenticated(refreshedSession)

                    crashlyticsManager.log("Session auto-refreshed successfully")
                } else {
                    println("❌ $TAG - Session refresh failed")
                    crashlyticsManager.log("Session auto-refresh failed")
                    userSessionManager.validateAndRefreshSession()
                }
            } catch (e: Exception) {
                println("❌ ERROR: $TAG - refresh loop error: ${e.message}")
                crashlyticsManager.recordException(e)
                // Można dodać tutaj logikę ponownej próby (exponential backoff)
            }

            println("========================================")
        }
        println("ℹ️ $TAG - Refresh loop ended")
    }

    /**
     * Force immediate refresh
     */
    suspend fun refreshNow() {
        println("========================================")
        println("🔄 $TAG - FORCE REFRESH NOW")
        println("========================================")

        try {
            val refreshedSession = supabaseAuthService.refreshSession()

            if (refreshedSession != null) {
                println("✅ $TAG - Force refresh successful")
                sessionDataStore.saveSession(refreshedSession)
                userSessionManager.onUserAuthenticated(refreshedSession)
            } else {
                println("❌ $TAG - Force refresh failed")
                userSessionManager.validateAndRefreshSession()
            }
        } catch (e: Exception) {
            println("❌ ERROR: $TAG - force refresh error: ${e.message}")
            crashlyticsManager.recordException(e)
        }

        println("========================================")
    }

    /**
     * Get service status
     */
    fun getStatus(): RefreshServiceStatus {
        return RefreshServiceStatus(
            isRunning = serviceScope?.isActive ?: false,
            isUserAuthenticated = userSessionManager.isAuthenticated(),
            nextRefreshIn = REFRESH_INTERVAL_MS
        )
    }
}

/**
 * Service status
 */
data class RefreshServiceStatus(
    val isRunning: Boolean,
    val isUserAuthenticated: Boolean,
    val nextRefreshIn: Long
)