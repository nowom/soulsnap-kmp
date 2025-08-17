package pl.soulsnaps.features.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.features.auth.model.*

/**
 * Dialog upgrade'u planu - pokazuje gdy użytkownik próbuje uzyskać dostęp do funkcji premium
 */
@Composable
fun UpgradeDialog(
    featureRestrictionInfo: FeatureRestrictionInfo,
    onUpgrade: (SubscriptionPlan) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.width(400.dp),
        title = {
            Text(
                text = "🔒 Funkcja Premium",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature info
                FeatureInfoCard(featureRestrictionInfo)
                
                // Upgrade options
                UpgradeOptionsCard(
                    currentPlan = featureRestrictionInfo.currentPlan,
                    recommendedPlan = featureRestrictionInfo.recommendedPlan,
                    onUpgrade = onUpgrade
                )
                
                // Benefits
                BenefitsCard(featureRestrictionInfo.recommendedPlan)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    featureRestrictionInfo.recommendedPlan?.let { onUpgrade(it) }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Uaktualnij Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Może później",
                    fontSize = 14.sp
                )
            }
        }
    )
}

@Composable
private fun FeatureInfoCard(featureRestrictionInfo: FeatureRestrictionInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Funkcja: ${getFeatureDisplayName(featureRestrictionInfo.feature)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = featureRestrictionInfo.message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (featureRestrictionInfo.currentPlan != null) {
                Text(
                    text = "Twój plan: ${getPlanDisplayName(featureRestrictionInfo.currentPlan)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpgradeOptionsCard(
    currentPlan: SubscriptionPlan?,
    recommendedPlan: SubscriptionPlan?,
    onUpgrade: (SubscriptionPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Dostępne Plany",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Basic Plan
            PlanOptionCard(
                plan = SubscriptionPlan.BASIC,
                isCurrent = currentPlan == SubscriptionPlan.BASIC,
                isRecommended = recommendedPlan == SubscriptionPlan.BASIC,
                price = "9.99 zł/miesiąc",
                onSelect = { onUpgrade(SubscriptionPlan.BASIC) }
            )
            
            // Premium Plan
            PlanOptionCard(
                plan = SubscriptionPlan.PREMIUM,
                isCurrent = currentPlan == SubscriptionPlan.PREMIUM,
                isRecommended = recommendedPlan == SubscriptionPlan.PREMIUM,
                price = "19.99 zł/miesiąc",
                onSelect = { onUpgrade(SubscriptionPlan.PREMIUM) }
            )
            
            // Family Plan
            PlanOptionCard(
                plan = SubscriptionPlan.FAMILY,
                isCurrent = currentPlan == SubscriptionPlan.FAMILY,
                isRecommended = recommendedPlan == SubscriptionPlan.FAMILY,
                price = "29.99 zł/miesiąc",
                onSelect = { onUpgrade(SubscriptionPlan.FAMILY) }
            )
        }
    }
}

@Composable
private fun PlanOptionCard(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    isRecommended: Boolean,
    price: String,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isRecommended -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isRecommended -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent || isRecommended) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getPlanDisplayName(plan),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = price,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isCurrent) {
                    Text(
                        text = "Twój plan",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (isRecommended) {
                    Text(
                        text = "Zalecany",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (!isCurrent) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecommended) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Wybierz",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitsCard(recommendedPlan: SubscriptionPlan?) {
    if (recommendedPlan == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Korzyści z planu ${getPlanDisplayName(recommendedPlan)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            val benefits = getPlanBenefits(recommendedPlan)
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✓",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = benefit,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Helper functions
fun getFeatureDisplayName(feature: FeatureCategory): String {
    return when (feature) {
        FeatureCategory.MEMORY_CAPTURE -> "Przechwytywanie Wspomnień"
        FeatureCategory.MEMORY_ANALYSIS -> "Analiza Wspomnień"
        FeatureCategory.PATTERN_DETECTION -> "Wykrywanie Wzorców"
        FeatureCategory.INSIGHTS -> "Szczegółowe Insights"
        FeatureCategory.SHARING -> "Udostępnianie"
        FeatureCategory.COLLABORATION -> "Współpraca"
        FeatureCategory.EXPORT -> "Eksport Danych"
        FeatureCategory.BACKUP -> "Backup"
        FeatureCategory.CUSTOMIZATION -> "Personalizacja"
        FeatureCategory.ADVANCED_AI -> "Zaawansowane AI"
        FeatureCategory.API_ACCESS -> "Dostęp do API"
        FeatureCategory.SUPPORT -> "Wsparcie"
    }
}

fun getPlanDisplayName(plan: SubscriptionPlan): String {
    return when (plan) {
        SubscriptionPlan.FREE -> "Darmowy"
        SubscriptionPlan.BASIC -> "Basic"
        SubscriptionPlan.PREMIUM -> "Premium"
        SubscriptionPlan.FAMILY -> "Family"
        SubscriptionPlan.ENTERPRISE -> "Enterprise"
        SubscriptionPlan.LIFETIME -> "Lifetime"
    }
}

private fun getPlanBenefits(plan: SubscriptionPlan): List<String> {
    return when (plan) {
        SubscriptionPlan.FREE -> listOf(
            "Podstawowe przechwytywanie wspomnień",
            "Ograniczona analiza"
        )
        SubscriptionPlan.BASIC -> listOf(
            "Rozszerzona analiza wspomnień",
            "Więcej miejsca na dane",
            "Podstawowe insights"
        )
        SubscriptionPlan.PREMIUM -> listOf(
            "Wykrywanie wzorców",
            "Szczegółowe insights",
            "Eksport danych",
            "Backup w chmurze"
        )
        SubscriptionPlan.FAMILY -> listOf(
            "Współpraca rodzinna",
            "Udostępnianie wspomnień",
            "Więcej miejsca",
            "Wszystkie funkcje Premium"
        )
        SubscriptionPlan.ENTERPRISE -> listOf(
            "Zaawansowane AI",
            "Dostęp do API",
            "Priorytetowe wsparcie",
            "Wszystkie funkcje"
        )
        SubscriptionPlan.LIFETIME -> listOf(
            "Dożywotni dostęp",
            "Wszystkie funkcje",
            "Bez miesięcznych opłat",
            "Priorytetowe wsparcie"
        )
    }
}

/**
 * Dialog akcji wymaganej - pokazuje gdy użytkownik próbuje wykonać akcję
 */
@Composable
fun ActionRestrictionDialog(
    actionRestrictionInfo: ActionRestrictionInfo,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.width(400.dp),
        title = {
            Text(
                text = "⚠️ Akcja Ograniczona",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = actionRestrictionInfo.message,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                if (actionRestrictionInfo.currentLimit != null) {
                    Text(
                        text = "Aktualny limit: ${actionRestrictionInfo.currentLimit}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (actionRestrictionInfo.limitType != null) {
                    Text(
                        text = "Typ limitu: ${actionRestrictionInfo.limitType}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpgrade()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = when (actionRestrictionInfo.requiredAction) {
                        RequiredAction.UPGRADE_PLAN -> "Uaktualnij Plan"
                        RequiredAction.RENEW_SUBSCRIPTION -> "Odnów Subskrypcję"
                        RequiredAction.VERIFY_ACCOUNT -> "Zweryfikuj Konto"
                        RequiredAction.WAIT_FOR_RESET -> "Poczekaj na Reset"
                        RequiredAction.CONTACT_SUPPORT -> "Skontaktuj się z Supportem"
                        RequiredAction.PAYMENT_REQUIRED -> "Zapłać"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Anuluj",
                    fontSize = 14.sp
                )
            }
        }
    )
}
