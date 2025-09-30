package pl.soulsnaps.sync.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Offline sync processor that handles pending operations
 */
class OfflineSyncProcessor(
    private val syncQueue: OfflineSyncQueue,
    private val onlineDataSource: OnlineDataSource,
    private val connectivityMonitor: ConnectivityMonitor,
    private val crashlyticsManager: CrashlyticsManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    
    private var isRunning = false
    
    /**
     * Start processing offline sync queue
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        
        coroutineScope.launch {
            // Monitor connectivity changes
            connectivityMonitor.connected.collectLatest { isConnected ->
                if (isConnected) {
                    crashlyticsManager.log("OfflineSyncProcessor: Network connected, starting sync")
                    processPendingOperations()
                } else {
                    crashlyticsManager.log("OfflineSyncProcessor: Network disconnected, pausing sync")
                }
            }
        }
        
        // Also process immediately if connected
        coroutineScope.launch {
            if (connectivityMonitor.connected.value) {
                processPendingOperations()
            }
        }
    }
    
    /**
     * Stop processing
     */
    fun stop() {
        isRunning = false
    }
    
    /**
     * Process all pending operations
     */
    suspend fun processPendingOperations() {
        if (!connectivityMonitor.connected.value) {
            crashlyticsManager.log("OfflineSyncProcessor: Skipping sync - no network connection")
            return
        }
        
        crashlyticsManager.log("OfflineSyncProcessor: Starting to process ${syncQueue.getOperationsCount()} operations")
        
        while (syncQueue.getOperationsCount() > 0 && connectivityMonitor.connected.value) {
            val operation = syncQueue.getNextOperation()
            if (operation == null) break
            
            try {
                val success = processOperation(operation)
                if (success) {
                    syncQueue.markCompleted(operation.id)
                    crashlyticsManager.log("OfflineSyncProcessor: Successfully processed operation ${operation.id}")
                } else {
                    syncQueue.markFailed(operation.id, incrementRetry = true)
                    crashlyticsManager.log("OfflineSyncProcessor: Failed to process operation ${operation.id}, retry count: ${operation.retryCount}")
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e)
                syncQueue.markFailed(operation.id, incrementRetry = true)
                crashlyticsManager.log("OfflineSyncProcessor: Exception processing operation ${operation.id}: ${e.message}")
            }
        }
        
        crashlyticsManager.log("OfflineSyncProcessor: Finished processing operations")
    }
    
    /**
     * Process a single operation
     */
    private suspend fun processOperation(operation: SyncOperation): Boolean {
        return try {
            val memory = Json.decodeFromString<Memory>(operation.data)
            
            when (operation.type) {
                SyncOperationType.INSERT -> {
                    val result = onlineDataSource.insertMemory(memory, operation.userId)
                    result != null
                }
                SyncOperationType.UPDATE -> {
                    onlineDataSource.updateMemory(memory, operation.userId)
                }
                SyncOperationType.DELETE -> {
                    onlineDataSource.deleteMemory(operation.memoryId, operation.userId)
                }
                SyncOperationType.FAVORITE -> {
                    // Extract favorite status from memory
                    onlineDataSource.markAsFavorite(operation.memoryId, memory.isFavorite ?: false, operation.userId)
                }
            }
        } catch (e: Exception) {
            crashlyticsManager.log("OfflineSyncProcessor: Failed to process operation ${operation.id}: ${e.message}")
            false
        }
    }
    
    /**
     * Force retry failed operations
     */
    suspend fun retryFailedOperations() {
        crashlyticsManager.log("OfflineSyncProcessor: Retrying ${syncQueue.getFailedOperationsCount()} failed operations")
        syncQueue.retryFailedOperations()
        processPendingOperations()
    }
    
    /**
     * Get sync status
     */
    fun getSyncStatus(): SyncStatus {
        return SyncStatus(
            pendingOperations = syncQueue.getOperationsCount(),
            failedOperations = syncQueue.getFailedOperationsCount(),
            isConnected = connectivityMonitor.connected.value,
            isProcessing = syncQueue.isProcessing.value
        )
    }
}

/**
 * Sync status information
 */
data class SyncStatus(
    val pendingOperations: Int,
    val failedOperations: Int,
    val isConnected: Boolean,
    val isProcessing: Boolean
) {
    val hasPendingWork: Boolean get() = pendingOperations > 0
    val hasFailedWork: Boolean get() = failedOperations > 0
    val canSync: Boolean get() = isConnected && !isProcessing
}
