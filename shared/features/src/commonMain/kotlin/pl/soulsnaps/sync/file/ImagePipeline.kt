package pl.soulsnaps.sync.file

/**
 * Image processing pipeline for upload optimization
 */
interface ImagePipeline {
    /**
     * Convert image to JPEG bytes with compression and resizing
     * 
     * @param localUri Local file URI or path
     * @param maxLongEdgePx Maximum size for the longer edge (width or height)
     * @param quality JPEG quality (0-100)
     * @return Compressed JPEG bytes
     */
    suspend fun toJpegBytes(
        localUri: String, 
        maxLongEdgePx: Int = 1920, 
        quality: Int = 85
    ): ByteArray
    
    /**
     * Get image dimensions without loading full image
     * 
     * @param localUri Local file URI or path
     * @return Pair of (width, height) or null if cannot read
     */
    suspend fun getImageDimensions(localUri: String): Pair<Int, Int>?
    
    /**
     * Check if file is a valid image
     * 
     * @param localUri Local file URI or path
     * @return true if valid image format
     */
    suspend fun isValidImage(localUri: String): Boolean
}

/**
 * Image processing result
 */
data class ImageProcessingResult(
    val success: Boolean,
    val data: ByteArray? = null,
    val originalSize: Long = 0,
    val compressedSize: Long = 0,
    val compressionRatio: Float = 0f,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as ImageProcessingResult
        
        if (success != other.success) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (originalSize != other.originalSize) return false
        if (compressedSize != other.compressedSize) return false
        if (compressionRatio != other.compressionRatio) return false
        if (errorMessage != other.errorMessage) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + originalSize.hashCode()
        result = 31 * result + compressedSize.hashCode()
        result = 31 * result + compressionRatio.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
