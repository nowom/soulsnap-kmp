package pl.soulsnaps.features.upgrade

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.DefaultPlans
import pl.soulsnaps.access.model.PlanType

/**
 * UpgradeRecommendationEngine - inteligentny silnik rekomendacji upgrade
 * 
 * Analizuje:
 * - Aktualny plan użytkownika
 * - Wykorzystanie limitów
 * - Częstotliwość używania funkcji
 * - Typ użytkownika (casual, power user, etc.)
 * - Korzyści z upgrade
 */
class UpgradeRecommendationEngine(
    private val planRegistry: PlanRegistryReader
) {
    
    private val _recommendations = MutableStateFlow<List<UpgradeRecommendation>>(emptyList())
    val recommendations: Flow<List<UpgradeRecommendation>> = _recommendations.asStateFlow()
    
    /**
     * Analizuje użytkownika i generuje rekomendacje
     */
    suspend fun analyzeUserAndGenerateRecommendations(
        currentPlan: String,
        usageStats: UsageStatistics,
        userBehavior: UserBehavior
    ): List<UpgradeRecommendation> {
        val recommendations = mutableListOf<UpgradeRecommendation>()
        
        // 1. Analiza wykorzystania limitów
        val limitBasedRecommendations = analyzeLimitUsage(currentPlan, usageStats)
        recommendations.addAll(limitBasedRecommendations)
        
        // 2. Analiza zachowania użytkownika
        val behaviorBasedRecommendations = analyzeUserBehavior(currentPlan, userBehavior)
        recommendations.addAll(behaviorBasedRecommendations)
        
        // 3. Analiza funkcji premium
        val featureBasedRecommendations = analyzeFeatureGaps(currentPlan, userBehavior)
        recommendations.addAll(featureBasedRecommendations)
        
        // 4. Sortuj według priorytetu
        val sortedRecommendations = recommendations.sortedByDescending { it.priority }
        
        _recommendations.value = sortedRecommendations
        return sortedRecommendations
    }
    
    /**
     * Analizuje wykorzystanie limitów i sugeruje upgrade
     */
    private suspend fun analyzeLimitUsage(currentPlan: String, usageStats: UsageStatistics): List<UpgradeRecommendation> {
        val recommendations = mutableListOf<UpgradeRecommendation>()
        val currentPlanDef = planRegistry.getPlan(currentPlan) ?: return emptyList()
        
        // Sprawdź wykorzystanie SoulSnaps
        val snapsCapacity = currentPlanDef.quotas["snaps.capacity"]
        if (snapsCapacity != null && snapsCapacity > 0) {
            val snapsUsage = usageStats.soulSnapsCount.toFloat() / snapsCapacity.toFloat()
            if (snapsUsage > 0.8f) {
            recommendations.add(
                UpgradeRecommendation(
                    type = UpgradeType.LIMIT_REACHED,
                    title = "Limit SoulSnaps prawie wyczerpany",
                    description = "Wykorzystałeś ${(snapsUsage * 100).toInt()}% swojego limitu SoulSnaps. Upgrade zwiększy limit.",
                    priority = Priority.HIGH,
                    currentPlan = currentPlan,
                    recommendedPlan = getNextPlan(currentPlan),
                    benefits = listOf(
                        "Większy limit SoulSnaps",
                        "Więcej miejsca na wspomnienia",
                        "Brak obaw o przekroczenie limitu"
                    ),
                    urgency = Urgency.HIGH
                )
            )
        }
        }
        
        // Sprawdź wykorzystanie AI
        val aiDaily = currentPlanDef.quotas["ai.daily"]
        if (aiDaily != null && aiDaily > 0) {
            val aiUsage = usageStats.aiAnalysisCount.toFloat() / aiDaily.toFloat()
            if (aiUsage > 0.7f) {
            recommendations.add(
                UpgradeRecommendation(
                    type = UpgradeType.LIMIT_REACHED,
                    title = "Limit analizy AI prawie wyczerpany",
                    description = "Wykorzystałeś ${(aiUsage * 100).toInt()}% dziennego limitu analizy AI.",
                    priority = Priority.MEDIUM,
                    currentPlan = currentPlan,
                    recommendedPlan = getNextPlan(currentPlan),
                    benefits = listOf(
                        "Więcej analiz AI dziennie",
                        "Głębsze insights",
                        "Lepsze zrozumienie emocji"
                    ),
                    urgency = Urgency.MEDIUM
                )
            )
        }
        }
        
        return recommendations
    }
    
    /**
     * Analizuje zachowanie użytkownika
     */
    private suspend fun analyzeUserBehavior(currentPlan: String, userBehavior: UserBehavior): List<UpgradeRecommendation> {
        val recommendations = mutableListOf<UpgradeRecommendation>()
        
        // Power user detection
        if (userBehavior.dailyActiveDays > 20 && userBehavior.avgSessionDuration > 15) {
            recommendations.add(
                UpgradeRecommendation(
                    type = UpgradeType.POWER_USER,
                    title = "Jesteś Power User!",
                    description = "Używasz aplikacji intensywnie. Premium plan jest dla Ciebie idealny.",
                    priority = Priority.HIGH,
                    currentPlan = currentPlan,
                    recommendedPlan = PlanType.PREMIUM_USER.name,
                    benefits = listOf(
                        "Nielimitowane funkcje",
                        "Zaawansowana analityka",
                        "Funkcje eksperymentalne",
                        "Priorytetowe wsparcie"
                    ),
                    urgency = Urgency.HIGH
                )
            )
        }
        
        // Feature usage analysis
        if (userBehavior.affirmationsUsage > 0.8f && currentPlan == PlanType.GUEST.name) {
            recommendations.add(
                UpgradeRecommendation(
                    type = UpgradeType.FEATURE_USAGE,
                    title = "Kochasz afirmacje!",
                    description = "Intensywnie używasz afirmacji. Upgrade odblokuje pełne funkcje.",
                    priority = Priority.MEDIUM,
                    currentPlan = currentPlan,
                    recommendedPlan = PlanType.FREE_USER.name,
                    benefits = listOf(
                        "Nielimitowane afirmacje",
                        "Personalizowane treści",
                        "Historia afirmacji",
                        "Eksport afirmacji"
                    ),
                    urgency = Urgency.MEDIUM
                )
            )
        }
        
        return recommendations
    }
    
    /**
     * Analizuje brakujące funkcje premium
     */
    private suspend fun analyzeFeatureGaps(currentPlan: String, userBehavior: UserBehavior): List<UpgradeRecommendation> {
        val recommendations = mutableListOf<UpgradeRecommendation>()
        
        when (currentPlan) {
            PlanType.GUEST.name -> {
                recommendations.add(
                    UpgradeRecommendation(
                        type = UpgradeType.FEATURE_GAP,
                        title = "Odkryj pełny potencjał SoulSnaps",
                        description = "Jako gość masz ograniczony dostęp. Upgrade odblokuje wszystkie funkcje.",
                        priority = Priority.HIGH,
                        currentPlan = currentPlan,
                        recommendedPlan = PlanType.FREE_USER.name,
                        benefits = listOf(
                            "Nielimitowane SoulSnaps",
                            "Pełne funkcje afirmacji",
                            "Zaawansowane ćwiczenia",
                            "Wirtualne lustro",
                            "Podstawowa analityka"
                        ),
                        urgency = Urgency.HIGH
                    )
                )
            }
            
            PlanType.FREE_USER.name -> {
                if (userBehavior.analyticsInterest > 0.6f) {
                    recommendations.add(
                        UpgradeRecommendation(
                            type = UpgradeType.FEATURE_GAP,
                            title = "Interesujesz się analityką?",
                            description = "Premium plan oferuje zaawansowaną analitykę i insights.",
                            priority = Priority.MEDIUM,
                            currentPlan = currentPlan,
                            recommendedPlan = PlanType.PREMIUM_USER.name,
                            benefits = listOf(
                                "Zaawansowana analityka",
                                "Trendy emocjonalne",
                                "Eksport raportów",
                                "Funkcje eksperymentalne",
                                "Priorytetowe wsparcie"
                            ),
                            urgency = Urgency.MEDIUM
                        )
                    )
                }
            }
        }
        
        return recommendations
    }
    
    /**
     * Pobiera następny plan w hierarchii
     */
    private fun getNextPlan(currentPlan: String): String {
        return when (currentPlan) {
            PlanType.GUEST.name -> PlanType.FREE_USER.name
            PlanType.FREE_USER.name -> PlanType.PREMIUM_USER.name
            PlanType.PREMIUM_USER.name -> PlanType.ENTERPRISE_USER.name
            else -> PlanType.PREMIUM_USER.name
        }
    }
    
    /**
     * Pobiera aktualne rekomendacje
     */
    fun getCurrentRecommendations(): List<UpgradeRecommendation> {
        return _recommendations.value
    }
    
    /**
     * Czyści rekomendacje
     */
    fun clearRecommendations() {
        _recommendations.value = emptyList()
    }
}

/**
 * Typy rekomendacji upgrade
 */
enum class UpgradeType {
    LIMIT_REACHED,      // Limit prawie wyczerpany
    POWER_USER,         // Użytkownik intensywny
    FEATURE_USAGE,      // Intensywne używanie funkcji
    FEATURE_GAP,        // Brakujące funkcje premium
    PROMOTIONAL         // Promocja specjalna
}

/**
 * Priorytet rekomendacji
 */
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Pilność upgrade
 */
enum class Urgency {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Rekomendacja upgrade
 */
data class UpgradeRecommendation(
    val type: UpgradeType,
    val title: String,
    val description: String,
    val priority: Priority,
    val currentPlan: String,
    val recommendedPlan: String,
    val benefits: List<String>,
    val urgency: Urgency,
    val promotionalPrice: Double? = null,
    val validUntil: Long? = null
)

/**
 * Statystyki użycia
 */
data class UsageStatistics(
    val soulSnapsCount: Int = 0,
    val aiAnalysisCount: Int = 0,
    val affirmationsCount: Int = 0,
    val exercisesCount: Int = 0,
    val storageUsedGB: Float = 0f,
    val lastActivityDate: Long = 0L
)

/**
 * Zachowanie użytkownika
 */
data class UserBehavior(
    val dailyActiveDays: Int = 0,
    val avgSessionDuration: Int = 0, // w minutach
    val affirmationsUsage: Float = 0f, // 0.0 - 1.0
    val analyticsInterest: Float = 0f, // 0.0 - 1.0
    val featureUsagePattern: Map<String, Float> = emptyMap()
)

