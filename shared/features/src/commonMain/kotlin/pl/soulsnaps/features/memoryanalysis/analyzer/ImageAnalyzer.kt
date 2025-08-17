package pl.soulsnaps.features.memoryanalysis.analyzer

import pl.soulsnaps.photo.SharedImage
import pl.soulsnaps.features.memoryanalysis.model.*

/**
 * Cross-platform interface for image analysis
 * Implemented differently on Android (TensorFlow Lite + OpenCV) and iOS (Core ML + Vision)
 */
expect class ImageAnalyzer {
    
    /**
     * Analyze a single image and return comprehensive analysis
     */
    suspend fun analyzeImage(image: SharedImage): ImageAnalysis
    
    /**
     * Analyze multiple images in batch for better performance
     */
    suspend fun analyzeBatch(images: List<SharedImage>): List<ImageAnalysis>
    
    /**
     * Analyze colors in an image
     */
    suspend fun analyzeColors(image: SharedImage): ColorAnalysis
    
    /**
     * Detect faces in an image
     */
    suspend fun detectFaces(image: SharedImage): FaceDetection?
    
    /**
     * Analyze mood based on image content
     */
    suspend fun analyzeMood(image: SharedImage): MoodAnalysis
    
    /**
     * Analyze image composition
     */
    suspend fun analyzeComposition(image: SharedImage): CompositionAnalysis
    
    /**
     * Get dominant colors from image
     */
    suspend fun getDominantColors(image: SharedImage, count: Int = 5): List<DominantColor>
    
    /**
     * Check if image analysis is available on this platform
     */
    fun isAnalysisAvailable(): Boolean
    
    /**
     * Get supported analysis features for this platform
     */
    fun getSupportedFeatures(): List<AnalysisFeature>
}

/**
 * Available analysis features
 */
enum class AnalysisFeature {
    COLOR_ANALYSIS,
    FACE_DETECTION,
    EMOTION_DETECTION,
    MOOD_ANALYSIS,
    COMPOSITION_ANALYSIS,
    AGE_DETECTION,
    GENDER_DETECTION,
    OBJECT_DETECTION
}

/**
 * Analysis configuration
 */
data class AnalysisConfig(
    val enableColorAnalysis: Boolean = true,
    val enableFaceDetection: Boolean = true,
    val enableMoodAnalysis: Boolean = true,
    val enableCompositionAnalysis: Boolean = true,
    val maxFaces: Int = 10,
    val colorPaletteSize: Int = 16,
    val processingQuality: ProcessingQuality = ProcessingQuality.BALANCED
)

enum class ProcessingQuality {
    FAST,      // Lower quality, faster processing
    BALANCED,  // Balanced quality and speed
    HIGH       // Higher quality, slower processing
}
