package pl.soulsnaps.features.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.features.integration.AppScopeIntegration
import pl.soulsnaps.features.auth.model.*

/**
 * Ekran zarządzania subskrypcją - pokazuje status użytkownika i opcje upgrade'u
 */
@Composable
fun SubscriptionScreen(
    appScopeIntegration: AppScopeIntegration,
    userId: String,
    modifier: Modifier = Modifier
) {
    var userStatus by remember { mutableStateOf<UserAppStatus?>(null) }
    var userLimits by remember { mutableStateOf<UserLimitsStatus?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load user data
    LaunchedEffect(userId) {
        try {
            userStatus = appScopeIntegration.getUserStatus(userId)
            userLimits = appScopeIntegration.checkUserLimits(userId)
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        SubscriptionHeader(userStatus)
        
        // Current Plan Info
        userStatus?.let { status ->
            when (status) {
                is UserAppStatus.Active -> {
                    CurrentPlanCard(status.userScope)
                    UsageLimitsCard(userLimits)
                    FeatureAccessCard(status.featureSummary)
                    UpgradeRecommendationsCard(
                        recommendations = status.upgradeRecommendations,
                        onUpgrade = { plan ->
                            selectedPlan = plan
                            showUpgradeDialog = true
                        }
                    )
                }
                is UserAppStatus.NotFound -> {
                    NoSubscriptionCard(
                        onSubscribe = { plan ->
                            selectedPlan = plan
                            showUpgradeDialog = true
                        }
                    )
                }
                is UserAppStatus.Inactive -> {
                    InactiveSubscriptionCard(
                        onRenew = { plan ->
                            selectedPlan = plan
                            showUpgradeDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Upgrade Dialog
    if (showUpgradeDialog && selectedPlan != null) {
        val currentPlan = (userStatus as? UserAppStatus.Active)?.userScope?.subscriptionPlan
        val restrictionInfo = FeatureRestrictionInfo(
            feature = FeatureCategory.MEMORY_ANALYSIS,
            restrictionType = RestrictionType.UPGRADE_REQUIRED,
            message = "Uaktualnij plan, aby uzyskać dostęp do wszystkich funkcji",
            requiredAction = RequiredAction.UPGRADE_PLAN,
            currentPlan = currentPlan,
            recommendedPlan = selectedPlan
        )
        
        UpgradeDialog(
            featureRestrictionInfo = restrictionInfo,
            onUpgrade = { newPlan ->
                // Handle upgrade
                // Note: These suspend functions should be called from a coroutine context
                // For now, we'll handle this in the LaunchedEffect
            },
            onDismiss = {
                showUpgradeDialog = false
                selectedPlan = null
            }
        )
    }
}

@Composable
private fun SubscriptionHeader(userStatus: UserAppStatus?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💎 Twoja Subskrypcja",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (userStatus) {
                is UserAppStatus.Active -> {
                    Text(
                        text = "Plan: ${getPlanDisplayName(userStatus.userScope.subscriptionPlan)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Status: Aktywny",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is UserAppStatus.NotFound -> {
                    Text(
                        text = "Brak aktywnej subskrypcji",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Wybierz plan, aby rozpocząć",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is UserAppStatus.Inactive -> {
                    Text(
                        text = "Subskrypcja nieaktywna",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Odnów plan, aby kontynuować",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                null -> {
                    Text(
                        text = "Ładowanie...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentPlanCard(userScope: UserScope) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Aktualny Plan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Plan:")
                Text(
                    text = getPlanDisplayName(userScope.subscriptionPlan),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rola:")
                Text(
                    text = getRoleDisplayName(userScope.role),
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (userScope.validUntil != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ważny do:")
                    Text(
                        text = formatDate(userScope.validUntil),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageLimitsCard(userLimits: UserLimitsStatus?) {
    if (userLimits !is UserLimitsStatus.Active) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Limity Użycia",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            LimitProgressBar(
                label = "Wspomnienia",
                current = userLimits.memories.current,
                limit = userLimits.memories.limit,
                percentage = userLimits.memories.percentage
            )
            
            LimitProgressBar(
                label = "Miejsce",
                current = userLimits.storage.current.toInt(),
                limit = userLimits.storage.limit,
                percentage = userLimits.storage.percentage,
                unit = "GB"
            )
            
            LimitProgressBar(
                label = "Analizy dziennie",
                current = userLimits.dailyAnalysis.current,
                limit = userLimits.dailyAnalysis.limit,
                percentage = userLimits.dailyAnalysis.percentage
            )
            
            LimitProgressBar(
                label = "Eksporty miesięcznie",
                current = userLimits.monthlyExports.current,
                limit = userLimits.monthlyExports.limit,
                percentage = userLimits.monthlyExports.percentage
            )
        }
    }
}

@Composable
private fun LimitProgressBar(
    label: String,
    current: Int,
    limit: Int,
    percentage: Float,
    unit: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp
            )
            Text(
                text = "$current/$limit$unit",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        LinearProgressIndicator(
            progress = (percentage / 100f).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth(),
            color = when {
                percentage >= 90 -> MaterialTheme.colorScheme.error
                percentage >= 70 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun FeatureAccessCard(featureSummary: FeatureAccessSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dostęp do Funkcji",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (featureSummary.accessibleFeatures.isNotEmpty()) {
                Text(
                    text = "Dostępne (${featureSummary.accessibleFeatures.size}):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                featureSummary.accessibleFeatures.forEach { feature: FeatureCategory ->
                    Text(
                        text = "✓ ${getFeatureDisplayName(feature)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (featureSummary.restrictedFeatures.isNotEmpty()) {
                Text(
                    text = "Ograniczone (${featureSummary.restrictedFeatures.size}):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                featureSummary.restrictedFeatures.forEach { feature: FeatureCategory ->
                    Text(
                        text = "✗ ${getFeatureDisplayName(feature)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeRecommendationsCard(
    recommendations: List<UpgradeRecommendation>,
    onUpgrade: (SubscriptionPlan) -> Unit
) {
    if (recommendations.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Rekomendacje Upgrade'u",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            recommendations.forEach { recommendation: UpgradeRecommendation ->
                UpgradeRecommendationCard(
                    recommendation = recommendation,
                    onUpgrade = onUpgrade
                )
            }
        }
    }
}

@Composable
private fun UpgradeRecommendationCard(
    recommendation: UpgradeRecommendation,
    onUpgrade: (SubscriptionPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getFeatureDisplayName(recommendation.feature),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Button(
                    onClick = { onUpgrade(recommendation.recommendedPlan) },
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Uaktualnij",
                        fontSize = 12.sp
                    )
                }
            }
            
            Text(
                text = recommendation.reason,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Zalecany plan: ${getPlanDisplayName(recommendation.recommendedPlan)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NoSubscriptionCard(onSubscribe: (SubscriptionPlan) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎯 Rozpocznij z SoulSnaps",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Wybierz plan, który najlepiej pasuje do Twoich potrzeb",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = { onSubscribe(SubscriptionPlan.BASIC) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rozpocznij z planem Basic")
            }
        }
    }
}

@Composable
private fun InactiveSubscriptionCard(onRenew: (SubscriptionPlan) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚠️ Subskrypcja Nieaktywna",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Twoja subskrypcja wygasła. Odnów plan, aby kontynuować korzystanie z wszystkich funkcji.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = { onRenew(SubscriptionPlan.PREMIUM) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Odnów Subskrypcję")
            }
        }
    }
}

// Helper functions
private fun getRoleDisplayName(role: UserRole): String {
    return when (role) {
        UserRole.FREE_USER -> "Darmowy użytkownik"
        UserRole.PREMIUM_USER -> "Premium użytkownik"
        UserRole.FAMILY_USER -> "Rodzinny użytkownik"
        UserRole.ENTERPRISE_USER -> "Firmowy użytkownik"
        UserRole.ADMIN -> "Administrator"
        UserRole.MODERATOR -> "Moderator"
    }
}

private fun formatDate(timestamp: Long): String {
    // Simple date formatting - in real app use proper date formatting
    return "Data: $timestamp"
}
