package pl.soulsnaps.domain.model

/**
 * Reprezentuje ćwiczenie wspierające autoregulację emocjonalną.
 * Represents an exercise supporting emotional self-regulation.
 */
data class Exercise(
    val id: String,
    val title: String,
    val description: String,
    val category: EmotionCategory,
    var isCompleted: Boolean = false
)

/**
 * Reprezentuje rytuał dzienny (poranny lub wieczorny).
 * Represents a daily ritual (morning or evening).
 */
data class Ritual(
    val id: String,
    val title: String,
    val description: String,
    val exercises: List<Exercise>
)

/**
 * Fazy sesji oddechowej.
 * Phases of a breathing session.
 */
enum class BreathingPhase {
    INHALE, HOLD, EXHALE, PAUSE
}