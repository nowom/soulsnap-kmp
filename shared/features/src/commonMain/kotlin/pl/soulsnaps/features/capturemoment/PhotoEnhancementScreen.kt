package pl.soulsnaps.features.capturemoment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.designsystem.SoulSnapTypography

@Composable
fun PhotoEnhancementScreen(
    photoUri: String?,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<PhotoFilter?>(null) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorScheme.surface)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColorScheme.onSurface
                )
            }
            
            Text(
                text = "Enhance Photo",
                style = SoulSnapTypography.headlineSmall,
                color = AppColorScheme.onSurface
            )
            
            IconButton(onClick = { /* Reset to original */ }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = AppColorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Photo Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColorScheme.surfaceVariant)
        ) {
            if (photoUri != null) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Photo to enhance",
                    modifier = Modifier.fillMaxSize(),
                    tint = AppColorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "No photo",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    tint = AppColorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter Selection
        Text(
            text = "Filters",
            style = SoulSnapTypography.titleMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(PhotoFilter.values()) { filter ->
                FilterOption(
                    filter = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Adjustment Controls
        Text(
            text = "Adjustments",
            style = SoulSnapTypography.titleMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Brightness
        AdjustmentSlider(
            label = "Brightness",
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = -0.5f..0.5f,
            icon = Icons.Default.WbSunny
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contrast
        AdjustmentSlider(
            label = "Contrast",
            value = contrast,
            onValueChange = { contrast = it },
            valueRange = 0.5f..2f,
            icon = Icons.Default.Tune
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Saturation
        AdjustmentSlider(
            label = "Saturation",
            value = saturation,
            onValueChange = { saturation = it },
            valueRange = 0f..2f,
            icon = Icons.Default.Palette
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Save Enhanced",
                onClick = { photoUri?.let { onSave(it) } },
                modifier = Modifier.weight(1f),
                enabled = photoUri != null
            )
        }
    }
}

@Composable
private fun FilterOption(
    filter: PhotoFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) AppColorScheme.primaryContainer
                else AppColorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) AppColorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = filter.displayName,
                tint = if (isSelected) AppColorScheme.primary else AppColorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = filter.displayName,
                style = SoulSnapTypography.labelSmall,
                color = if (isSelected) AppColorScheme.primary else AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = label,
                style = SoulSnapTypography.bodyMedium,
                color = AppColorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = value.toString().take(4),
                style = SoulSnapTypography.bodySmall,
                color = AppColorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AppColorScheme.primary,
                activeTrackColor = AppColorScheme.primary,
                inactiveTrackColor = AppColorScheme.surfaceVariant
            )
        )
    }
}

enum class PhotoFilter(
    val icon: ImageVector,
    val displayName: String
) {
    NONE(Icons.Default.Image, "Original"),
    VINTAGE(Icons.Default.FilterVintage, "Vintage"),
    BLACK_WHITE(Icons.Default.FilterBAndW, "B&W"),
    WARM(Icons.Default.WbSunny, "Warm"),
    COOL(Icons.Default.AcUnit, "Cool"),
    DRAMATIC(Icons.Default.Contrast, "Dramatic"),
    SOFT(Icons.Default.BlurOn, "Soft"),
    SHARP(Icons.Default.Tune, "Sharp")
}
