package pl.soulsnaps.sync.offline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Offline sync queue for managing pending operations
 */
@Serializable
data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val memoryId: Long,
    val userId: String,
    val data: String, // Serialized memory data
    val timestamp: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val priority: SyncPriority = SyncPriority.NORMAL
)

@Serializable
enum class SyncOperationType {
    INSERT, UPDATE, DELETE, FAVORITE
}

@Serializable
enum class SyncPriority {
    HIGH, NORMAL, LOW
}

/**
 * Offline sync queue implementation
 */
class OfflineSyncQueue {
    
    private val _pendingOperations = MutableStateFlow<List<SyncOperation>>(emptyList())
    val pendingOperations: StateFlow<List<SyncOperation>> = _pendingOperations.asStateFlow()
    
    private val _failedOperations = MutableStateFlow<List<SyncOperation>>(emptyList())
    val failedOperations: StateFlow<List<SyncOperation>> = _failedOperations.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    /**
     * Add operation to sync queue
     */
    suspend fun addOperation(
        type: SyncOperationType,
        memory: Memory,
        userId: String,
        priority: SyncPriority = SyncPriority.NORMAL
    ) {
        val operation = SyncOperation(
            id = "${type.name}_${memory.id}_${getCurrentTimeMillis()}",
            type = type,
            memoryId = memory.id.toLong(),
            userId = userId,
            data = kotlinx.serialization.json.Json.encodeToString(memory),
            timestamp = getCurrentTimeMillis(),
            priority = priority
        )
        
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.add(operation)
        
        // Sort by priority and timestamp
        currentOperations.sortWith(compareBy<SyncOperation> { it.priority.ordinal }.thenBy { it.timestamp })
        
        _pendingOperations.value = currentOperations
    }
    
    /**
     * Get next operation to process
     */
    fun getNextOperation(): SyncOperation? {
        return _pendingOperations.value.firstOrNull()
    }
    
    /**
     * Mark operation as completed
     */
    suspend fun markCompleted(operationId: String) {
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.removeAll { it.id == operationId }
        _pendingOperations.value = currentOperations
    }
    
    /**
     * Mark operation as failed
     */
    suspend fun markFailed(operationId: String, incrementRetry: Boolean = true) {
        val currentOperations = _pendingOperations.value.toMutableList()
        val operation = currentOperations.find { it.id == operationId }
        
        if (operation != null) {
            currentOperations.remove(operation)
            
            if (incrementRetry && operation.retryCount < operation.maxRetries) {
                val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                currentOperations.add(updatedOperation)
                _pendingOperations.value = currentOperations
            } else {
                // Move to failed operations
                val failedOps = _failedOperations.value.toMutableList()
                failedOps.add(operation)
                _failedOperations.value = failedOps
            }
        }
    }
    
    /**
     * Retry failed operations
     */
    suspend fun retryFailedOperations() {
        val failedOps = _failedOperations.value.toMutableList()
        val currentOps = _pendingOperations.value.toMutableList()
        
        // Move failed operations back to pending with reset retry count
        failedOps.forEach { operation ->
            val retryOperation = operation.copy(retryCount = 0)
            currentOps.add(retryOperation)
        }
        
        _failedOperations.value = emptyList()
        _pendingOperations.value = currentOps
    }
    
    /**
     * Clear all operations
     */
    suspend fun clearAll() {
        _pendingOperations.value = emptyList()
        _failedOperations.value = emptyList()
    }
    
    /**
     * Get operations count
     */
    fun getOperationsCount(): Int = _pendingOperations.value.size
    
    /**
     * Get failed operations count
     */
    fun getFailedOperationsCount(): Int = _failedOperations.value.size
    
    /**
     * Check if queue is empty
     */
    fun isEmpty(): Boolean = _pendingOperations.value.isEmpty()
    
    /**
     * Check if queue has failed operations
     */
    fun hasFailedOperations(): Boolean = _failedOperations.value.isNotEmpty()
    
    /**
     * Get operations by type
     */
    fun getOperationsByType(type: SyncOperationType): List<SyncOperation> {
        return _pendingOperations.value.filter { it.type == type }
    }
    
    /**
     * Get operations by priority
     */
    fun getOperationsByPriority(priority: SyncPriority): List<SyncOperation> {
        return _pendingOperations.value.filter { it.priority == priority }
    }
}
