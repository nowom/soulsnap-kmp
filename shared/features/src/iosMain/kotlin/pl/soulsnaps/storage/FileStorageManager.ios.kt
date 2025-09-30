package pl.soulsnaps.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import kotlin.random.Random

/**
 * iOS implementation of FileStorageManager using app's Documents directory
 */
class LocalFileStorageManager : FileStorageManager {
    
    private val fileManager = NSFileManager.defaultManager
    private val documentsDir = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).firstOrNull()
    private val appStorageDir = documentsDir?.URLByAppendingPathComponent("soulsnaps_media")
    private val photosDir = appStorageDir?.URLByAppendingPathComponent("photos")
    private val audioDir = appStorageDir?.URLByAppendingPathComponent("audio")
    
    init {
        // Ensure directories exist
        photosDir?.let { fileManager.createDirectoryAtURL(it, true, null, null) }
        audioDir?.let { fileManager.createDirectoryAtURL(it, true, null, null) }
        println("DEBUG: FileStorageManager - photosDir: ${photosDir?.path}")
        println("DEBUG: FileStorageManager - audioDir: ${audioDir?.path}")
    }
    
    override suspend fun savePhoto(photoData: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "photo_${Random.nextLong()}.jpg"
        val fileURL = photosDir.URLByAppendingPathComponent(fileName)
        
        try {
            photoData.usePinned { pinned ->
                fileManager.createFileAtPath(
                    fileURL?.path ?: throw RuntimeException("Invalid file path"),
                    pinned.addressOf(0),
                    null
                )
            }
            fileName
        } catch (e: Exception) {
            throw RuntimeException("Failed to save photo: ${e.message}", e)
        }
    }
    
    override suspend fun saveAudio(audioData: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "audio_${Random.nextLong()}.m4a"
        val fileURL = audioDir.URLByAppendingPathComponent(fileName)
        
        try {
            audioData.usePinned { pinned ->
                fileManager.createFileAtPath(
                    fileURL?.path ?: throw RuntimeException("Invalid file path"),
                    pinned.addressOf(0),
                    null
                )
            }
            fileName
        } catch (e: Exception) {
            throw RuntimeException("Failed to save audio: ${e.message}", e)
        }
    }
    
    override suspend fun getPhotoPath(fileName: String): String? = withContext(Dispatchers.IO) {
        val fileURL = photosDir.URLByAppendingPathComponent(fileName)
        if (fileManager.fileExistsAtPath(fileURL?.path ?: "")) {
            fileURL?.path
        } else {
            null
        }
    }
    
    override suspend fun getAudioPath(fileName: String): String? = withContext(Dispatchers.IO) {
        val fileURL = audioDir.URLByAppendingPathComponent(fileName)
        if (fileManager.fileExistsAtPath(fileURL?.path ?: "")) {
            fileURL?.path
        } else {
            null
        }
    }
    
    override suspend fun deletePhoto(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val fileURL = photosDir.URLByAppendingPathComponent(fileName)
        try {
            fileManager.removeItemAtURL(fileURL, null)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun deleteAudio(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val fileURL = audioDir.URLByAppendingPathComponent(fileName)
        try {
            fileManager.removeItemAtURL(fileURL, null)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun cleanupOrphanedFiles(): Int = withContext(Dispatchers.IO) {
        // This would be implemented to clean up files not referenced by any memory
        // For now, return 0 as we don't have the logic to determine orphaned files
        0
    }
}

actual object FileStorageManagerFactory {
    actual fun create(): FileStorageManager {
        return LocalFileStorageManager()
    }
}
