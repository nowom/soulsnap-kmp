package pl.soulsnaps.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.utils.toLocalDateTime

@Composable
fun GreetingCard(
    userName: String = "Użytkowniku",
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dzień dobry, $userName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getCurrentTimeMillis().toLocalDateTime().date.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColorScheme.primaryContainer)
            ) {
                Text(
                    text = "🔔",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun AffirmationOfTheDayCard(
    affirmation: String,
    isPlaying: Boolean = false,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onChangeClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onNavigateToAffirmations: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Afirmacja dnia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onSecondaryContainer
                )
                if (isOffline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColorScheme.errorContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (affirmation.isNotEmpty()) {
                Text(
                    text = affirmation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        // TODO: Copy to clipboard
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = if (isPlaying) onPauseClick else onPlayClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColorScheme.primary)
                        ) {
                            Text(
                                text = if (isPlaying) "⏸️" else "▶️",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "Pauza" else "Odtwórz",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColorScheme.onSecondaryContainer
                        )
                    }
                    
                    Row {
                        IconButton(
                            onClick = onChangeClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "🔄",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Brak afirmacji na dziś",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Przejdź do Afirmacji",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToAffirmations() }
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTodayCard(
    emotion: String,
    emoji: String,
    description: String,
    modifier: Modifier = Modifier,
    onQuizClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Twoje emocje dziś",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = emotion,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColorScheme.onPrimaryContainer
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Zrób szybki quiz",
                style = MaterialTheme.typography.labelMedium,
                color = AppColorScheme.primary,
                modifier = Modifier.clickable { onQuizClick() }
            )
        }
    }
}

@Composable
fun LastSnapCard(
    lastSnap: Memory?,
    monthlyUsage: Int = 0,
    monthlyLimit: Int = 30,
    modifier: Modifier = Modifier,
    onSnapClick: (Memory) -> Unit = {},
    onAddFirstSnap: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ostatni SoulSnap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onSurface
                )
                if (monthlyLimit > 0) {
                    Text(
                        text = "$monthlyUsage/$monthlyLimit w tym miesiącu",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (lastSnap != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSnapClick(lastSnap) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    AsyncImage(
                        model = lastSnap.imageUrl,
                        contentDescription = "Ostatni Snap",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = lastSnap.title.ifEmpty { "Bez tytułu" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = AppColorScheme.onSurface
                        )
                        Text(
                            text = lastSnap.description.ifEmpty { "Bez opisu" },
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Emotion chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = lastSnap.mood?.name?: "".ifEmpty { "😊" },
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nie masz jeszcze wspomnień",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dodaj pierwsze ✨",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColorScheme.primary,
                        modifier = Modifier.clickable { onAddFirstSnap() }
                    )
                }
            }
        }
    }
}

@Composable
fun BiofeedbackStrip(
    heartRate: String? = null,
    sleep: String? = null,
    steps: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (heartRate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tętno: $heartRate",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (sleep != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "😴",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sen: $sleep",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (steps != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👣",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kroki: $steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuickShortcutsRow(
    modifier: Modifier = Modifier,
    onSnapsClick: () -> Unit = {},
    onAffirmationsClick: () -> Unit = {},
    onExercisesClick: () -> Unit = {},
    onDailyQuizClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickShortcutButton(
            icon = "📸",
            text = "Snaps",
            onClick = onSnapsClick
        )
        QuickShortcutButton(
            icon = "✨",
            text = "Afirmacje",
            onClick = onAffirmationsClick
        )
        QuickShortcutButton(
            icon = "🧘",
            text = "Ćwiczenia",
            onClick = onExercisesClick
        )
        QuickShortcutButton(
            icon = "🧠",
            text = "Quiz",
            onClick = onDailyQuizClick
        )
    }
}

@Composable
private fun QuickShortcutButton(
    icon: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AppColorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
