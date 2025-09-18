package pl.soulsnaps.features.memoryanalysis.engine

import kotlinx.datetime.*
import pl.soulsnaps.features.memoryanalysis.model.*
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.domain.model.MoodType as DomainMoodType

/**
 * Engine for detecting patterns in memories and generating insights
 */
class PatternDetectionEngine : PatternDetectionEngineInterface {
    
    /**
     * Analyze all memories and detect patterns
     */
    override suspend fun detectPatterns(memories: List<Memory>): MemoryPatterns {
        val locationPatterns = detectLocationPatterns(memories)
        val timePatterns = detectTimePatterns(memories)
        val activityPatterns = detectActivityPatterns(memories)
        val moodPatterns = detectMoodPatterns(memories)
        
        return MemoryPatterns(
            locationPatterns = locationPatterns,
            timePatterns = timePatterns,
            activityPatterns = activityPatterns,
            moodPatterns = moodPatterns
        )
    }
    
    /**
     * Generate insights from memories
     */
    override suspend fun generateInsights(memories: List<Memory>): pl.soulsnaps.features.memoryanalysis.model.MemoryInsights {
        val patterns = detectPatterns(memories)
        
        // Generate weekly stats
        val weeklyStats = WeeklyStats(
            totalPhotos = memories.size,
            averageMood = calculateAverageMood(memories),
            topLocations = patterns.locationPatterns.take(5).map { it.location },
            moodTrend = MoodTrend.IMPROVING, // Placeholder
            activityBreakdown = patterns.activityPatterns.associate { it.activityType to it.frequency }
        )
        
        // Generate monthly trends
        val monthlyTrends = MonthlyTrends(
            moodProgression = listOf(), // Placeholder
            locationExploration = LocationExploration(
                newLocations = listOf(), // Placeholder
                favoriteLocations = patterns.locationPatterns.take(3).map { it.location },
                locationDiversity = 0.8f // Placeholder
            ),
            activityEvolution = ActivityEvolution(
                newActivities = listOf(), // Placeholder
                activityDiversity = 0.7f, // Placeholder
                mostActiveTime = TimeOfDay.AFTERNOON // Placeholder
            )
        )
        
        // Generate recommendations
        val recommendations = listOf<Recommendation>() // Placeholder
        
        return pl.soulsnaps.features.memoryanalysis.model.MemoryInsights(
            weeklyStats = weeklyStats,
            monthlyTrends = monthlyTrends,
            recommendations = recommendations,
            generatedAt = getCurrentTimeMillis()
        )
    }
    
    /**
     * Detect location-based patterns
     */
    private fun detectLocationPatterns(memories: List<Memory>): List<LocationPattern> {
        val locationGroups = memories
            .filter { it.locationName != null }
            .groupBy { it.locationName!! }
        
        return locationGroups.map { (location, locationMemories) ->
            val averageMood = calculateAverageMood(locationMemories)
            val favoriteTime = findFavoriteTime(locationMemories)
            val confidence = calculateConfidence(locationMemories.size)
            
            LocationPattern(
                location = location,
                frequency = locationMemories.size,
                averageMood = averageMood,
                favoriteTime = favoriteTime,
                confidence = confidence
            )
        }.sortedByDescending { it.frequency }
    }
    
    /**
     * Detect time-based patterns
     */
    private fun detectTimePatterns(memories: List<Memory>): List<TimePattern> {
        val timeGroups = memories
            .filter { it.createdAt > 0 }
            .groupBy { getTimeOfDay(it.createdAt) }
        
        return timeGroups.map { (timeOfDay, timeMemories) ->
            val averageMood = calculateAverageMood(timeMemories)
            val commonLocations = findCommonLocations(timeMemories)
            val confidence = calculateConfidence(timeMemories.size)
            
            TimePattern(
                timeOfDay = timeOfDay,
                frequency = timeMemories.size,
                averageMood = averageMood,
                commonLocations = commonLocations,
                confidence = confidence
            )
        }.sortedByDescending { it.frequency }
    }
    
    /**
     * Detect activity-based patterns
     */
    private fun detectActivityPatterns(memories: List<Memory>): List<ActivityPattern> {
        // Since Memory doesn't have tags, we'll infer activity from other properties
        val activityGroups = memories
            .groupBy { inferActivityType(it) }
        
        return activityGroups.map { (activityType, activityMemories) ->
            val averageMood = calculateAverageMood(activityMemories)
            val commonLocations = findCommonLocations(activityMemories)
            val confidence = calculateConfidence(activityMemories.size)
            
            ActivityPattern(
                activityType = activityType,
                frequency = activityMemories.size,
                averageMood = averageMood,
                commonLocations = commonLocations,
                confidence = confidence
            )
        }.sortedByDescending { it.frequency }
    }
    
    /**
     * Detect mood-based patterns
     */
    private fun detectMoodPatterns(memories: List<Memory>): List<MoodPattern> {
        val moodGroups = memories
            .filter { it.mood != null }
            .groupBy { it.mood!! }
        
        return moodGroups.map { (moodType, moodMemories) ->
            val commonLocations = findCommonLocations(moodMemories)
            val commonTimes = findCommonTimes(moodMemories)
            val confidence = calculateConfidence(moodMemories.size)
            
            MoodPattern(
                moodType = moodType,
                frequency = moodMemories.size,
                commonLocations = commonLocations,
                commonTimes = commonTimes,
                confidence = confidence
            )
        }.sortedByDescending { it.frequency }
    }
    
    /**
     * Generate weekly statistics
     */
    suspend fun generateWeeklyStats(memories: List<Memory>): WeeklyStats {
        // For now, use a simple approach without complex date calculations
        val weeklyMemories = memories.takeLast(7) // Last 7 memories
        
        val totalPhotos = weeklyMemories.size
        val averageMood = calculateAverageMood(weeklyMemories)
        val topLocations = findTopLocations(weeklyMemories, 5)
        val moodTrend = calculateMoodTrend(weeklyMemories)
        val activityBreakdown = calculateActivityBreakdown(weeklyMemories)
        
        return WeeklyStats(
            totalPhotos = totalPhotos,
            averageMood = averageMood,
            topLocations = topLocations,
            moodTrend = moodTrend,
            activityBreakdown = activityBreakdown
        )
    }
    
    /**
     * Generate monthly trends
     */
    suspend fun generateMonthlyTrends(memories: List<Memory>): MonthlyTrends {
        // For now, use a simple approach without complex date calculations
        val monthlyMemories = memories.takeLast(30) // Last 30 memories
        
        val moodProgression = calculateMoodProgression(monthlyMemories)
        val locationExploration = calculateLocationExploration(monthlyMemories)
        val activityEvolution = calculateActivityEvolution(monthlyMemories)
        
        return MonthlyTrends(
            moodProgression = moodProgression,
            locationExploration = locationExploration,
            activityEvolution = activityEvolution
        )
    }
    
    /**
     * Generate recommendations based on patterns
     */
    suspend fun generateRecommendations(patterns: MemoryPatterns): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // Photo suggestions based on time patterns
        val lowPhotoTimes = patterns.timePatterns.filter { it.frequency < 3 }
        if (lowPhotoTimes.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.PHOTO_SUGGESTION,
                    title = "Explore New Times",
                    description = "Try taking photos during ${lowPhotoTimes.first().timeOfDay.name.lowercase()} for variety",
                    priority = Priority.MEDIUM,
                    actionItems = listOf("Set reminders for new times", "Plan activities during low-photo periods")
                )
            )
        }
        
        // Location exploration suggestions
        val favoriteLocations = patterns.locationPatterns.take(3)
        if (favoriteLocations.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.LOCATION_EXPLORATION,
                    title = "Discover New Places",
                    description = "You often visit ${favoriteLocations.first().location}. Try exploring nearby areas!",
                    priority = Priority.HIGH,
                    actionItems = listOf("Research nearby attractions", "Plan weekend trips", "Join local groups")
                )
            )
        }
        
        // Mood improvement suggestions
        val lowMoodPatterns = patterns.moodPatterns.filter { it.moodType in listOf(DomainMoodType.SAD) }
        if (lowMoodPatterns.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.MOOD_IMPROVEMENT,
                    title = "Boost Your Mood",
                    description = "Consider activities that make you happy during challenging times",
                    priority = Priority.HIGH,
                    actionItems = listOf("Plan fun activities", "Visit favorite places", "Connect with friends")
                )
            )
        }
        
        return recommendations
    }
    
    // Helper functions
    private fun calculateAverageMood(memories: List<Memory>): DomainMoodType {
        val moodCounts = memories
            .filter { it.mood != null }
            .groupBy { it.mood!! }
            .mapValues { it.value.size }
        
        return if (moodCounts.isNotEmpty()) {
            moodCounts.maxByOrNull { it.value }?.key ?: DomainMoodType.NEUTRAL
        } else {
            DomainMoodType.NEUTRAL
        }
    }
    
    private fun findFavoriteTime(memories: List<Memory>): TimeOfDay {
        val timeCounts = memories
            .filter { it.createdAt > 0 }
            .groupBy { getTimeOfDay(it.createdAt) }
            .mapValues { it.value.size }
        
        return if (timeCounts.isNotEmpty()) {
            timeCounts.maxByOrNull { it.value }?.key ?: TimeOfDay.AFTERNOON
        } else {
            TimeOfDay.AFTERNOON
        }
    }
    
    private fun findCommonLocations(memories: List<Memory>): List<String> {
        return memories
            .filter { it.locationName != null }
            .groupBy { it.locationName!! }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }
    
    private fun findCommonTimes(memories: List<Memory>): List<TimeOfDay> {
        return memories
            .filter { it.createdAt > 0 }
            .groupBy { getTimeOfDay(it.createdAt) }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }
    
    private fun calculateConfidence(count: Int): Float {
        return when {
            count >= 10 -> 0.9f
            count >= 5 -> 0.7f
            count >= 2 -> 0.5f
            else -> 0.3f
        }
    }
    
    private fun getTimeOfDay(timestamp: Long): TimeOfDay {
        val utilsTimeOfDay = pl.soulsnaps.utils.getTimeOfDayEnum(timestamp)
        return when (utilsTimeOfDay) {
            pl.soulsnaps.utils.TimeOfDay.EARLY_MORNING -> TimeOfDay.EARLY_MORNING
            pl.soulsnaps.utils.TimeOfDay.MORNING -> TimeOfDay.MORNING
            pl.soulsnaps.utils.TimeOfDay.AFTERNOON -> TimeOfDay.AFTERNOON
            pl.soulsnaps.utils.TimeOfDay.EVENING -> TimeOfDay.EVENING
            pl.soulsnaps.utils.TimeOfDay.NIGHT -> TimeOfDay.NIGHT
            pl.soulsnaps.utils.TimeOfDay.LATE_NIGHT -> TimeOfDay.LATE_NIGHT
        }
    }
    
    private fun inferActivityType(memory: Memory): ActivityType {
        // Infer activity from memory properties
        val locationName = memory.locationName?.lowercase() ?: ""
        val description = memory.description.lowercase()
        
        return when {
            locationName.contains("outdoor") || locationName.contains("nature") || description.contains("outdoor") -> ActivityType.OUTDOOR
            locationName.contains("restaurant") || locationName.contains("cafe") || description.contains("food") -> ActivityType.FOOD
            locationName.contains("work") || locationName.contains("office") || description.contains("work") -> ActivityType.WORK
            locationName.contains("travel") || locationName.contains("trip") || description.contains("travel") -> ActivityType.TRAVEL
            description.contains("family") || description.contains("home") -> ActivityType.FAMILY
            description.contains("friend") || description.contains("social") -> ActivityType.SOCIAL
            else -> ActivityType.LEISURE
        }
    }
    
    private fun findTopLocations(memories: List<Memory>, count: Int): List<String> {
        return memories
            .filter { it.locationName != null }
            .groupBy { it.locationName!! }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(count)
            .map { it.key }
    }
    
    private fun calculateMoodTrend(memories: List<Memory>): MoodTrend {
        // Simple implementation - can be enhanced with more sophisticated analysis
        val sortedMemories = memories
            .filter { it.mood != null && it.createdAt > 0 }
            .sortedBy { it.createdAt }
        
        if (sortedMemories.size < 2) return MoodTrend.STABLE
        
        val firstMood = sortedMemories.first().mood!!
        val lastMood = sortedMemories.last().mood!!
        
        return when {
            firstMood == lastMood -> MoodTrend.STABLE
            firstMood.ordinal < lastMood.ordinal -> MoodTrend.IMPROVING
            else -> MoodTrend.DECLINING
        }
    }
    
    private fun calculateActivityBreakdown(memories: List<Memory>): Map<ActivityType, Int> {
        return memories
            .groupBy { inferActivityType(it) }
            .mapValues { it.value.size }
    }
    
    private fun calculateMoodProgression(memories: List<Memory>): List<MoodDataPoint> {
        // Simplified mood progression calculation
        val moodProgression = mutableListOf<MoodDataPoint>()
        
        // Group memories by chunks of 7 (weekly)
        val weeklyChunks = memories.chunked(7)
        
        weeklyChunks.forEachIndexed { weekIndex, weekMemories ->
            if (weekMemories.isNotEmpty()) {
                val averageMood = calculateAverageMood(weekMemories)
                val moodScore = calculateMoodScore(weekMemories)
                
                // Use current time for all weeks to avoid complex date arithmetic
                val weekTimestamp = getCurrentTimeMillis()
                
                moodProgression.add(
                    MoodDataPoint(
                        date = weekTimestamp,
                        averageMood = averageMood,
                        moodScore = moodScore
                    )
                )
            }
        }
        
        return moodProgression.sortedBy { it.date }
    }
    
    private fun calculateMoodScore(memories: List<Memory>): Float {
        val moodValues = memories
            .filter { it.mood != null }
            .map { moodToScore(it.mood!!) }
        
        return if (moodValues.isNotEmpty()) {
            moodValues.average().toFloat()
        } else {
            0.5f
        }
    }
    
    private fun moodToScore(mood: DomainMoodType): Float {
        return when (mood) {
            DomainMoodType.EXCITED -> 1.0f
            DomainMoodType.HAPPY -> 0.8f
            DomainMoodType.RELAXED -> 0.6f
            DomainMoodType.NEUTRAL -> 0.5f
            DomainMoodType.SAD -> 0.2f
        }
    }
    
    private fun calculateLocationExploration(memories: List<Memory>): LocationExploration {
        val allLocations = memories
            .filter { it.locationName != null }
            .map { it.locationName!! }
            .distinct()
        
        val newLocations = allLocations.takeLast(5) // Last 5 locations
        val favoriteLocations = findTopLocations(memories, 5)
        val locationDiversity = calculateDiversity(allLocations.size, memories.size)
        
        return LocationExploration(
            newLocations = newLocations,
            favoriteLocations = favoriteLocations,
            locationDiversity = locationDiversity
        )
    }
    
    private fun calculateActivityEvolution(memories: List<Memory>): ActivityEvolution {
        val allActivities = memories
            .map { inferActivityType(it) }
            .distinct()
        
        val newActivities = allActivities.takeLast(3) // Last 3 activities
        val activityDiversity = calculateDiversity(allActivities.size, memories.size)
        val mostActiveTime = findFavoriteTime(memories)
        
        return ActivityEvolution(
            newActivities = newActivities,
            activityDiversity = activityDiversity,
            mostActiveTime = mostActiveTime
        )
    }
    
    private fun calculateDiversity(uniqueCount: Int, totalCount: Int): Float {
        return if (totalCount > 0) {
            (uniqueCount.toFloat() / totalCount).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}
