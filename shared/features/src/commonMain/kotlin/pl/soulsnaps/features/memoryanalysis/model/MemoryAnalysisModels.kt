package pl.soulsnaps.features.memoryanalysis.model


import pl.soulsnaps.domain.model.MoodType as DomainMoodType

/**
 * Core data models for Memory Analysis & Insights
 */

// Color Analysis
data class ColorAnalysis(
    val dominantColors: List<DominantColor>,
    val colorPalette: List<Color>,
    val brightness: Float, // 0.0 - 1.0
    val saturation: Float, // 0.0 - 1.0
    val contrast: Float, // 0.0 - 1.0
    val temperature: ColorTemperature
)

data class DominantColor(
    val color: Color,
    val percentage: Float, // 0.0 - 1.0
    val position: ColorPosition
)

data class Color(
    val red: Int, // 0-255
    val green: Int, // 0-255
    val blue: Int, // 0-255
    val alpha: Int = 255 // 0-255
)

data class ColorPosition(
    val x: Float, // 0.0 - 1.0
    val y: Float // 0.0 - 1.0
)

enum class ColorTemperature {
    WARM, COOL, NEUTRAL
}

// Face Detection
data class FaceDetection(
    val faces: List<Face>,
    val faceCount: Int,
    val primaryEmotion: Emotion?,
    val confidence: Float
)

data class Face(
    val bounds: FaceBounds,
    val emotions: List<Emotion>,
    val age: AgeRange?,
    val gender: Gender?,
    val confidence: Float
)

data class FaceBounds(
    val x: Float, // 0.0 - 1.0
    val y: Float, // 0.0 - 1.0
    val width: Float, // 0.0 - 1.0
    val height: Float // 0.0 - 1.0
)

data class Emotion(
    val type: EmotionType,
    val confidence: Float
)

enum class EmotionType {
    HAPPY, SAD, ANGRY, SURPRISED, FEARFUL, DISGUSTED, NEUTRAL
}

enum class AgeRange {
    CHILD, TEEN, YOUNG_ADULT, ADULT, SENIOR
}

enum class Gender {
    MALE, FEMALE, UNKNOWN
}

// Mood Analysis - Using domain MoodType
data class MoodAnalysis(
    val primaryMood: DomainMoodType,
    val moodScore: Float, // 0.0 - 1.0
    val confidence: Float,
    val factors: List<MoodFactor>,
    val timestamp: Long
)

data class MoodFactor(
    val type: MoodFactorType,
    val impact: Float, // -1.0 to 1.0
    val description: String
)

enum class MoodFactorType {
    BRIGHTNESS, COLORS, FACES, COMPOSITION, TIME_OF_DAY, LOCATION
}

enum class MoodTrend {
    IMPROVING, DECLINING, STABLE, FLUCTUATING
}

// Pattern Detection
data class MemoryPatterns(
    val locationPatterns: List<LocationPattern>,
    val timePatterns: List<TimePattern>,
    val activityPatterns: List<ActivityPattern>,
    val moodPatterns: List<MoodPattern>
)

data class LocationPattern(
    val location: String,
    val frequency: Int,
    val averageMood: DomainMoodType,
    val favoriteTime: TimeOfDay,
    val confidence: Float
)

data class TimePattern(
    val timeOfDay: TimeOfDay,
    val frequency: Int,
    val averageMood: DomainMoodType,
    val commonLocations: List<String>,
    val confidence: Float
)

data class ActivityPattern(
    val activityType: ActivityType,
    val frequency: Int,
    val averageMood: DomainMoodType,
    val commonLocations: List<String>,
    val confidence: Float
)

data class MoodPattern(
    val moodType: DomainMoodType,
    val frequency: Int,
    val commonLocations: List<String>,
    val commonTimes: List<TimeOfDay>,
    val confidence: Float
)

enum class TimeOfDay {
    EARLY_MORNING, MORNING, AFTERNOON, EVENING, NIGHT, LATE_NIGHT
}

enum class ActivityType {
    OUTDOOR, INDOOR, SOCIAL, SOLO, WORK, LEISURE, TRAVEL, FOOD, FAMILY
}

// Memory Insights
data class MemoryInsights(
    val weeklyStats: WeeklyStats,
    val monthlyTrends: MonthlyTrends,
    val recommendations: List<Recommendation>,
    val generatedAt: Long
)

data class WeeklyStats(
    val totalPhotos: Int,
    val averageMood: DomainMoodType,
    val topLocations: List<String>,
    val moodTrend: MoodTrend,
    val activityBreakdown: Map<ActivityType, Int>
)

data class MonthlyTrends(
    val moodProgression: List<MoodDataPoint>,
    val locationExploration: LocationExploration,
    val activityEvolution: ActivityEvolution
)

data class MoodDataPoint(
    val date: Long,
    val averageMood: DomainMoodType,
    val moodScore: Float
)

data class LocationExploration(
    val newLocations: List<String>,
    val favoriteLocations: List<String>,
    val locationDiversity: Float // 0.0 - 1.0
)

data class ActivityEvolution(
    val newActivities: List<ActivityType>,
    val activityDiversity: Float, // 0.0 - 1.0
    val mostActiveTime: TimeOfDay
)

data class Recommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority,
    val actionItems: List<String>
)

enum class RecommendationType {
    PHOTO_SUGGESTION, LOCATION_EXPLORATION, MOOD_IMPROVEMENT, ACTIVITY_DIVERSITY
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

// Image Analysis Result
data class ImageAnalysis(
    val colorAnalysis: ColorAnalysis,
    val faceDetection: FaceDetection?,
    val moodAnalysis: MoodAnalysis,
    val composition: CompositionAnalysis,
    val metadata: ImageMetadata
)

data class CompositionAnalysis(
    val ruleOfThirds: Boolean,
    val symmetry: Float, // 0.0 - 1.0
    val balance: Float, // 0.0 - 1.0
    val focalPoint: FocalPoint?
)

data class FocalPoint(
    val x: Float, // 0.0 - 1.0
    val y: Float, // 0.0 - 1.0
    val strength: Float // 0.0 - 1.0
)

data class ImageMetadata(
    val timestamp: Long,
    val location: String?,
    val weather: String?,
    val deviceInfo: String?,
    val processingTime: Long // milliseconds
)
