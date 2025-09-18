package pl.soulsnaps.sync.queue

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.soulsnaps.database.SoulSnapDatabase
import pl.soulsnaps.sync.model.SyncTask
import pl.soulsnaps.sync.model.SyncTaskState
import pl.soulsnaps.sync.model.CreateMemory
import pl.soulsnaps.sync.model.UpdateMemory
import pl.soulsnaps.sync.model.ToggleFavorite
import pl.soulsnaps.sync.model.DeleteMemory
import pl.soulsnaps.sync.model.PullAll
import pl.soulsnaps.utils.getCurrentTimeMillis
import kotlin.math.min
import kotlin.math.pow

/**
 * SyncQueue implementation using SQLDelight
 */
class SyncQueue(
    private val database: SoulSnapDatabase,
    private val config: pl.soulsnaps.sync.model.SyncConfig
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Enqueue a sync task (idempotent)
     */
    suspend fun enqueue(task: SyncTask) {
        val currentTime = getCurrentTimeMillis()
        val payload = json.encodeToString(task)
        
        database.soulSnapDatabaseQueries.insertSyncTask(
            id = task.id,
            type = task::class.simpleName ?: "Unknown",
            payload = payload,
            state = SyncTaskState.PENDING.name,
            attempt_count = 0L,
            next_run_at = currentTime,
            created_at = currentTime,
            updated_at = currentTime,
            local_id = task.localId
        )
        
        println("DEBUG: SyncQueue.enqueue() - enqueued task: ${task.id}")
    }
    
    /**
     * Get due tasks for processing
     */
    suspend fun getDueTasks(limit: Int = config.maxParallelTasks): List<SyncTaskEntity> {
        val currentTime = getCurrentTimeMillis()
        val tasks = database.soulSnapDatabaseQueries.selectDueTasks(
            next_run_at = currentTime,
            value_ = limit.toLong()
        ).executeAsList()
        
        return tasks.map { it.toSyncTaskEntity() }
    }
    
    /**
     * Get running tasks
     */
    suspend fun getRunningTasks(): List<SyncTaskEntity> {
        return database.soulSnapDatabaseQueries.selectRunningTasks()
            .executeAsList()
            .map { it.toSyncTaskEntity() }
    }
    
    /**
     * Get tasks for specific local ID
     */
    suspend fun getTasksForLocalId(localId: Long): List<SyncTaskEntity> {
        return database.soulSnapDatabaseQueries.selectTasksByLocalId(localId)
            .executeAsList()
            .map { it.toSyncTaskEntity() }
    }
    
    /**
     * Mark task as running
     */
    suspend fun markRunning(taskId: String) {
        val currentTime = getCurrentTimeMillis()
        database.soulSnapDatabaseQueries.markTaskRunning(
            updated_at = currentTime,
            id = taskId
        )
        println("DEBUG: SyncQueue.markRunning() - marked task as running: $taskId")
    }
    
    /**
     * Mark task as completed
     */
    suspend fun markCompleted(taskId: String) {
        val currentTime = getCurrentTimeMillis()
        database.soulSnapDatabaseQueries.markTaskCompleted(
            updated_at = currentTime,
            id = taskId
        )
        println("DEBUG: SyncQueue.markCompleted() - marked task as completed: $taskId")
    }
    
    /**
     * Mark task as failed and reschedule with backoff
     */
    suspend fun markFailed(taskId: String, attemptCount: Int) {
        val currentTime = getCurrentTimeMillis()
        val nextRunAt = calculateNextRunAt(attemptCount, currentTime)
        
        database.soulSnapDatabaseQueries.markTaskFailed(
            next_run_at = nextRunAt,
            updated_at = currentTime,
            id = taskId
        )
        
        println("DEBUG: SyncQueue.markFailed() - marked task as failed: $taskId, next run at: $nextRunAt")
    }
    
    /**
     * Cancel all tasks for specific local ID
     */
    suspend fun cancelTasksForLocalId(localId: Long) {
        val currentTime = getCurrentTimeMillis()
        database.soulSnapDatabaseQueries.cancelTasksForLocalId(
            updated_at = currentTime,
            local_id = localId
        )
        println("DEBUG: SyncQueue.cancelTasksForLocalId() - cancelled tasks for local ID: $localId")
    }
    
    /**
     * Get pending task count
     */
    suspend fun getPendingCount(): Int {
        return database.soulSnapDatabaseQueries.countPendingTasks().executeAsOne().toInt()
    }
    
    /**
     * Get running task count
     */
    suspend fun getRunningCount(): Int {
        return database.soulSnapDatabaseQueries.countRunningTasks().executeAsOne().toInt()
    }
    
    /**
     * Clean up completed tasks older than specified time
     */
    suspend fun cleanupCompletedTasks(olderThanMs: Long) {
        val cutoffTime = getCurrentTimeMillis() - olderThanMs
        val deletedCount = database.soulSnapDatabaseQueries.deleteCompletedTasks(cutoffTime)
        println("DEBUG: SyncQueue.cleanupCompletedTasks() - deleted $deletedCount completed tasks")
    }
    
    /**
     * Calculate next run time with exponential backoff + jitter
     */
    private fun calculateNextRunAt(attemptCount: Int, currentTime: Long): Long {
        val baseDelay = config.backoffBaseMs
        val maxDelay = config.backoffMaxMs
        
        // Exponential backoff: 15s, 30s, 1m, 2m, 4m, 8m, 16m, 32m, 1h
        val exponentialDelay = (baseDelay * (2.0.pow(attemptCount))).toLong()
        val delay = min(exponentialDelay, maxDelay)
        
        // Add jitter (±25% of delay)
        val jitter = (delay * 0.25 * (Math.random() - 0.5)).toLong()
        
        return currentTime + delay + jitter
    }
}

/**
 * Sync task entity from database
 */
data class SyncTaskEntity(
    val id: String,
    val type: String,
    val payload: String,
    val state: SyncTaskState,
    val attemptCount: Int,
    val nextRunAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val localId: Long
) {
    /**
     * Parse payload to SyncTask
     */
    fun toSyncTask(): SyncTask? {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            when (type) {
                "CreateMemory" -> json.decodeFromString<CreateMemory>(payload)
                "UpdateMemory" -> json.decodeFromString<UpdateMemory>(payload)
                "ToggleFavorite" -> json.decodeFromString<ToggleFavorite>(payload)
                "DeleteMemory" -> json.decodeFromString<DeleteMemory>(payload)
                "PullAll" -> json.decodeFromString<PullAll>(payload)
                else -> null
            }
        } catch (e: Exception) {
            println("ERROR: SyncTaskEntity.toSyncTask() - failed to parse task: $e")
            null
        }
    }
}

/**
 * Extension to convert database row to entity
 */
private fun pl.soulsnaps.database.Sync_queue.toSyncTaskEntity(): SyncTaskEntity {
    return SyncTaskEntity(
        id = id,
        type = type,
        payload = payload,
        state = runCatching { SyncTaskState.valueOf(state) }.getOrElse { SyncTaskState.PENDING },
        attemptCount = attempt_count.toInt(),
        nextRunAt = next_run_at,
        createdAt = created_at,
        updatedAt = updated_at,
        localId = local_id
    )
}
