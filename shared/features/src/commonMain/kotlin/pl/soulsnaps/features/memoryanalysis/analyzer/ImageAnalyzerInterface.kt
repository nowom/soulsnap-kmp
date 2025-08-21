package pl.soulsnaps.features.memoryanalysis.analyzer

import pl.soulsnaps.photo.SharedImageInterface
import pl.soulsnaps.features.memoryanalysis.model.*

/**
 * Interface for ImageAnalyzer - allows mocking in tests
 */
interface ImageAnalyzerInterface {
    
    /**
     * Analyze a single image and return comprehensive analysis
     */
    suspend fun analyzeImage(image: SharedImageInterface): ImageAnalysis
    
    /**
     * Analyze multiple images in batch for better performance
     */
    suspend fun analyzeBatch(images: List<SharedImageInterface>): List<ImageAnalysis>
    
    /**
     * Analyze colors in an image
     */
    suspend fun analyzeColors(image: SharedImageInterface): ColorAnalysis
    
    /**
     * Detect faces in an image
     */
    suspend fun detectFaces(image: SharedImageInterface): FaceDetection?
    
    /**
     * Analyze mood based on image content
     */
    suspend fun analyzeMood(image: SharedImageInterface): MoodAnalysis
    
    /**
     * Analyze image composition
     */
    suspend fun analyzeComposition(image: SharedImageInterface): CompositionAnalysis
    
    /**
     * Get dominant colors from image
     */
    suspend fun getDominantColors(image: SharedImageInterface, count: Int): List<DominantColor>
    
    /**
     * Check if image analysis is available on this platform
     */
    fun isAnalysisAvailable(): Boolean
    
    /**
     * Get supported analysis features for this platform
     */
    fun getSupportedFeatures(): List<AnalysisFeature>
}
