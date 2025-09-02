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

/**
 * CapacityAnalyticsScreen - ekran wyświetlający analitykę wykorzystania limitów
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapacityAnalyticsScreen(
    analytics: CapacityAnalytics,
    userId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val usageStats by analytics.usageStats.collectAsState()
    val alerts by analytics.alerts.collectAsState()
    val trends by analytics.trends.collectAsState()
    
    // Auto-refresh stats
    LaunchedEffect(Unit) {
        analytics.updateUsageStats(userId)
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
                analytics.dismissAlert(alertId)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Detailed Usage Section
        usageStats?.let { stats ->
            DetailedUsageSection(stats)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Trends Section
        TrendsSection(trends)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recommendations Section
        val report = analytics.getUsageReport()
        if (report.recommendations.isNotEmpty()) {
            RecommendationsSection(report.recommendations)
        }
    }
}

@Composable
private fun UsageOverviewSection(usageStats: CapacityUsageStats?) {
    if (usageStats == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Przegląd Wykorzystania",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UsageCard(
                title = "Wspomnienia",
                value = "${usageStats.totalMemories}",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f)
            )
            
            UsageCard(
                title = "Zdjęcia",
                value = "${usageStats.totalPhotos}",
                icon = Icons.Default.Photo,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UsageCard(
                title = "Audio",
                value = "${usageStats.totalAudio}",
                icon = Icons.Default.AudioFile,
                modifier = Modifier.weight(1f)
            )
            
            UsageCard(
                title = "Storage",
                value = "${usageStats.storageUsed / (1024 * 1024)} MB",
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UsageCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: List<CapacityAlert>,
    onDismiss: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alerty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        alerts.forEach { alert ->
            AlertCard(alert = alert, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun AlertCard(
    alert: CapacityAlert,
    onDismiss: (String) -> Unit
) {
    val backgroundColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFFEBEE)
        AlertSeverity.WARNING -> Color(0xFFFFF3E0)
        AlertSeverity.INFO -> Color(0xFFE3F2FD)
    }
    
    val iconColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFD32F2F)
        AlertSeverity.WARNING -> Color(0xFFF57C00)
        AlertSeverity.INFO -> Color(0xFF1976D2)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (alert.type) {
                        AlertType.STORAGE_HIGH -> Icons.Default.Storage
                        AlertType.MEMORIES_HIGH -> Icons.Default.Memory
                        AlertType.BACKUP_NEEDED -> Icons.Default.Backup
                    },
                    contentDescription = null,
                    tint = iconColor
                )
                
                Column {
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Ostatnia aktualizacja: ${formatTimestamp(alert.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = { onDismiss(alert.id) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}

@Composable
private fun DetailedUsageSection(stats: CapacityUsageStats) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Szczegółowe Wykorzystanie",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsageDetailRow("Wspomnienia", "${stats.totalMemories}")
                UsageDetailRow("Zdjęcia", "${stats.totalPhotos}")
                UsageDetailRow("Audio", "${stats.totalAudio}")
                UsageDetailRow("Wideo", "${stats.totalVideos}")
                UsageDetailRow("Storage", "${stats.storageUsed / (1024 * 1024)} MB")
                stats.lastBackup?.let { backup ->
                    UsageDetailRow("Ostatni backup", formatTimestamp(backup))
                }
            }
        }
    }
}

@Composable
private fun UsageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrendsSection(trends: List<UsageTrend>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Trendy Wykorzystania",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (trends.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak danych o trendach",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            trends.takeLast(5).forEach { trend ->
                TrendCard(trend = trend)
            }
        }
    }
}

@Composable
private fun TrendCard(trend: UsageTrend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(trend.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                                 Text(
                     text = "Wzrost: ${(trend.growthRate * 10).toInt() / 10.0}%",
                     style = MaterialTheme.typography.bodySmall,
                     color = if (trend.growthRate > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                 )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wspomnienia: ${trend.memoriesCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Storage: ${trend.storageUsed / (1024 * 1024)} MB",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(recommendations: List<UsageRecommendation>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Rekomendacje",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        recommendations.forEach { recommendation ->
            RecommendationCard(recommendation = recommendation)
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: UsageRecommendation) {
    val priorityColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> Color(0xFFD32F2F)
        RecommendationPriority.MEDIUM -> Color(0xFFF57C00)
        RecommendationPriority.LOW -> Color(0xFF1976D2)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = priorityColor),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = recommendation.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // Simple timestamp formatting for KMP common
    val seconds = timestamp / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return "${days}d ${hours % 24}h ${minutes % 60}m"
}
