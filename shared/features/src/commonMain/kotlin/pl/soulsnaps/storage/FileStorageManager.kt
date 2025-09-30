package pl.soulsnaps.storage

/**
 * Manages file storage for large content (photos, audio) instead of storing in database
 * This prevents SQLite CursorWindow errors and improves performance
 */
interface FileStorageManager {
    suspend fun savePhoto(photoData: ByteArray): String
    suspend fun saveAudio(audioData: ByteArray): String
    suspend fun getPhotoPath(fileName: String): String?
    suspend fun getAudioPath(fileName: String): String?
    suspend fun deletePhoto(fileName: String): Boolean
    suspend fun deleteAudio(fileName: String): Boolean
    suspend fun cleanupOrphanedFiles(): Int
}

/**
 * Factory for creating FileStorageManager instances
 */
expect object FileStorageManagerFactory {
    fun create(): FileStorageManager
}
