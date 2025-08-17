package pl.soulsnaps.features.capturemoment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

/**
 * Service for applying photo filters and effects
 */
class PhotoFilterService {
    
    /**
     * Get color filter for specific filter types
     */
    fun getColorFilter(filter: PhotoFilter): ColorFilter? {
        return when (filter) {
            PhotoFilter.VINTAGE -> ColorFilter.tint(
                Color(0xFF8B4513), // Saddle brown tint
                blendMode = androidx.compose.ui.graphics.BlendMode.Softlight
            )
            PhotoFilter.WARM -> ColorFilter.tint(
                Color(0xFFFF8C00), // Dark orange tint
                blendMode = androidx.compose.ui.graphics.BlendMode.Softlight
            )
            PhotoFilter.COOL -> ColorFilter.tint(
                Color(0xFF4169E1), // Royal blue tint
                blendMode = androidx.compose.ui.graphics.BlendMode.Softlight
            )
            PhotoFilter.DRAMATIC -> ColorFilter.tint(
                Color(0xFF2F2F2F), // Dark gray tint
                blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
            )
            else -> null
        }
    }
    
    /**
     * Get recommended adjustment values for each filter
     */
    fun getRecommendedAdjustments(filter: PhotoFilter): FilterAdjustments {
        return when (filter) {
            PhotoFilter.NONE -> FilterAdjustments(0f, 1f, 1f)
            PhotoFilter.VINTAGE -> FilterAdjustments(0.1f, 1.2f, 0.7f)
            PhotoFilter.BLACK_WHITE -> FilterAdjustments(0f, 1.3f, 0f)
            PhotoFilter.WARM -> FilterAdjustments(0.05f, 1.1f, 1.2f)
            PhotoFilter.COOL -> FilterAdjustments(-0.05f, 1.1f, 1.1f)
            PhotoFilter.DRAMATIC -> FilterAdjustments(-0.1f, 1.5f, 1.3f)
            PhotoFilter.SOFT -> FilterAdjustments(0.15f, 0.8f, 0.9f)
            PhotoFilter.SHARP -> FilterAdjustments(0.05f, 1.4f, 1.0f)
        }
    }
}

data class FilterAdjustments(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float
)
