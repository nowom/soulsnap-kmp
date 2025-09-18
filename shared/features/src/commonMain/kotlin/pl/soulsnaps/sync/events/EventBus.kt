package pl.soulsnaps.sync.events

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Domain events for sync system
 */
sealed class AppEvent {
    data class SnapSynced(val localId: Long) : AppEvent()
    data class SnapSyncFailed(val localId: Long, val error: String) : AppEvent()
    data class SnapDeleted(val localId: Long) : AppEvent()
    data class SyncStarted(val taskCount: Int) : AppEvent()
    data class SyncCompleted(val successCount: Int, val failureCount: Int) : AppEvent()
    data class SyncFailed(val error: String) : AppEvent()
    data class ConnectivityChanged(val connected: Boolean) : AppEvent()
}

/**
 * Event bus for domain events
 */
interface EventBus {
    fun emit(event: AppEvent)
    fun observe(): Flow<AppEvent>
}

/**
 * Simple event bus implementation
 */
class SimpleEventBus : EventBus {
    
    private val _events = MutableSharedFlow<AppEvent>(replay = 0)
    
    override fun emit(event: AppEvent) {
        println("DEBUG: EventBus.emit() - $event")
        _events.tryEmit(event)
    }
    
    override fun observe(): Flow<AppEvent> = _events.asSharedFlow()
}

/**
 * Singleton event bus instance
 */
object GlobalEventBus : EventBus by SimpleEventBus()
