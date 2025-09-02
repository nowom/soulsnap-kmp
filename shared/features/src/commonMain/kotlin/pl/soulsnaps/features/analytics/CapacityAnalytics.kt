package pl.soulsnaps.features.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.utils.getCurrentTimeMillis

class CapacityAnalytics(
    private val accessGuard: AccessGuard,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    private val _usageStats = MutableStateFlow<CapacityUsageStats?>(null)
    val usageStats: StateFlow<CapacityUsageStats?> = _usageStats.asStateFlow()
    
    private val _trends = MutableStateFlow<List<UsageTrend>>(emptyList())
    val trends: StateFlow<List<UsageTrend>> = _trends.asStateFlow()
    
    private val _alerts = MutableStateFlow<List<CapacityAlert>>(emptyList())
    val alerts: StateFlow<List<CapacityAlert>> = _alerts.asStateFlow()
    
    suspend fun updateUsageStats(userId: String) {
        try {
            val newStats = CapacityUsageStats(
                userId = userId,
                timestamp = getCurrentTimeMillis(),
                totalMemories = 0, // TODO: Get from repository
                totalPhotos = 0,   // TODO: Get from repository
                totalAudio = 0,    // TODO: Get from repository
                totalVideos = 0,   // TODO: Get from repository
                storageUsed = 0L,  // TODO: Get from repository
                lastBackup = null  // TODO: Get from repository
            )
            
            _usageStats.value = newStats
            updateTrends(newStats)
            checkAlerts(newStats)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private fun updateTrends(stats: CapacityUsageStats) {
        val newTrend = UsageTrend(
            timestamp = getCurrentTimeMillis(),
            memoriesCount = stats.totalMemories,
            storageUsed = stats.storageUsed,
            growthRate = calculateGrowthRate(stats)
        )
        
        val currentTrends = _trends.value.toMutableList()
        currentTrends.add(newTrend)
        
        // Keep only last 30 trends
        if (currentTrends.size > 30) {
            currentTrends.removeAt(0)
        }
        
        _trends.value = currentTrends
    }
    
    private fun calculateGrowthRate(stats: CapacityUsageStats): Double {
        val currentTrends = _trends.value
        if (currentTrends.size < 2) return 0.0
        
        val previous = currentTrends[currentTrends.size - 2]
        val current = currentTrends.last()
        
        return if (previous.memoriesCount > 0) {
            ((current.memoriesCount - previous.memoriesCount).toDouble() / previous.memoriesCount) * 100
        } else 0.0
    }
    
    private fun checkAlerts(stats: CapacityUsageStats) {
        val newAlerts = mutableListOf<CapacityAlert>()
        
        // Check storage usage
        if (stats.storageUsed > 100 * 1024 * 1024) { // 100MB
            newAlerts.add(
                CapacityAlert(
                    type = AlertType.STORAGE_HIGH,
                    message = "Storage usage is high",
                    timestamp = getCurrentTimeMillis(),
                    severity = AlertSeverity.WARNING
                )
            )
        }
        
        // Check memory count
        if (stats.totalMemories > 100) {
            newAlerts.add(
                CapacityAlert(
                    type = AlertType.MEMORIES_HIGH,
                    message = "High number of memories",
                    timestamp = getCurrentTimeMillis(),
                    severity = AlertSeverity.INFO
                )
            )
        }
        
        // Check backup status
        if (stats.lastBackup == null || getCurrentTimeMillis() - stats.lastBackup > 7 * 24 * 60 * 60 * 1000) {
            newAlerts.add(
                CapacityAlert(
                    type = AlertType.BACKUP_NEEDED,
                    message = "Backup needed",
                    timestamp = getCurrentTimeMillis(),
                    severity = AlertSeverity.WARNING
                )
            )
        }
        
        _alerts.value = newAlerts
    }
    
    fun getUsageReport(): UsageReport {
        val stats = _usageStats.value
        val currentTrends = _trends.value
        
        return UsageReport(
            currentUsage = stats,
            trends = currentTrends,
            recommendations = generateRecommendations(stats, currentTrends),
            generatedAt = getCurrentTimeMillis()
        )
    }
    
    private fun generateRecommendations(
        stats: CapacityUsageStats?,
        trends: List<UsageTrend>
    ): List<UsageRecommendation> {
        val recommendations = mutableListOf<UsageRecommendation>()
        
        stats?.let { s ->
            if (s.storageUsed > 50 * 1024 * 1024) { // 50MB
                recommendations.add(
                    UsageRecommendation(
                        type = RecommendationType.STORAGE_OPTIMIZATION,
                        title = "Optimize Storage",
                        description = "Consider compressing photos or removing unused content",
                        priority = RecommendationPriority.MEDIUM
                    )
                )
            }
            
            if (s.totalMemories > 50) {
                recommendations.add(
                    UsageRecommendation(
                        type = RecommendationType.ORGANIZATION,
                        title = "Organize Memories",
                        description = "Create albums and categories for better organization",
                        priority = RecommendationPriority.LOW
                    )
                )
            }
        }
        
        if (trends.size >= 2) {
            val recentGrowth = trends.takeLast(3).map { it.growthRate }.average()
            if (recentGrowth > 20.0) {
                recommendations.add(
                    UsageRecommendation(
                        type = RecommendationType.GROWTH_MANAGEMENT,
                        title = "Manage Growth",
                        description = "Your memory collection is growing rapidly. Consider setting limits.",
                        priority = RecommendationPriority.HIGH
                    )
                )
            }
        }
        
        return recommendations
    }
    
    fun clearAlerts() {
        _alerts.value = emptyList()
    }
    
    fun dismissAlert(alertId: String) {
        val currentAlerts = _alerts.value.toMutableList()
        currentAlerts.removeAll { it.id == alertId }
        _alerts.value = currentAlerts
    }
}

// Data classes
data class CapacityUsageStats(
    val userId: String,
    val timestamp: Long,
    val totalMemories: Int,
    val totalPhotos: Int,
    val totalAudio: Int,
    val totalVideos: Int,
    val storageUsed: Long,
    val lastBackup: Long?
)

data class UsageTrend(
    val timestamp: Long,
    val memoriesCount: Int,
    val storageUsed: Long,
    val growthRate: Double
)

data class CapacityAlert(
    val id: String = "alert_${getCurrentTimeMillis()}",
    val type: AlertType,
    val message: String,
    val timestamp: Long,
    val severity: AlertSeverity
)

data class UsageReport(
    val currentUsage: CapacityUsageStats?,
    val trends: List<UsageTrend>,
    val recommendations: List<UsageRecommendation>,
    val generatedAt: Long
)

data class UsageRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority
)

enum class AlertType {
    STORAGE_HIGH,
    MEMORIES_HIGH,
    BACKUP_NEEDED
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class RecommendationType {
    STORAGE_OPTIMIZATION,
    ORGANIZATION,
    GROWTH_MANAGEMENT
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH
}
