package pl.soulsnaps.features.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * CapacityAnalyticsScreen - ekran wyświetlający analitykę wykorzystania limitów
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacityAnalyticsScreen(
    analytics: CapacityAnalytics,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val usageStats by analytics.usageStats.collectAsState()
    val alerts by analytics.alerts.collectAsState()
    val trends by analytics.trends.collectAsState()
    
    // Auto-refresh stats
    LaunchedEffect(Unit) {
        analytics.updateUsageStats("current_user") // TODO: get real user ID
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analityka Wykorzystania",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Usage Overview Cards
        UsageOverviewSection(usageStats)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Alerts Section
        if (alerts.isNotEmpty()) {
            AlertsSection(alerts) { alertId ->
                analytics.clearAlert(alertId)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Detailed Usage Section
        DetailedUsageSection(usageStats)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Trends Section
        TrendsSection(trends)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recommendations Section
        RecommendationsSection(usageStats)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun UsageOverviewSection(usageStats: CapacityUsageStats) {
    Text(
        text = "Przegląd Wykorzystania",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UsageCard(
            title = "Snapy",
            percentage = usageStats.usagePercentages.snaps,
            icon = Icons.Default.PhotoCamera,
            modifier = Modifier.weight(1f)
        )
        
        UsageCard(
            title = "Storage",
            percentage = usageStats.usagePercentages.storage,
            icon = Icons.Default.Storage,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UsageCard(
            title = "AI Analizy",
            percentage = usageStats.usagePercentages.aiAnalysis,
            icon = Icons.Default.Psychology,
            modifier = Modifier.weight(1f)
        )
        
        UsageCard(
            title = "Wspomnienia",
            percentage = usageStats.usagePercentages.memories,
            icon = Icons.Default.Memory,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UsageCard(
    title: String,
    percentage: Double?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val percentageValue = percentage ?: 0.0
            val color = when {
                percentageValue >= 90 -> Color.Red
                percentageValue >= 75 -> Color(0xFFFF8C00) // Orange
                percentageValue >= 50 -> Color(0xFFFFD700) // Yellow
                else -> Color.Green
            }
            
            Text(
                text = "${percentageValue.toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            LinearProgressIndicator(
                progress = (percentageValue / 100f).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: List<CapacityAlert>,
    onDismiss: (String) -> Unit
) {
    Text(
        text = "Alerty",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    alerts.forEach { alert ->
        AlertCard(alert = alert, onDismiss = onDismiss)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AlertCard(
    alert: CapacityAlert,
    onDismiss: (String) -> Unit
) {
    val backgroundColor = when (alert.type) {
        AlertType.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        AlertType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
    }
    
    val textColor = when (alert.type) {
        AlertType.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
        AlertType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${alert.percentage.toInt()}% wykorzystane",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = { onDismiss(alert.id) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textColor
                )
            }
        }
    }
}

@Composable
private fun DetailedUsageSection(usageStats: CapacityUsageStats) {
    Text(
        text = "Szczegółowe Wykorzystanie",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    usageStats.capacityInfo?.let { capacityInfo ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                UsageDetailRow("Snapy", capacityInfo.snaps)
                UsageDetailRow("Storage (GB)", capacityInfo.storage)
                UsageDetailRow("AI Analizy", capacityInfo.aiAnalysis)
                UsageDetailRow("Wspomnienia", capacityInfo.memories)
            }
        }
    }
}

@Composable
private fun UsageDetailRow(
    label: String,
    quotaInfo: pl.soulsnaps.features.auth.mvp.guard.QuotaInfo?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "${quotaInfo?.current ?: 0}/${quotaInfo?.limit ?: 0}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrendsSection(trends: CapacityTrends) {
    Text(
        text = "Trendy",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ostatnia aktualizacja: ${formatTimestamp(trends.lastUpdate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Historia: ${trends.usageHistory.size} wpisów",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendationsSection(usageStats: CapacityUsageStats) {
    Text(
        text = "Rekomendacje",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    usageStats.upgradeRecommendation?.let { recommendation ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Upgrade Rekomendowany",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                recommendation.recommendedPlan?.let { plan ->
                    Text(
                        text = "Plan: $plan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Pilność: ${recommendation.urgency.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (recommendation.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Powody:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    recommendation.recommendations.forEach { reason ->
                        Text(
                            text = "• $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}.${localDateTime.monthNumber.toString().padStart(2, '0')}.${localDateTime.year} ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}
