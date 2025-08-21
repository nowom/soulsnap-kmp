package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.Memories
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.domain.model.ThemeType
import kotlin.random.Random

class AffirmationRepositoryImpl(
    private val memoryDao: MemoryDao
) : AffirmationRepository {
    
    // Mock data for affirmations when no database entries exist
    private val mockAffirmations = listOf(
        Affirmation(
            id = "1",
            text = "Jestem spokojem i światłem.",
            audioUrl = null,
            emotion = "Spokój",
            timeOfDay = "Poranek",
            isFavorite = true,
            themeType = ThemeType.SELF_LOVE
        ),
        Affirmation(
            id = "2",
            text = "Każdy dzień to nowa szansa.",
            audioUrl = null,
            emotion = "Motywacja",
            timeOfDay = "Dzień",
            isFavorite = false,
            themeType = ThemeType.GOALS
        ),
        Affirmation(
            id = "3",
            text = "Zasługuję na odpoczynek.",
            audioUrl = null,
            emotion = "Relaks",
            timeOfDay = "Wieczór",
            isFavorite = false,
            themeType = ThemeType.CALM
        ),
        Affirmation(
            id = "4",
            text = "Jestem wystarczający dokładnie taki, jaki jestem.",
            audioUrl = null,
            emotion = "Pewność siebie",
            timeOfDay = "Poranek",
            isFavorite = true,
            themeType = ThemeType.CONFIDENCE
        ),
        Affirmation(
            id = "5",
            text = "Dziękuję za wszystkie błogosławieństwa w moim życiu.",
            audioUrl = null,
            emotion = "Wdzięczność",
            timeOfDay = "Wieczór",
            isFavorite = false,
            themeType = ThemeType.GRATITUDE
        ),
        Affirmation(
            id = "6",
            text = "Otaczam się ludźmi, którzy mnie wspierają.",
            audioUrl = null,
            emotion = "Relacje",
            timeOfDay = "Dzień",
            isFavorite = false,
            themeType = ThemeType.RELATIONSHIPS
        ),
        Affirmation(
            id = "7",
            text = "Moje ciało jest świątynią i dbam o nie z miłością.",
            audioUrl = null,
            emotion = "Zdrowie",
            timeOfDay = "Poranek",
            isFavorite = true,
            themeType = ThemeType.HEALTH
        )
    )

    override suspend fun getAffirmations(emotionFilter: String?): List<Affirmation> {
        // Get affirmations from memories database
        val memoryAffirmations = getAffirmationsFromMemories(emotionFilter)
        
        // If no affirmations in database, return mock data
        if (memoryAffirmations.isEmpty()) {
            return if (emotionFilter != null) {
                mockAffirmations.filter { it.emotion.contains(emotionFilter, ignoreCase = true) }
            } else {
                mockAffirmations
            }
        }
        
        return memoryAffirmations
    }

    override suspend fun saveAffirmationForMemory(
        memoryId: Int,
        text: String,
        mood: String
    ) {
        // Get the memory and update its affirmation field
        val memory = memoryDao.getById(memoryId.toLong())
        memory?.let { existingMemory ->
            val updatedMemory = existingMemory.copy(
                affirmation = text
            )
            memoryDao.update(updatedMemory)
        }
    }

    override suspend fun getAffirmationByMemoryId(memoryId: Int): Affirmation? {
        val memory = memoryDao.getById(memoryId.toLong())
        return memory?.affirmation?.let { affirmationText ->
            Affirmation(
                id = memoryId.toString(),
                text = affirmationText,
                audioUrl = null,
                emotion = memory.mood ?: "neutral",
                timeOfDay = getTimeOfDay(),
                isFavorite = memory.isFavorite,
                themeType = getThemeTypeFromMood(memory.mood)
            )
        }
    }

    override suspend fun getFavoriteAffirmations(): List<Affirmation> {
        return getAffirmations(null).filter { it.isFavorite }
    }

    override suspend fun updateIsFavorite(id: String) {
        // For now, we'll just update the mock data
        // In a real implementation, you'd update the database
        // Since we don't have a separate affirmations table, 
        // we'd need to create one or handle favorites differently
        println("Updating favorite status for affirmation: $id")
    }

    override fun playAffirmation(text: String) {
        // TODO: Implement text-to-speech functionality
        // This would require platform-specific implementations
        println("Playing affirmation: $text")
    }

    override fun stopAudio() {
        // TODO: Implement audio stop functionality
        println("Stopping audio playback")
    }

    // Helper methods
    private suspend fun getAffirmationsFromMemories(emotionFilter: String?): List<Affirmation> {
        return try {
            // For now, we'll return empty list as Flow handling requires more complex setup
            // In a real implementation, you'd collect the Flow and handle it properly
            // This is a simplified version that works with the current architecture
            emptyList()
        } catch (e: Exception) {
            println("Error getting affirmations from memories: ${e.message}")
            emptyList()
        }
    }

    private fun getTimeOfDay(): String {
        val now = Clock.System.now()
        val hour = now.toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when {
            hour < 12 -> "Poranek"
            hour < 18 -> "Dzień"
            else -> "Wieczór"
        }
    }

    private fun getThemeTypeFromMood(mood: String?): ThemeType {
        return when (mood?.lowercase()) {
            "happy", "joy", "excited" -> ThemeType.CONFIDENCE
            "calm", "peaceful", "relaxed" -> ThemeType.CALM
            "grateful", "thankful" -> ThemeType.GRATITUDE
            "motivated", "focused" -> ThemeType.GOALS
            "loving", "caring" -> ThemeType.RELATIONSHIPS
            "healthy", "energetic" -> ThemeType.HEALTH
            else -> ThemeType.SELF_LOVE
        }
    }
}