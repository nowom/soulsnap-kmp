package pl.soulsnaps.features.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import pl.soulsnaps.features.auth.mvp.guard.CapacityGuard
import pl.soulsnaps.features.auth.mvp.guard.CapacityInfo
import pl.soulsnaps.features.auth.mvp.guard.UpgradeRecommendation

/**
 * CapacityAnalytics - system monitorowania i raportowania wykorzystania limitów
 * 
 * Funkcjonalności:
 * - Real-time monitoring wykorzystania limitów
 * - Alerty gdy limity są bliskie wyczerpania
 * - Raporty wykorzystania w czasie
 * - Rekomendacje upgrade'ów
 * - Analityka trendów
 */
class CapacityAnalytics(
    private val capacityGuard: CapacityGuard
) {
    
    private val _usageStats = MutableStateFlow(CapacityUsageStats())
    val usageStats: StateFlow<CapacityUsageStats> = _usageStats
    
    private val _alerts = MutableStateFlow<List<CapacityAlert>>(emptyList())
    val alerts: StateFlow<List<CapacityAlert>> = _alerts
    
    private val _trends = MutableStateFlow(CapacityTrends())
    val trends: StateFlow<CapacityTrends> = _trends
    
    /**
     * Aktualizuj statystyki wykorzystania dla użytkownika
     */
    suspend fun updateUsageStats(userId: String) {
        try {
            val capacityInfo = capacityGuard.getCapacityInfo(userId)
            val recommendation = capacityGuard.getUpgradeRecommendation(userId)
            
            val newStats = CapacityUsageStats(
                userId = userId,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                capacityInfo = capacityInfo,
                upgradeRecommendation = recommendation,
                usagePercentages = calculateUsagePercentages(capacityInfo)
            )
            
            _usageStats.value = newStats
            
            // Sprawdź czy potrzebne są alerty
            checkForAlerts(newStats)
            
            // Aktualizuj trendy
            updateTrends(newStats)
            
        } catch (e: Exception) {
            // Log error but don't crash
            println("Error updating usage stats: ${e.message}")
        }
    }
    
    /**
     * Pobierz statystyki wykorzystania jako Flow
     */
    fun getUsageStatsFlow(userId: String): Flow<CapacityUsageStats> {
        return usageStats.map { it }
    }
    
    /**
     * Pobierz alerty jako Flow
     */
    fun getAlertsFlow(): Flow<List<CapacityAlert>> {
        return alerts
    }
    
    /**
     * Pobierz trendy jako Flow
     */
    fun getTrendsFlow(): Flow<CapacityTrends> {
        return trends
    }
    
    /**
     * Sprawdź czy użytkownik potrzebuje upgrade'u
     */
    suspend fun checkUpgradeNeeded(userId: String): UpgradeNeededResult {
        val stats = _usageStats.value
        val recommendation = capacityGuard.getUpgradeRecommendation(userId)
        
        return UpgradeNeededResult(
            needsUpgrade = recommendation.recommendedPlan != null,
            recommendedPlan = recommendation.recommendedPlan,
            urgency = recommendation.urgency,
            reasons = recommendation.recommendations
        )
    }
    
    /**
     * Pobierz raport wykorzystania
     */
    fun getUsageReport(userId: String): CapacityUsageReport {
        val stats = _usageStats.value
        val currentAlerts = _alerts.value
        val currentTrends = _trends.value
        
        return CapacityUsageReport(
            userId = userId,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            usageStats = stats,
            activeAlerts = currentAlerts,
            trends = currentTrends,
            recommendations = generateRecommendations(stats, currentAlerts, currentTrends)
        )
    }
    
    /**
     * Wyczyść alerty
     */
    fun clearAlerts() {
        _alerts.value = emptyList()
    }
    
    /**
     * Wyczyść konkretny alert
     */
    fun clearAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
    }
    
    // Private helper methods
    
    private fun calculateUsagePercentages(capacityInfo: CapacityInfo): UsagePercentages {
        return UsagePercentages(
            snaps = calculatePercentage(capacityInfo.snaps?.current, capacityInfo.snaps?.limit),
            storage = calculatePercentage(capacityInfo.storage?.current, capacityInfo.storage?.limit),
            aiAnalysis = calculatePercentage(capacityInfo.aiAnalysis?.current, capacityInfo.aiAnalysis?.limit),
            memories = calculatePercentage(capacityInfo.memories?.current, capacityInfo.memories?.limit)
        )
    }
    
    private fun calculatePercentage(current: Int?, limit: Int?): Double {
        if (current == null || limit == null || limit <= 0) return 0.0
        return (current.toDouble() / limit.toDouble()) * 100.0
    }
    
    private fun checkForAlerts(stats: CapacityUsageStats) {
        val newAlerts = mutableListOf<CapacityAlert>()
        
        // Sprawdź różne progi alertów
        stats.usagePercentages.snaps?.let { percentage ->
            when {
                percentage >= 90 -> newAlerts.add(
                    CapacityAlert(
                        id = "snaps_critical_${stats.timestamp}",
                        type = AlertType.CRITICAL,
                        category = AlertCategory.SNAPS,
                        message = "Krytyczne wykorzystanie snapów: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
                percentage >= 75 -> newAlerts.add(
                    CapacityAlert(
                        id = "snaps_warning_${stats.timestamp}",
                        type = AlertType.WARNING,
                        category = AlertCategory.SNAPS,
                        message = "Wysokie wykorzystanie snapów: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
            }
        }
        
        stats.usagePercentages.storage?.let { percentage ->
            when {
                percentage >= 90 -> newAlerts.add(
                    CapacityAlert(
                        id = "storage_critical_${stats.timestamp}",
                        type = AlertType.CRITICAL,
                        category = AlertCategory.STORAGE,
                        message = "Krytyczne wykorzystanie storage: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
                percentage >= 75 -> newAlerts.add(
                    CapacityAlert(
                        id = "storage_warning_${stats.timestamp}",
                        type = AlertType.WARNING,
                        category = AlertCategory.STORAGE,
                        message = "Wysokie wykorzystanie storage: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
            }
        }
        
        stats.usagePercentages.aiAnalysis?.let { percentage ->
            when {
                percentage >= 90 -> newAlerts.add(
                    CapacityAlert(
                        id = "ai_critical_${stats.timestamp}",
                        type = AlertType.CRITICAL,
                        category = AlertCategory.AI_ANALYSIS,
                        message = "Krytyczne wykorzystanie AI: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
                percentage >= 75 -> newAlerts.add(
                    CapacityAlert(
                        id = "ai_warning_${stats.timestamp}",
                        type = AlertType.WARNING,
                        category = AlertCategory.AI_ANALYSIS,
                        message = "Wysokie wykorzystanie AI: ${percentage.toInt()}%",
                        percentage = percentage
                    )
                )
            }
        }
        
        _alerts.value = newAlerts
    }
    
    private fun updateTrends(stats: CapacityUsageStats) {
        val currentTrends = _trends.value
        val updatedHistory = currentTrends.usageHistory + stats
        val limitedHistory = if (updatedHistory.size > 30) {
            updatedHistory.takeLast(30)
        } else {
            updatedHistory
        }
        
        val newTrends = currentTrends.copy(
            lastUpdate = stats.timestamp,
            usageHistory = limitedHistory
        )
        
        _trends.value = newTrends
    }
    
    private fun generateRecommendations(
        stats: CapacityUsageStats,
        alerts: List<CapacityAlert>,
        trends: CapacityTrends
    ): List<CapacityRecommendation> {
        val recommendations = mutableListOf<CapacityRecommendation>()
        
        // Rekomendacje na podstawie alertów
        if (alerts.any { it.type == AlertType.CRITICAL }) {
            recommendations.add(
                CapacityRecommendation(
                    type = RecommendationType.UPGRADE_URGENT,
                    title = "Pilny upgrade potrzebny",
                    description = "Wykorzystanie limitów jest krytyczne. Rozważ upgrade planu.",
                    priority = Priority.HIGH
                )
            )
        }
        
        // Rekomendacje na podstawie trendów
        if (trends.usageHistory.size >= 3) {
            val recentUsage = trends.usageHistory.takeLast(3)
            val avgUsage = recentUsage.map { it.usagePercentages.snaps ?: 0.0 }.average()
            
            if (avgUsage > 70) {
                recommendations.add(
                    CapacityRecommendation(
                        type = RecommendationType.UPGRADE_RECOMMENDED,
                        title = "Upgrade zalecany",
                        description = "Średnie wykorzystanie snapów: ${avgUsage.toInt()}%. Rozważ upgrade.",
                        priority = Priority.MEDIUM
                    )
                )
            }
        }
        
        return recommendations
    }
}

// Data classes

data class CapacityUsageStats(
    val userId: String = "",
    val timestamp: Long = 0L,
    val capacityInfo: CapacityInfo? = null,
    val upgradeRecommendation: UpgradeRecommendation? = null,
    val usagePercentages: UsagePercentages = UsagePercentages()
)

data class UsagePercentages(
    val snaps: Double? = null,
    val storage: Double? = null,
    val aiAnalysis: Double? = null,
    val memories: Double? = null
)

data class CapacityAlert(
    val id: String,
    val type: AlertType,
    val category: AlertCategory,
    val message: String,
    val percentage: Double,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

enum class AlertType {
    WARNING, CRITICAL
}

enum class AlertCategory {
    SNAPS, STORAGE, AI_ANALYSIS, MEMORIES
}

data class CapacityTrends(
    val lastUpdate: Long = 0L,
    val usageHistory: List<CapacityUsageStats> = emptyList()
)

data class UpgradeNeededResult(
    val needsUpgrade: Boolean,
    val recommendedPlan: String?,
    val urgency: pl.soulsnaps.features.auth.mvp.guard.UpgradeUrgency,
    val reasons: List<String>
)

data class CapacityUsageReport(
    val userId: String,
    val timestamp: Long,
    val usageStats: CapacityUsageStats,
    val activeAlerts: List<CapacityAlert>,
    val trends: CapacityTrends,
    val recommendations: List<CapacityRecommendation>
)

data class CapacityRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority
)

enum class RecommendationType {
    UPGRADE_URGENT, UPGRADE_RECOMMENDED, OPTIMIZE_USAGE, MONITOR
}

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
