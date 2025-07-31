package pl.soulsnaps.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a single emotion in Plutchik's Wheel of Emotions.
 * Can be a basic emotion at a specific intensity level or a complex emotion (diad/triad).
 */
data class Emotion(
    val id: String, // Unique identifier (e.g., "JOY_MEDIUM", "LOVE")
    val name: String, // Emotion name in Polish
    val emotionCategory: EmotionCategory? = null, // Basic emotion if this is a basic emotion
    val intensity: EmotionIntensity? = null, // Intensity level if this is a basic emotion
    val primaryEmotion1: EmotionCategory? = null, // First primary emotion for diad/triad
    val primaryEmotion2: EmotionCategory? = null, // Second primary emotion for diad/triad
    val description: String, // Short description of the emotion
    val examples: List<String>, // Example situations where this emotion might be felt
    val color: Color // Color representing the emotion on the wheel
)

/**
 * Extension function to lighten a color.
 * @param factor Lightening factor (0.0f - no change, 1.0f - to white).
 */
fun Color.lighter(factor: Float = 0.2f): Color {
    val red = (this.red + (1f - this.red) * factor).coerceIn(0f, 1f)
    val green = (this.green + (1f - this.green) * factor).coerceIn(0f, 1f)
    val blue = (this.blue + (1f - this.blue) * factor).coerceIn(0f, 1f)
    return Color(red, green, blue, this.alpha)
}

/**
 * Extension function to darken a color.
 * @param factor Darkening factor (0.0f - no change, 1.0f - to black).
 */
fun Color.darker(factor: Float = 0.2f): Color {
    val red = (this.red * (1f - factor)).coerceIn(0f, 1f)
    val green = (this.green * (1f - factor)).coerceIn(0f, 1f)
    val blue = (this.blue * (1f - factor)).coerceIn(0f, 1f)
    return Color(red, green, blue, this.alpha)
}

/**
 * Linear interpolation between two colors.
 * @param start Starting color.
 * @param end Ending color.
 * @param fraction Proportion (0.0f - start, 1.0f - end).
 */
fun Color.Lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, end.red, fraction),
        green = lerp(start.green, end.green, fraction),
        blue = lerp(start.blue, end.blue, fraction),
        alpha = lerp(start.alpha, end.alpha, fraction)
    )
}

/**
 * Linear interpolation for float values.
 */
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}