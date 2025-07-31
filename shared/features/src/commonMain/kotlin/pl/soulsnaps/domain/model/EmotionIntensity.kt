package pl.soulsnaps.domain.model

/**
 * Enum representing the intensity levels of emotions in Plutchik's Wheel.
 * Each basic emotion can have different intensity levels, creating variations of the emotion.
 */
enum class EmotionIntensity {
    LOW,    // Low intensity - milder version of the emotion
    MEDIUM, // Medium intensity - standard version of the emotion
    HIGH    // High intensity - intense version of the emotion
} 