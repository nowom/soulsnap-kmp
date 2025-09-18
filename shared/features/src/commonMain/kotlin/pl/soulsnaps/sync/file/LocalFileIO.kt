package pl.soulsnaps.sync.file

/**
 * Local file I/O operations for sync system
 */
interface LocalFileIO {
    /**
     * Read file as byte array
     * 
     * @param localUri Local file URI or path
     * @return File content as bytes
     */
    suspend fun readBytes(localUri: String): ByteArray
    
    /**
     * Write bytes to local file
     * 
     * @param localUri Local file URI or path
     * @param data Bytes to write
     * @return true if successful
     */
    suspend fun writeBytes(localUri: String, data: ByteArray): Boolean
    
    /**
     * Check if file exists
     * 
     * @param localUri Local file URI or path
     * @return true if file exists
     */
    suspend fun exists(localUri: String): Boolean
    
    /**
     * Get file size in bytes
     * 
     * @param localUri Local file URI or path
     * @return File size or -1 if not found
     */
    suspend fun getFileSize(localUri: String): Long
    
    /**
     * Delete local file
     * 
     * @param localUri Local file URI or path
     * @return true if deleted successfully
     */
    suspend fun delete(localUri: String): Boolean
    
    /**
     * Get file extension
     * 
     * @param localUri Local file URI or path
     * @return File extension (e.g., "jpg", "mp4") or null
     */
    fun getFileExtension(localUri: String): String?
    
    /**
     * Get MIME type from file
     * 
     * @param localUri Local file URI or path
     * @return MIME type (e.g., "image/jpeg") or null
     */
    suspend fun getMimeType(localUri: String): String?
}

/**
 * File operation result
 */
data class FileOperationResult(
    val success: Boolean,
    val data: ByteArray? = null,
    val size: Long = 0,
    val mimeType: String? = null,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as FileOperationResult
        
        if (success != other.success) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (size != other.size) return false
        if (mimeType != other.mimeType) return false
        if (errorMessage != other.errorMessage) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + size.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
