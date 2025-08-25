package pl.soulsnaps.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.DashboardCard
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.components.SubtitleText
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.CaptionText
import pl.soulsnaps.components.TitleText
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.features.auth.mvp.guard.UserPlanManager
import pl.soulsnaps.features.auth.mvp.guard.PlanRegistryReader
import pl.soulsnaps.features.auth.mvp.guard.DefaultPlans
import pl.soulsnaps.features.auth.mvp.guard.model.PlanType
import pl.soulsnaps.features.upgrade.UpgradeRecommendationEngine
import pl.soulsnaps.features.upgrade.UsageStatistics
import pl.soulsnaps.features.upgrade.UserBehavior

@Composable
fun ScopeAwareDashboard(
    onAddNewSnap: () -> Unit = {},
    onNavigateToSoulSnaps: () -> Unit = {},
    onNavigateToAffirmations: () -> Unit = {},
    onNavigateToExercises: () -> Unit = {},
    onNavigateToVirtualMirror: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onUpgradePlan: () -> Unit = {},
    userPlanManager: UserPlanManager = UserPlanManager(),
    planRegistry: PlanRegistryReader = DefaultPlans
) {
    val currentPlan by userPlanManager.currentPlan.collectAsState(initial = null)
    val planDefinition = currentPlan?.let { planRegistry.getPlan(it) }
    
    // Upgrade recommendation engine
    val upgradeEngine = remember { UpgradeRecommendationEngine(planRegistry) }
    val recommendations by upgradeEngine.recommendations.collectAsState(initial = emptyList())
    
    // Generate recommendations when current plan changes
    LaunchedEffect(currentPlan) {
        currentPlan?.let { plan ->
            // Mock usage statistics and user behavior for demo
            val usageStats = UsageStatistics(
                soulSnapsCount = 8, // 80% of 10 limit for GUEST
                aiAnalysisCount = 1, // 100% of 1 limit for GUEST
                affirmationsCount = 15,
                exercisesCount = 8,
                storageUsedGB = 0.8f,
                lastActivityDate = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
            
            val userBehavior = UserBehavior(
                dailyActiveDays = 25, // Power user
                avgSessionDuration = 20, // 20 minutes
                affirmationsUsage = 0.9f, // High usage
                analyticsInterest = 0.7f, // Interested in analytics
                featureUsagePattern = mapOf(
                    "affirmations" to 0.9f,
                    "exercises" to 0.8f,
                    "analytics" to 0.7f
                )
            )
            
            upgradeEngine.analyzeUserAndGenerateRecommendations(plan, usageStats, userBehavior)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with plan info
        PlanAwareHeader(currentPlan = currentPlan, planDefinition = planDefinition)
        
        // Plan-specific features
        PlanFeaturesSection(
            currentPlan = currentPlan,
            planDefinition = planDefinition,
            onAddNewSnap = onAddNewSnap,
            onNavigateToSoulSnaps = onNavigateToSoulSnaps,
            onNavigateToAffirmations = onNavigateToAffirmations,
            onNavigateToExercises = onNavigateToExercises,
            onNavigateToVirtualMirror = onNavigateToVirtualMirror,
            onNavigateToAnalytics = onNavigateToAnalytics
        )
        
        // Upgrade recommendations
        if (recommendations.isNotEmpty()) {
            UpgradeRecommendationSection(
                recommendations = recommendations,
                onUpgrade = onUpgradePlan
            )
        }
        
        // Usage statistics
        UsageStatisticsSection(
            currentPlan = currentPlan,
            planDefinition = planDefinition
        )
    }
}

@Composable
private fun PlanAwareHeader(
    currentPlan: String?,
    planDefinition: pl.soulsnaps.features.auth.mvp.guard.PlanDefinition?
) {
    DashboardCard(
        title = "Twój plan: ${getPlanDisplayName(currentPlan) ?: "Ładowanie..."}",
        subtitle = getPlanDescription(currentPlan) ?: ""
    ) {
        Column {
            if (currentPlan != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        TitleText(
                            text = getPlanDisplayName(currentPlan) ?: currentPlan,
                            color = AppColorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BodyText(
                            text = getPlanDescription(currentPlan) ?: "",
                            color = AppColorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Plan badge
                    Text(
                        text = currentPlan,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (currentPlan) {
                            PlanType.GUEST.name -> AppColorScheme.error
                            PlanType.FREE_USER.name -> AppColorScheme.primary
                            PlanType.PREMIUM_USER.name -> AppColorScheme.secondary
                            else -> AppColorScheme.onSurfaceVariant
                        }
                    )
                }
            } else {
                BodyText(
                    text = "Ładowanie planu...",
                    color = AppColorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlanFeaturesSection(
    currentPlan: String?,
    planDefinition: pl.soulsnaps.features.auth.mvp.guard.PlanDefinition?,
    onAddNewSnap: () -> Unit,
    onNavigateToSoulSnaps: () -> Unit,
    onNavigateToAffirmations: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToVirtualMirror: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    DashboardCard(
        title = "Dostępne funkcje",
        subtitle = "Funkcje dostępne w Twoim planie"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Basic features (available in all plans)
            ActionButton(
                icon = "📸",
                text = "Dodaj SoulSnap",
                onClick = onAddNewSnap,
                modifier = Modifier.fillMaxWidth()
            )
            
            ActionButton(
                icon = "📚",
                text = "Przeglądaj SoulSnaps",
                onClick = onNavigateToSoulSnaps,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Plan-specific features
            when (currentPlan) {
                PlanType.GUEST.name -> {
                    // Guest features
                    ActionButton(
                        icon = "🎧",
                        text = "Afirmacje (ograniczone)",
                        onClick = onNavigateToAffirmations,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ActionButton(
                        icon = "🧠",
                        text = "Ćwiczenia (podstawowe)",
                        onClick = onNavigateToExercises,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                PlanType.FREE_USER.name -> {
                    // Free user features
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            icon = "🎧",
                            text = "Afirmacje",
                            onClick = onNavigateToAffirmations,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = "🧠",
                            text = "Ćwiczenia",
                            onClick = onNavigateToExercises,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    ActionButton(
                        icon = "🪞",
                        text = "Wirtualne Lustro",
                        onClick = onNavigateToVirtualMirror,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ActionButton(
                        icon = "📊",
                        text = "Podstawowa analityka",
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                PlanType.PREMIUM_USER.name -> {
                    // Premium features
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            icon = "🎧",
                            text = "Afirmacje Premium",
                            onClick = onNavigateToAffirmations,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = "🧠",
                            text = "Ćwiczenia Premium",
                            onClick = onNavigateToExercises,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            icon = "🪞",
                            text = "Wirtualne Lustro",
                            onClick = onNavigateToVirtualMirror,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = "📊",
                            text = "Pełna analityka",
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    ActionButton(
                        icon = "🚀",
                        text = "Funkcje eksperymentalne",
                        onClick = { /* TODO: Premium features */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                else -> {
                    // Loading state
                    BodyText(
                        text = "Ładowanie funkcji...",
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeRecommendationSection(
    recommendations: List<pl.soulsnaps.features.upgrade.UpgradeRecommendation>,
    onUpgrade: () -> Unit
) {
    DashboardCard(
        title = "💡 Rekomendacje dla Ciebie",
        subtitle = "Odkryj pełny potencjał SoulSnaps"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recommendations.take(2).forEach { recommendation ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    BodyText(
                        text = recommendation.title,
                        color = AppColorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ActionButton(
                icon = "⬆️",
                text = "Zobacz wszystkie rekomendacje",
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UsageStatisticsSection(
    currentPlan: String?,
    planDefinition: pl.soulsnaps.features.auth.mvp.guard.PlanDefinition?
) {
    DashboardCard(
        title = "📊 Statystyki użycia",
        subtitle = "Jak wykorzystujesz swój plan"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (planDefinition != null) {
                // Show plan limits and usage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BodyText(
                        text = "SoulSnaps:",
                        color = AppColorScheme.onSurface
                    )
                    BodyText(
                        text = "0 / ${planDefinition.quotas["snaps.capacity"] ?: "∞"}",
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BodyText(
                        text = "Analiza AI dziennie:",
                        color = AppColorScheme.onSurface
                    )
                    BodyText(
                        text = "0 / ${planDefinition.quotas["ai.daily"] ?: "∞"}",
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BodyText(
                        text = "Przestrzeń dyskowa:",
                        color = AppColorScheme.onSurface
                    )
                    BodyText(
                        text = "0 GB / ${planDefinition.quotas["storage.gb"] ?: "∞"} GB",
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            } else {
                BodyText(
                    text = "Ładowanie statystyk...",
                    color = AppColorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper functions
private fun getPlanDisplayName(planName: String?): String? {
    return when (planName) {
        PlanType.GUEST.name -> "Gość"
        PlanType.FREE_USER.name -> "Użytkownik Darmowy"
        PlanType.PREMIUM_USER.name -> "Użytkownik Premium"
        PlanType.ENTERPRISE_USER.name -> "Użytkownik Enterprise"
        else -> planName
    }
}

private fun getPlanDescription(planName: String?): String? {
    return when (planName) {
        PlanType.GUEST.name -> "Podstawowy dostęp z ograniczonymi funkcjami"
        PlanType.FREE_USER.name -> "Darmowy plan z podstawowymi funkcjami"
        PlanType.PREMIUM_USER.name -> "Premium plan ze wszystkimi funkcjami"
        PlanType.ENTERPRISE_USER.name -> "Enterprise plan z nieograniczonym dostępem"
        else -> "Plan użytkownika"
    }
}
