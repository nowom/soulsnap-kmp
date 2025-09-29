package pl.soulsnaps.features.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.CaptionText
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.components.TitleText
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.DefaultPlans
import pl.soulsnaps.access.manager.PlanDefinition
import pl.soulsnaps.access.manager.PlanPricing
import pl.soulsnaps.access.model.PlanType

@Composable
fun UpgradeScreen(
    onBack: () -> Unit,
    onUpgradeToPlan: (String) -> Unit,
    onDismiss: () -> Unit,
    currentPlan: String,
    recommendations: List<UpgradeRecommendation> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    onClearError: (() -> Unit)? = null,
    planRegistry: PlanRegistryReader = koinInject()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        UpgradeHeader(
            onBack = onBack,
            onDismiss = onDismiss
        )
        
        // Content
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Error message
            errorMessage?.let { message ->
                ErrorSection(
                    message = message,
                    onRetry = onRetry,
                    onClearError = onClearError
                )
            }
            
            // Loading state
            if (isLoading) {
                LoadingSection()
            } else {
                // Current plan info
                CurrentPlanSection(currentPlan = currentPlan)
                
                // Recommendations
                if (recommendations.isNotEmpty()) {
                    RecommendationsSection(
                        recommendations = recommendations,
                        onUpgradeToPlan = onUpgradeToPlan
                    )
                }
                
                // Plan comparison
                PlanComparisonSection(
                    currentPlan = currentPlan,
                    onUpgradeToPlan = onUpgradeToPlan,
                    planRegistry = planRegistry
                )
                
                // Why upgrade
                WhyUpgradeSection()
                
                // FAQ
                FAQSection()
            }
        }
    }
}

@Composable
private fun UpgradeHeader(
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Wróć",
                tint = AppColorScheme.onSurface
            )
        }
        
        HeadingText(
            text = "Ulepsz swój plan",
            color = AppColorScheme.onSurface
        )
        
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Zamknij",
                tint = AppColorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CurrentPlanSection(currentPlan: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Twój aktualny plan",
                style = MaterialTheme.typography.titleMedium,
                color = AppColorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getPlanDisplayName(currentPlan),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            BodyText(
                text = getPlanDescription(currentPlan),
                color = AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendationsSection(
    recommendations: List<UpgradeRecommendation>,
    onUpgradeToPlan: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeadingText(
            text = "💡 Rekomendacje dla Ciebie",
            color = AppColorScheme.onSurface
        )
        
        recommendations.take(3).forEach { recommendation ->
            RecommendationCard(
                recommendation = recommendation,
                onUpgrade = { onUpgradeToPlan(recommendation.recommendedPlan) }
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: UpgradeRecommendation,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.urgency) {
                Urgency.CRITICAL -> Color(0xFFFFEBEE)
                Urgency.HIGH -> Color(0xFFFFF3E0)
                Urgency.MEDIUM -> Color(0xFFE8F5E8)
                Urgency.LOW -> AppColorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    TitleText(
                        text = recommendation.title,
                        color = AppColorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    BodyText(
                        text = recommendation.description,
                        color = AppColorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Benefits
                    recommendation.benefits.take(3).forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Green
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            CaptionText(
                                text = benefit,
                                color = AppColorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                
                // Urgency indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (recommendation.urgency) {
                                Urgency.CRITICAL -> Color.Red
                                Urgency.HIGH -> Color(0xFFFF9800)
                                Urgency.MEDIUM -> Color.Green
                                Urgency.LOW -> Color.Gray
                            }
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActionButton(
                icon = "⬆️",
                text = "Ulepsz do ${getPlanDisplayName(recommendation.recommendedPlan)}",
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlanComparisonSection(
    currentPlan: String,
    onUpgradeToPlan: (String) -> Unit,
    planRegistry: PlanRegistryReader
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Porównanie planów",
            color = AppColorScheme.onSurface
        )
        
        // Available plans
        val availablePlans = listOf(
            PlanType.GUEST.name,
            PlanType.FREE_USER.name,
            PlanType.PREMIUM_USER.name
        )
        
        availablePlans.forEach { plan ->
            if (plan != currentPlan) {
                PlanCard(
                    planName = plan,
                    isCurrentPlan = plan == currentPlan,
                    onUpgrade = { onUpgradeToPlan(plan) },
                    planRegistry = planRegistry
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    planName: String,
    isCurrentPlan: Boolean,
    onUpgrade: () -> Unit,
    planRegistry: PlanRegistryReader
) {
    var planDefinition by remember { mutableStateOf<PlanDefinition?>(null) }
    var pricing by remember { mutableStateOf<PlanPricing?>(null) }
    
    LaunchedEffect(planName) {
        planDefinition = planRegistry.getPlan(planName)
        pricing = planDefinition?.pricing
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan) {
                AppColorScheme.primary.copy(alpha = 0.1f)
            } else {
                AppColorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    TitleText(
                        text = getPlanDisplayName(planName),
                        color = AppColorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    BodyText(
                        text = getPlanDescription(planName),
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                if (isCurrentPlan) {
                    Text(
                        text = "AKTUALNY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pricing
            pricing?.let { price ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            price.monthlyPrice != null -> "${price.monthlyPrice} ${price.currency}/miesiąc"
                            price.yearlyPrice != null -> "${price.yearlyPrice} ${price.currency}/rok"
                            price.lifetimePrice != null -> "${price.lifetimePrice} ${price.currency} jednorazowo"
                            else -> "Darmowy"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColorScheme.onSurface
                    )
                    
                    if (price.monthlyPrice == 0f) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Key features
            planDefinition?.let { plan ->
                val keyFeatures = listOf(
                    "SoulSnaps: ${plan.quotas["snaps.capacity"] ?: "∞"}",
                    "AI analiza: ${plan.quotas["ai.daily"] ?: "∞"}/dzień",
                    "Przestrzeń: ${plan.quotas["storage.gb"] ?: "∞"} GB"
                )
                
                keyFeatures.forEach { feature ->
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
                        CaptionText(
                            text = feature,
                            color = AppColorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isCurrentPlan) {
                ActionButton(
                    icon = "🚀",
                    text = "Wybierz plan",
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WhyUpgradeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            HeadingText(
                text = "Dlaczego warto ulepszyć?",
                color = AppColorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val benefits = listOf(
                "🚀 Nielimitowane funkcje",
                "📊 Zaawansowana analityka",
                "🎯 Personalizowane treści",
                "💾 Więcej miejsca na dane",
                "🔒 Priorytetowe wsparcie",
                "⚡ Funkcje eksperymentalne"
            )
            
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FAQSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            HeadingText(
                text = "Często zadawane pytania",
                color = AppColorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val faqs = listOf(
                "Q: Czy mogę zmienić plan w dowolnym momencie?" to "A: Tak, możesz zmienić plan w każdej chwili.",
                "Q: Co się stanie z moimi danymi po upgrade?" to "A: Wszystkie Twoje dane zostaną zachowane.",
                "Q: Czy mogę anulować subskrypcję?" to "A: Tak, możesz anulować w każdej chwili.",
                "Q: Czy są ukryte opłaty?" to "A: Nie, wszystkie opłaty są jasno określone."
            )
            
            faqs.forEach { (question, answer) ->
                Column {
                    TitleText(
                        text = question,
                        color = AppColorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BodyText(
                        text = answer,
                        color = AppColorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// Helper functions
private fun getPlanDisplayName(planName: String): String {
    return when (planName) {
        PlanType.GUEST.name -> "Gość"
        PlanType.FREE_USER.name -> "Użytkownik Darmowy"
        PlanType.PREMIUM_USER.name -> "Użytkownik Premium"
        PlanType.ENTERPRISE_USER.name -> "Użytkownik Enterprise"
        else -> planName
    }
}

private fun getPlanDescription(planName: String): String {
    return when (planName) {
        PlanType.GUEST.name -> "Podstawowy dostęp z ograniczonymi funkcjami"
        PlanType.FREE_USER.name -> "Darmowy plan z podstawowymi funkcjami"
        PlanType.PREMIUM_USER.name -> "Premium plan ze wszystkimi funkcjami"
        PlanType.ENTERPRISE_USER.name -> "Enterprise plan z nieograniczonym dostępem"
        else -> "Plan użytkownika"
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: (() -> Unit)?,
    onClearError: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.error.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wystąpił błąd",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColorScheme.error
                )
                onClearError?.let { clearError ->
                    IconButton(onClick = clearError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Zamknij",
                            tint = AppColorScheme.error
                        )
                    }
                }
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColorScheme.onSurface
            )
            
            onRetry?.let { retry ->
                ActionButton(
                    icon = "🪞",
                    text = "Spróbuj ponownie",
                    onClick = retry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple loading indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "...",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColorScheme.primary
                )
            }
            
            Text(
                text = "Ładowanie planów...",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
