package pl.soulsnaps.sync.model

import kotlinx.serialization.Serializable

/**
 * Universal sync task types
 */
@Serializable
sealed class SyncTask {
    abstract val id: String
    abstract val localId: Long
}

@Serializable
data class CreateMemory(
    override val localId: Long,
    val plannedRemotePhotoPath: String,
    val plannedRemoteAudioPath: String? = null,
    override val id: String = "CREATE:$localId"
) : SyncTask()

@Serializable
data class UpdateMemory(
    override val localId: Long,
    val reuploadPhoto: Boolean = false,
    val reuploadAudio: Boolean = false,
    override val id: String = "UPDATE:$localId"
) : SyncTask()

@Serializable
data class ToggleFavorite(
    override val localId: Long,
    val isFavorite: Boolean,
    override val id: String = "FAV:$localId:$isFavorite"
) : SyncTask()

@Serializable
data class DeleteMemory(
    override val localId: Long,
    val remotePhotoPath: String?,
    val remoteAudioPath: String?,
    override val id: String = "DELETE:$localId"
) : SyncTask()

@Serializable
data class PullAll(
    override val id: String = "PULL_ALL",
    override val localId: Long = -1
) : SyncTask()

/**
 * Sync status for UI
 */
data class SyncStatus(
    val running: Boolean,
    val pendingCount: Int,
    val lastError: String? = null,
    val lastSyncTime: Long? = null
)

/**
 * Sync task state
 */
enum class SyncTaskState {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Sync configuration
 */
data class SyncConfig(
    val maxParallelTasks: Int = 3,
    val backoffBaseMs: Long = 15000, // 15 seconds
    val backoffMaxMs: Long = 3600000, // 1 hour
    val uploadCompression: Boolean = true,
    val pullOnStartup: Boolean = true,
    val retryOnMetered: Boolean = false
)

/**
 * Storage paths (deterministic)
 */
object StoragePaths {
    fun photoPath(userId: String, localId: Long): String {
        return "soulsnaps/$userId/$localId/photo.jpg"
    }
    
    fun audioPath(userId: String, localId: Long): String {
        return "soulsnaps/$userId/$localId/audio.m4a"
    }
}

/**
 * Sync metrics for observability
 */
data class SyncMetrics(
    val pendingTasks: Int = 0,
    val runningTasks: Int = 0,
    val completedTasks: Int = 0,
    val failedTasks: Int = 0,
    val lastSyncDurationMs: Long? = null,
    val uploadBytesTotal: Long = 0,
    val backoffLevel: Int = 0
)