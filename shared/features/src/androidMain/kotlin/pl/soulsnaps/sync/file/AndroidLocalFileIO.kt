package pl.soulsnaps.sync.file

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of LocalFileIO
 */
class AndroidLocalFileIO(
    private val context: Context
) : LocalFileIO {
    
    override suspend fun readBytes(localUri: String): ByteArray {
        return try {
            println("DEBUG: AndroidLocalFileIO.readBytes() - reading: $localUri")
            
            val uri = Uri.parse(localUri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open input stream for URI: $localUri")
            
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            println("DEBUG: AndroidLocalFileIO.readBytes() - read ${bytes.size} bytes")
            bytes
            
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.readBytes() - error: ${e.message}")
            throw e
        }
    }
    
    override suspend fun writeBytes(localUri: String, data: ByteArray): Boolean {
        return try {
            println("DEBUG: AndroidLocalFileIO.writeBytes() - writing ${data.size} bytes to: $localUri")
            
            val file = File(localUri)
            file.parentFile?.mkdirs()
            
            val outputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.close()
            
            println("DEBUG: AndroidLocalFileIO.writeBytes() - write successful")
            true
            
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.writeBytes() - error: ${e.message}")
            false
        }
    }
    
    override suspend fun exists(localUri: String): Boolean {
        return try {
            val uri = Uri.parse(localUri)
            if (uri.scheme == "content") {
                // Content URI - check via ContentResolver
                val inputStream = context.contentResolver.openInputStream(uri)
                val exists = inputStream != null
                inputStream?.close()
                exists
            } else {
                // File path
                File(localUri).exists()
            }
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.exists() - error: ${e.message}")
            false
        }
    }
    
    override suspend fun getFileSize(localUri: String): Long {
        return try {
            val uri = Uri.parse(localUri)
            if (uri.scheme == "content") {
                // Content URI - get size via ContentResolver
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    afd.length
                } ?: -1L
            } else {
                // File path
                val file = File(localUri)
                if (file.exists()) file.length() else -1L
            }
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.getFileSize() - error: ${e.message}")
            -1L
        }
    }
    
    override suspend fun delete(localUri: String): Boolean {
        return try {
            val uri = Uri.parse(localUri)
            if (uri.scheme == "content") {
                // Content URI - cannot delete
                println("WARN: AndroidLocalFileIO.delete() - cannot delete content URI: $localUri")
                false
            } else {
                // File path
                val file = File(localUri)
                val deleted = file.delete()
                println("DEBUG: AndroidLocalFileIO.delete() - deleted: $deleted")
                deleted
            }
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.delete() - error: ${e.message}")
            false
        }
    }
    
    override fun getFileExtension(localUri: String): String? {
        return try {
            val uri = Uri.parse(localUri)
            if (uri.scheme == "content") {
                // Content URI - get from MIME type
                val mimeType = context.contentResolver.getType(uri)
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } else {
                // File path
                val file = File(localUri)
                file.extension.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.getFileExtension() - error: ${e.message}")
            null
        }
    }
    
    override suspend fun getMimeType(localUri: String): String? {
        return try {
            val uri = Uri.parse(localUri)
            if (uri.scheme == "content") {
                // Content URI - get from ContentResolver
                context.contentResolver.getType(uri)
            } else {
                // File path - guess from extension
                val extension = getFileExtension(localUri)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        } catch (e: Exception) {
            println("ERROR: AndroidLocalFileIO.getMimeType() - error: ${e.message}")
            null
        }
    }
}
