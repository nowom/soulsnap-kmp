package pl.soulsnaps.features.memoryanalysis.analyzer

import pl.soulsnaps.photo.SharedImageInterface
import pl.soulsnaps.features.memoryanalysis.model.*

/**
 * Cross-platform interface for image analysis
 * Implemented differently on Android (TensorFlow Lite + OpenCV) and iOS (Core ML + Vision)
 */
expect class ImageAnalyzer : ImageAnalyzerInterface {
    
    /**
     * Analyze a single image and return comprehensive analysis
     */
    override suspend fun analyzeImage(image: SharedImageInterface): ImageAnalysis
    
    /**
     * Analyze multiple images in batch for better performance
     */
    override suspend fun analyzeBatch(images: List<SharedImageInterface>): List<ImageAnalysis>
    
    /**
     * Analyze colors in an image
     */
    override suspend fun analyzeColors(image: SharedImageInterface): ColorAnalysis
    
    /**
     * Detect faces in an image
     */
    override suspend fun detectFaces(image: SharedImageInterface): FaceDetection?
    
    /**
     * Analyze mood based on image content
     */
    override suspend fun analyzeMood(image: SharedImageInterface): MoodAnalysis
    
    /**
     * Analyze image composition
     */
    override suspend fun analyzeComposition(image: SharedImageInterface): CompositionAnalysis
    
    /**
     * Get dominant colors from image
     */
    override suspend fun getDominantColors(image: SharedImageInterface, count: Int): List<DominantColor>
    
    /**
     * Check if image analysis is available on this platform
     */
    override fun isAnalysisAvailable(): Boolean
    
    /**
     * Get supported analysis features for this platform
     */
    override fun getSupportedFeatures(): List<AnalysisFeature>
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
