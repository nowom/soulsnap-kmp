package pl.soulsnaps.sync.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Android implementation of ImagePipeline
 */
class AndroidImagePipeline(
    private val context: Context
) : ImagePipeline {
    
    override suspend fun toJpegBytes(
        localUri: String, 
        maxLongEdgePx: Int, 
        quality: Int
    ): ByteArray {
        return try {
            println("DEBUG: AndroidImagePipeline.toJpegBytes() - processing: $localUri")
            
            val uri = Uri.parse(localUri)
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open input stream for URI: $localUri")
            
            // Decode with bounds first to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            
            // Calculate scale factor
            val scaleFactor = calculateScaleFactor(originalWidth, originalHeight, maxLongEdgePx)
            
            // Decode with scaling
            val scaledOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
                inJustDecodeBounds = false
            }
            
            val secondInputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open input stream for URI: $localUri")
            
            val bitmap = BitmapFactory.decodeStream(secondInputStream, null, scaledOptions)
                ?: throw Exception("Cannot decode bitmap from URI: $localUri")
            
            secondInputStream.close()
            
            // Convert to JPEG bytes
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val jpegBytes = outputStream.toByteArray()
            
            bitmap.recycle()
            outputStream.close()
            
            println("DEBUG: AndroidImagePipeline.toJpegBytes() - original: ${originalWidth}x${originalHeight}, compressed: ${jpegBytes.size} bytes")
            jpegBytes
            
        } catch (e: Exception) {
            println("ERROR: AndroidImagePipeline.toJpegBytes() - error: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getImageDimensions(localUri: String): Pair<Int, Int>? {
        return try {
            val uri = Uri.parse(localUri)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else {
                null
            }
        } catch (e: Exception) {
            println("ERROR: AndroidImagePipeline.getImageDimensions() - error: ${e.message}")
            null
        }
    }
    
    override suspend fun isValidImage(localUri: String): Boolean {
        return try {
            val dimensions = getImageDimensions(localUri)
            dimensions != null && dimensions.first > 0 && dimensions.second > 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun calculateScaleFactor(width: Int, height: Int, maxLongEdge: Int): Int {
        val longEdge = maxOf(width, height)
        if (longEdge <= maxLongEdge) return 1
        
        var scaleFactor = 1
        while (longEdge / (scaleFactor * 2) > maxLongEdge) {
            scaleFactor *= 2
        }
        return scaleFactor
    }
}
