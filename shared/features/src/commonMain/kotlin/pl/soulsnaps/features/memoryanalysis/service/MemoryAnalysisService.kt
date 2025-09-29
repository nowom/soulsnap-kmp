package pl.soulsnaps.features.memoryanalysis.service

import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.access.guard.*
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.features.auth.model.*
import pl.soulsnaps.features.memoryanalysis.analyzer.ImageAnalyzerInterface
import pl.soulsnaps.features.memoryanalysis.engine.PatternDetectionEngineInterface
import pl.soulsnaps.features.memoryanalysis.model.*
import pl.soulsnaps.photo.SharedImageInterface
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.domain.model.MoodType as DomainMoodType

/**
 * Memory Analysis Service
 * Integruje się z AccessGuard dla access control
 */
class MemoryAnalysisService(
    private val imageAnalyzer: ImageAnalyzerInterface,
    private val patternDetectionEngine: PatternDetectionEngineInterface,
    private val guard: AccessGuard,
    private val userPlanManager: UserPlanManager
) {
    
    /**
     * Analiza pojedynczego wspomnienia - wymaga analysis.run.single scope
     */
    suspend fun analyzeMemory(
        userId: String,
        memory: Memory
    ): MemoryAnalysisResult {
        
        // SOLID: Dependency Inversion - guard to abstrakcja
        val accessResult = guard.allowAction(
            userId = userId,
            action = "analysis.run.single",
            quotaKey = "analysis.day",
            flagKey = "feature.analysis"
        )
        
        if (!accessResult.allowed) {
            return when (accessResult.reason) {
                DenyReason.MISSING_SCOPE -> MemoryAnalysisResult.Restricted(
                    memoryId = memory.id.toString(),
                    reason = accessResult.message ?: "Brak uprawnień do analizy",
                    requiredAction = "UPGRADE_PLAN",
                    recommendedPlan = accessResult.recommendedPlan
                )
                DenyReason.QUOTA_EXCEEDED -> MemoryAnalysisResult.Restricted(
                    memoryId = memory.id.toString(),
                    reason = accessResult.message ?: "Limit analiz wyczerpany",
                    requiredAction = "WAIT_RESET",
                    quotaInfo = accessResult.quotaInfo
                )
                DenyReason.FEATURE_OFF -> MemoryAnalysisResult.Restricted(
                    memoryId = memory.id.toString(),
                    reason = accessResult.message ?: "Funkcja niedostępna",
                    requiredAction = "WAIT_FEATURE",
                    featureInfo = accessResult.featureInfo
                )
                else -> MemoryAnalysisResult.Restricted(
                    memoryId = memory.id.toString(),
                    reason = "Nieznany błąd dostępu",
                    requiredAction = "CONTACT_SUPPORT"
                )
            }
        }
        
        val startTime = getCurrentTimeMillis()
        
        try {
            // Note: Memory doesn't have an 'image' field, so we'll skip image analysis for now
            val imageAnalysis: ImageAnalysis? = null
            
            // Generate basic insights for this memory
            val memoryInsights = generateBasicInsights(memory, imageAnalysis)
            
            val processingTime = getCurrentTimeMillis() - startTime
            
            return MemoryAnalysisResult.Success(
                memoryId = memory.id.toString(),
                imageAnalysis = imageAnalysis,
                insights = memoryInsights.content,
                processingTime = processingTime
            )
            
        } catch (e: Exception) {
            return MemoryAnalysisResult.Error(
                memoryId = memory.id.toString(),
                error = e.message ?: "Unknown error occurred",
                processingTime = getCurrentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Analiza wielu wspomnień dla wzorców - wymaga analysis.run.patterns scope
     */
    suspend fun analyzeMemories(
        userId: String,
        memories: List<Memory>
    ): MemoriesAnalysisResult {
        
        // SOLID: Interface Segregation - sprawdzamy konkretną akcję
        val accessResult = guard.allowAction(
            userId = userId,
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day",
            flagKey = "feature.patterns"
        )
        
        if (!accessResult.allowed) {
            return when (accessResult.reason) {
                DenyReason.MISSING_SCOPE -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Brak uprawnień do analizy wzorców",
                    requiredAction = "UPGRADE_PLAN",
                    recommendedPlan = accessResult.recommendedPlan
                )
                DenyReason.QUOTA_EXCEEDED -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Limit analiz wzorców wyczerpany",
                    requiredAction = "WAIT_RESET",
                    quotaInfo = accessResult.quotaInfo
                )
                DenyReason.FEATURE_OFF -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Funkcja wzorców niedostępna",
                    requiredAction = "WAIT_FEATURE",
                    featureInfo = accessResult.featureInfo
                )
                else -> MemoriesAnalysisResult.Restricted(
                    reason = "Nieznany błąd dostępu",
                    requiredAction = "CONTACT_SUPPORT"
                )
            }
        }
        
        val startTime = getCurrentTimeMillis()
        
        try {
            // Generate patterns and insights
            val patterns = patternDetectionEngine.detectPatterns(memories)
            val insights = patternDetectionEngine.generateInsights(memories)
            
            val processingTime = getCurrentTimeMillis() - startTime
            
            return MemoriesAnalysisResult.Success(
                patterns = patterns,
                insights = insights,
                processingTime = processingTime
            )
            
        } catch (e: Exception) {
            return MemoriesAnalysisResult.Error(
                error = e.message ?: "Unknown error occurred",
                processingTime = getCurrentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Analiza obrazów w batch - wymaga analysis.run.batch scope
     */
    suspend fun analyzeImagesBatch(
        userId: String,
        images: List<SharedImageInterface>
    ): MemoriesAnalysisResult {
        
        // SOLID: Open/Closed - łatwo dodać nowe sprawdzenia
        val accessResult = guard.allowAction(
            userId = userId,
            action = "analysis.run.batch",
            quotaKey = "analysis.batch.day",
            flagKey = "feature.batch_analysis"
        )
        
        if (!accessResult.allowed) {
            return when (accessResult.reason) {
                DenyReason.MISSING_SCOPE -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Brak uprawnień do analizy batch",
                    requiredAction = "UPGRADE_PLAN",
                    recommendedPlan = accessResult.recommendedPlan
                )
                DenyReason.QUOTA_EXCEEDED -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Limit analiz batch wyczerpany",
                    requiredAction = "WAIT_RESET",
                    quotaInfo = accessResult.quotaInfo
                )
                DenyReason.FEATURE_OFF -> MemoriesAnalysisResult.Restricted(
                    reason = accessResult.message ?: "Funkcja batch niedostępna",
                    requiredAction = "WAIT_FEATURE",
                    featureInfo = accessResult.featureInfo
                )
                else -> MemoriesAnalysisResult.Restricted(
                    reason = "Nieznany błąd dostępu",
                    requiredAction = "CONTACT_SUPPORT"
                )
            }
        }
        
        val startTime = getCurrentTimeMillis()
        
        try {
            // Batch image analysis
            val imageAnalyses = images.mapNotNull { image ->
                try {
                    imageAnalyzer.analyzeImage(image)
                } catch (e: Exception) {
                    null // Skip failed images
                }
            }
            
            val processingTime = getCurrentTimeMillis() - startTime
            
            return MemoriesAnalysisResult.Success(
                patterns = MemoryPatterns(
                    locationPatterns = emptyList(),
                    timePatterns = emptyList(),
                    activityPatterns = emptyList(),
                    moodPatterns = emptyList()
                ), // No patterns for batch images
                insights = pl.soulsnaps.features.memoryanalysis.model.MemoryInsights(
                    weeklyStats = WeeklyStats(
                        totalPhotos = images.size,
                        averageMood = DomainMoodType.NEUTRAL,
                        topLocations = emptyList(),
                        moodTrend = MoodTrend.STABLE,
                        activityBreakdown = emptyMap()
                    ),
                    monthlyTrends = MonthlyTrends(
                        moodProgression = emptyList(),
                        locationExploration = LocationExploration(
                            newLocations = emptyList(),
                            favoriteLocations = emptyList(),
                            locationDiversity = 0.0f
                        ),
                        activityEvolution = ActivityEvolution(
                            newActivities = emptyList(),
                            activityDiversity = 0.0f,
                            mostActiveTime = TimeOfDay.AFTERNOON
                        )
                    ),
                    recommendations = emptyList(),
                    generatedAt = getCurrentTimeMillis()
                ),
                processingTime = processingTime,
                imageAnalyses = imageAnalyses
            )
            
        } catch (e: Exception) {
            return MemoriesAnalysisResult.Error(
                error = e.message ?: "Unknown error occurred",
                processingTime = getCurrentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Pobierz możliwości analizy - sprawdza wszystkie dostępne funkcje
     */
    suspend fun getAnalysisCapabilities(userId: String): AnalysisCapabilities {
        
        // SOLID: Single Responsibility - każda metoda ma jedną odpowiedzialność
        val basicAnalysis = guard.canPerformAction(
            userId = userId,
            action = "analysis.run.single",
            flagKey = "feature.analysis"
        )
        
        val patternAnalysis = guard.canPerformAction(
            userId = userId,
            action = "analysis.run.patterns",
            flagKey = "feature.patterns"
        )
        
        val batchAnalysis = guard.canPerformAction(
            userId = userId,
            action = "analysis.run.batch",
            flagKey = "feature.batch_analysis"
        )
        
        val insightsAccess = guard.canPerformAction(
            userId = userId,
            action = "insights.read",
            flagKey = "feature.insights"
        )
        
        val exportAccess = guard.canPerformAction(
            userId = userId,
            action = "insights.export",
            flagKey = "feature.export"
        )
        
        return pl.soulsnaps.features.auth.model.AnalysisCapabilities(
            canAnalyzePhotos = basicAnalysis.allowed,
            canAnalyzeVideos = basicAnalysis.allowed,
            canAnalyzeAudio = basicAnalysis.allowed,
            canDetectPatterns = patternAnalysis.allowed,
            canGenerateInsights = insightsAccess.allowed,
            maxAnalysisPerDay = when (userPlanManager.getCurrentPlan()) {
                "PREMIUM" -> 1000
                "PRO" -> 500
                else -> 100 // FREE_USER
            },
            supportedFormats = listOf("jpg", "png", "mp4", "mov", "mp3", "wav")
        )
    }
    
    /**
     * Pobierz status quota użytkownika
     */
    suspend fun getUserQuotaStatus(userId: String): Map<String, Int> {
        return mapOf(
            "analysis.day" to guard.getQuotaStatus(userId, "analysis.day"),
            "analysis.patterns.day" to guard.getQuotaStatus(userId, "analysis.patterns.day"),
            "analysis.batch.day" to guard.getQuotaStatus(userId, "analysis.batch.day"),
            "snaps.capacity" to guard.getQuotaStatus(userId, "snaps.capacity")
        )
    }
    
    /**
     * Pobierz informacje o planie użytkownika
     */
    suspend fun getUserPlanInfo(userId: String): UserPlanInfo? {
        val scopes = guard.getUserScopes(userId)
        val features = guard.getAllFeatures()
        
        return UserPlanInfo(
            scopes = scopes,
            features = features,
            quotas = getUserQuotaStatus(userId)
        )
    }
    
    /**
     * Get upgrade recommendations for analysis features
     */
    suspend fun getAnalysisUpgradeRecommendations(userId: String): List<pl.soulsnaps.features.auth.model.UpgradeRecommendation> {
        return listOf(
            pl.soulsnaps.features.auth.model.UpgradeRecommendation(
                feature = pl.soulsnaps.features.auth.model.FeatureCategory.MEMORY_ANALYSIS,
                currentPlan = pl.soulsnaps.features.auth.model.SubscriptionPlan.FREE,
                recommendedPlan = pl.soulsnaps.features.auth.model.SubscriptionPlan.PREMIUM,
                reason = "Upgrade to access advanced memory analysis features",
                benefits = listOf("Advanced AI analysis", "Pattern detection", "Detailed insights"),
                estimatedCost = "29.99/month"
            )
        )
    }
    
    /**
     * Check if user can access analysis feature
     */
    suspend fun canAccessAnalysisFeature(userId: String, feature: pl.soulsnaps.features.auth.model.FeatureCategory): Boolean {
        val userScopes = guard.getUserScopes(userId)
        return userScopes.contains("analysis.${feature.name.lowercase()}")
    }
    
    // Helper methods
    private fun generateBasicInsights(memory: Memory, imageAnalysis: ImageAnalysis?): pl.soulsnaps.features.auth.model.MemoryInsight {
        return pl.soulsnaps.features.auth.model.MemoryInsight(
            id = memory.id.toString(),
            memoryId = memory.id.toString(),
            insightType = "BASIC",
            content = "Memory from ${memory.locationName ?: "unknown location"} with mood: ${memory.mood?.name ?: "unknown"}",
            confidence = 0.8f
        )
    }
    
    private fun generateMemoryInsights(memory: Memory, imageAnalysis: ImageAnalysis?): pl.soulsnaps.features.auth.model.MemoryInsight {
        return pl.soulsnaps.features.auth.model.MemoryInsight(
            id = memory.id.toString(),
            memoryId = memory.id.toString(),
            insightType = "ENHANCED",
            content = "Enhanced analysis of memory with image analysis",
            confidence = 0.9f
        )
    }
}

/**
 * Enhanced Memory Analysis Result - SOLID: Open/Closed
 */
sealed class MemoryAnalysisResult {
    data class Success(
        val memoryId: String,
        val imageAnalysis: ImageAnalysis?,
        val insights: String,
        val processingTime: Long
    ) : MemoryAnalysisResult()
    
    data class Error(
        val memoryId: String,
        val error: String,
        val processingTime: Long
    ) : MemoryAnalysisResult()
    
    data class Restricted(
        val memoryId: String,
        val reason: String,
        val requiredAction: String,
        val recommendedPlan: String? = null,
        val quotaInfo: QuotaInfo? = null,
        val featureInfo: FeatureInfo? = null
    ) : MemoryAnalysisResult()
}

/**
 * Enhanced Memories Analysis Result - SOLID: Open/Closed
 */
sealed class MemoriesAnalysisResult {
    data class Success(
        val patterns: MemoryPatterns,
        val insights: MemoryInsights,
        val processingTime: Long,
        val imageAnalyses: List<ImageAnalysis> = emptyList()
    ) : MemoriesAnalysisResult()
    
    data class Error(
        val error: String,
        val processingTime: Long
    ) : MemoriesAnalysisResult()
    
    data class Restricted(
        val reason: String,
        val requiredAction: String,
        val recommendedPlan: String? = null,
        val quotaInfo: QuotaInfo? = null,
        val featureInfo: FeatureInfo? = null
    ) : MemoriesAnalysisResult()
}

/**
 * User Plan Info - SOLID: Open/Closed
 */
data class UserPlanInfo(
    val scopes: List<String>,
    val features: Map<String, Boolean>,
    val quotas: Map<String, Int>
)
