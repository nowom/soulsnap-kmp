package pl.soulsnaps.sync.model

import kotlinx.serialization.Serializable

/**
 * Universal sync task types
 */
sealed interface SyncTask {
    val id: String
    val localId: Long
}

@Serializable
data class CreateMemory(
    override val localId: Long,
    val plannedRemotePhotoPath: String,
    val plannedRemoteAudioPath: String? = null
) : SyncTask {
    override val id = "CREATE:$localId"
}

@Serializable
data class UpdateMemory(
    override val localId: Long,
    val reuploadPhoto: Boolean = false,
    val reuploadAudio: Boolean = false
) : SyncTask {
    override val id = "UPDATE:$localId"
}

@Serializable
data class ToggleFavorite(
    override val localId: Long,
    val isFavorite: Boolean
) : SyncTask {
    override val id = "FAV:$localId:$isFavorite"
}

@Serializable
data class DeleteMemory(
    override val localId: Long,
    val remotePhotoPath: String?,
    val remoteAudioPath: String?
) : SyncTask {
    override val id = "DELETE:$localId"
}

@Serializable
data object PullAll : SyncTask {
    override val id = "PULL_ALL"
    override val localId: Long = -1
}

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