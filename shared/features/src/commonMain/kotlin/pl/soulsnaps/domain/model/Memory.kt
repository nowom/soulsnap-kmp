package pl.soulsnaps.domain.model

data class Memory(
    val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val mood: MoodType?,
    val photoUri: String?, // Local file path
    val audioUri: String?, // Local file path
    val imageUrl: String? = null, // Deprecated, use remotePhotoPath
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val affirmation: String? = null,
    val isFavorite: Boolean = false,
    val isSynced: Boolean = false,
    val remotePhotoPath: String? = null, // Supabase Storage path
    val remoteAudioPath: String? = null, // Supabase Storage path
    val remoteId: String? = null, // Remote database ID
    val syncState: SyncState = SyncState.PENDING,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Sync states for memory synchronization
 */
enum class SyncState {
    PENDING,    // Waiting to be synced
    SYNCING,    // Currently being synced
    SYNCED,     // Successfully synced
    FAILED      // Sync failed, will retry
}