package pl.soulsnaps.sync.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import pl.soulsnaps.sync.file.ImagePipeline
import pl.soulsnaps.sync.file.LocalFileIO

/**
 * Storage client interface for Supabase Storage
 */
interface StorageClient {
    suspend fun upload(
        bucket: String,
        key: String,
        filePath: String,
        upsert: Boolean = true
    ): StorageResult
    
    suspend fun uploadImage(
        bucket: String,
        key: String,
        localUri: String,
        maxLongEdgePx: Int = 1920,
        quality: Int = 85,
        upsert: Boolean = true
    ): StorageResult
    
    suspend fun uploadFile(
        bucket: String,
        key: String,
        localUri: String,
        upsert: Boolean = true
    ): StorageResult
    
    suspend fun delete(bucket: String, key: String): StorageResult
    
    suspend fun getSignedUrl(bucket: String, key: String): String?
}

/**
 * Storage operation result
 */
@Serializable
data class StorageResult(
    val success: Boolean,
    val path: String? = null,
    val errorMessage: String? = null
)

/**
 * Supabase Storage implementation
 */
class SupabaseStorageClient(
    private val supabaseClient: SupabaseClient,
    private val imagePipeline: ImagePipeline,
    private val localFileIO: LocalFileIO
) : StorageClient {
    
    private val storage = supabaseClient.storage
    
    override suspend fun upload(
        bucket: String,
        key: String,
        filePath: String,
        upsert: Boolean
    ): StorageResult {
        return try {
            println("DEBUG: SupabaseStorageClient.upload() - uploading to bucket: $bucket, key: $key")
            
            // Read file bytes
            val fileBytes = localFileIO.readBytes(filePath)
            
            // Upload to Supabase Storage
            storage.from(bucket).upload(
                path = key,
                data = fileBytes
            ) {
                if (upsert) {
                    this.upsert = true
                }
            }
            
            println("DEBUG: SupabaseStorageClient.upload() - upload successful: $key")
            StorageResult(success = true, path = key)
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.upload() - exception: ${e.message}")
            StorageResult(success = false, errorMessage = e.message ?: "Unknown error")
        }
    }
    
    override suspend fun delete(bucket: String, key: String): StorageResult {
        return try {
            println("DEBUG: SupabaseStorageClient.delete() - deleting from bucket: $bucket, key: $key")
            
            // Delete from Supabase Storage
            storage.from(bucket).delete(key)
            
            println("DEBUG: SupabaseStorageClient.delete() - delete successful: $key")
            StorageResult(success = true)
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.delete() - exception: ${e.message}")
            StorageResult(success = false, errorMessage = e.message ?: "Unknown error")
        }
    }
    
    override suspend fun uploadImage(
        bucket: String,
        key: String,
        localUri: String,
        maxLongEdgePx: Int,
        quality: Int,
        upsert: Boolean
    ): StorageResult {
        return try {
            println("DEBUG: SupabaseStorageClient.uploadImage() - processing image: $localUri")
            
            // Process image through pipeline
            val jpegBytes = imagePipeline.toJpegBytes(localUri, maxLongEdgePx, quality)
            
            // Upload to Supabase Storage
            storage.from(bucket).upload(
                path = key,
                data = jpegBytes
            ) {
                if (upsert) {
                    this.upsert = true
                }
            }
            
            println("DEBUG: SupabaseStorageClient.uploadImage() - upload successful: $key (${jpegBytes.size} bytes)")
            StorageResult(success = true, path = key)
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.uploadImage() - exception: ${e.message}")
            StorageResult(success = false, errorMessage = e.message ?: "Unknown error")
        }
    }
    
    override suspend fun uploadFile(
        bucket: String,
        key: String,
        localUri: String,
        upsert: Boolean
    ): StorageResult {
        return try {
            println("DEBUG: SupabaseStorageClient.uploadFile() - uploading file: $localUri")
            
            // Read file bytes
            val fileBytes = localFileIO.readBytes(localUri)
            
            // Upload to Supabase Storage
            storage.from(bucket).upload(
                path = key,
                data = fileBytes
            ) {
                if (upsert) {
                    this.upsert = true
                }
            }
            
            println("DEBUG: SupabaseStorageClient.uploadFile() - upload successful: $key (${fileBytes.size} bytes)")
            StorageResult(success = true, path = key)
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.uploadFile() - exception: ${e.message}")
            StorageResult(success = false, errorMessage = e.message ?: "Unknown error")
        }
    }
    
    override suspend fun getSignedUrl(bucket: String, key: String): String? {
        return try {
            println("DEBUG: SupabaseStorageClient.getSignedUrl() - generating signed URL for: $bucket/$key")
            
            // Generate signed URL from Supabase Storage
            val signedUrl = storage.from(bucket).createSignedUrl(
                path = key,
                expiresIn = kotlin.time.Duration.parse("1h")
            )
            
            println("DEBUG: SupabaseStorageClient.getSignedUrl() - signed URL generated: $signedUrl")
            signedUrl
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.getSignedUrl() - exception: ${e.message}")
            null
        }
    }
    
    /**
     * Get public URL for file (for public buckets)
     */
    suspend fun getPublicUrl(bucket: String, key: String): String? {
        return try {
            println("DEBUG: SupabaseStorageClient.getPublicUrl() - getting public URL for: $bucket/$key")
            
            val publicUrl = storage.from(bucket).publicUrl(key)
            
            println("DEBUG: SupabaseStorageClient.getPublicUrl() - public URL: $publicUrl")
            publicUrl
            
        } catch (e: Exception) {
            println("ERROR: SupabaseStorageClient.getPublicUrl() - exception: ${e.message}")
            null
        }
    }
    
    /**
     * Check if file exists in storage
     */
    suspend fun exists(bucket: String, key: String): Boolean {
        return try {
            println("DEBUG: SupabaseStorageClient.exists() - checking existence: $bucket/$key")
            
            // Try to get file info - if it throws, file doesn't exist
            storage.from(bucket).info(key)
            
            println("DEBUG: SupabaseStorageClient.exists() - file exists: $bucket/$key")
            true
            
        } catch (e: Exception) {
            println("DEBUG: SupabaseStorageClient.exists() - file does not exist: $bucket/$key")
            false
        }
    }
}

/**
 * Mock implementation for testing
 */
class MockStorageClient : StorageClient {
    override suspend fun upload(
        bucket: String,
        key: String,
        filePath: String,
        upsert: Boolean
    ): StorageResult {
        println("DEBUG: MockStorageClient.upload() - mock upload: $bucket/$key")
        return StorageResult(success = true, path = key)
    }
    
    override suspend fun uploadImage(
        bucket: String,
        key: String,
        localUri: String,
        maxLongEdgePx: Int,
        quality: Int,
        upsert: Boolean
    ): StorageResult {
        println("DEBUG: MockStorageClient.uploadImage() - mock image upload: $bucket/$key")
        return StorageResult(success = true, path = key)
    }
    
    override suspend fun uploadFile(
        bucket: String,
        key: String,
        localUri: String,
        upsert: Boolean
    ): StorageResult {
        println("DEBUG: MockStorageClient.uploadFile() - mock file upload: $bucket/$key")
        return StorageResult(success = true, path = key)
    }
    
    override suspend fun delete(bucket: String, key: String): StorageResult {
        println("DEBUG: MockStorageClient.delete() - mock delete: $bucket/$key")
        return StorageResult(success = true)
    }
    
    override suspend fun getSignedUrl(bucket: String, key: String): String? {
        println("DEBUG: MockStorageClient.getSignedUrl() - mock signed URL: $bucket/$key")
        return "https://mock.storage.com/$bucket/$key"
    }
}
