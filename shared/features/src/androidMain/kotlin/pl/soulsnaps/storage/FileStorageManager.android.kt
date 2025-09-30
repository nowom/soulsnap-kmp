package pl.soulsnaps.storage

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Android implementation of FileStorageManager using app's private storage
 */
class LocalFileStorageManager(
    private val context: Context
) : FileStorageManager {
    
    // Use app's private storage directory
    private val appStorageDir = File(context.filesDir, "soulsnaps_media")
    private val photosDir = File(appStorageDir, "photos")
    private val audioDir = File(appStorageDir, "audio")
    
    init {
        // Ensure directories exist
        photosDir.mkdirs()
        audioDir.mkdirs()
        println("DEBUG: FileStorageManager - photosDir: ${photosDir.absolutePath}")
        println("DEBUG: FileStorageManager - audioDir: ${audioDir.absolutePath}")
    }
    
    override suspend fun savePhoto(photoData: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "photo_${UUID.randomUUID()}.jpg"
        val file = File(photosDir, fileName)
        
        try {
            FileOutputStream(file).use { fos ->
                fos.write(photoData)
            }
            fileName
        } catch (e: IOException) {
            throw RuntimeException("Failed to save photo: ${e.message}", e)
        }
    }
    
    override suspend fun saveAudio(audioData: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "audio_${UUID.randomUUID()}.m4a"
        val file = File(audioDir, fileName)
        
        try {
            FileOutputStream(file).use { fos ->
                fos.write(audioData)
            }
            fileName
        } catch (e: IOException) {
            throw RuntimeException("Failed to save audio: ${e.message}", e)
        }
    }
    
    override suspend fun getPhotoPath(fileName: String): String? = withContext(Dispatchers.IO) {
        val file = File(photosDir, fileName)
        if (file.exists()) file.absolutePath else null
    }
    
    override suspend fun getAudioPath(fileName: String): String? = withContext(Dispatchers.IO) {
        val file = File(audioDir, fileName)
        if (file.exists()) file.absolutePath else null
    }
    
    override suspend fun deletePhoto(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(photosDir, fileName)
        file.delete()
    }
    
    override suspend fun deleteAudio(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(audioDir, fileName)
        file.delete()
    }
    
    override suspend fun cleanupOrphanedFiles(): Int = withContext(Dispatchers.IO) {
        // This would be implemented to clean up files not referenced by any memory
        // For now, return 0 as we don't have the logic to determine orphaned files
        0
    }
}

actual object FileStorageManagerFactory {
    actual fun create(): FileStorageManager {
        // For Android, we need to get Context from DI
        // This will be injected via Koin
        throw NotImplementedError("Use createWithContext() instead")
    }
    
    fun createWithContext(context: Context): FileStorageManager {
        return LocalFileStorageManager(context)
    }
}
