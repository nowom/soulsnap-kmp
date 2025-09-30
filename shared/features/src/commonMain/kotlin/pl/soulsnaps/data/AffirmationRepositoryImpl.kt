package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _mockAffirmations = MutableStateFlow(
        listOf(
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
    )
    
    val mockAffirmations: StateFlow<List<Affirmation>> = _mockAffirmations.asStateFlow()

    override suspend fun getAffirmations(emotionFilter: String?): List<Affirmation> {
        // Get affirmations from memories database
        val memoryAffirmations = getAffirmationsFromMemories(emotionFilter)
        
        // If no affirmations in database, return mock data
        if (memoryAffirmations.isEmpty()) {
            val currentAffirmations = _mockAffirmations.value
            return if (emotionFilter != null) {
                currentAffirmations.filter { it.emotion.contains(emotionFilter, ignoreCase = true) }
            } else {
                currentAffirmations
            }
        }
        
        return memoryAffirmations
    }

    override suspend fun saveAffirmationForMemory(
        memoryId: Int,
        text: String,
        mood: String
    ) {
        try {
            // Try to get the memory - this might fail if it has large Base64 fields
            val memory = memoryDao.getById(memoryId.toLong())
            memory?.let { existingMemory ->
                val updatedMemory = existingMemory.copy(
                    affirmation = text
                )
                memoryDao.update(updatedMemory)
            }
        } catch (e: Exception) {
            println("ERROR: AffirmationRepositoryImpl.saveAffirmationForMemory() - failed to save affirmation: ${e.message}")
            // If the memory has large fields that cause CursorWindow error, 
            // we'll skip saving the affirmation for now
            println("WARNING: Skipping affirmation save due to large memory data")
        }
    }

    override suspend fun getAffirmationByMemoryId(memoryId: Int): Affirmation? {
        try {
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
        } catch (e: Exception) {
            println("ERROR: AffirmationRepositoryImpl.getAffirmationByMemoryId() - failed to get affirmation: ${e.message}")
            return null
        }
    }

    override suspend fun getFavoriteAffirmations(): List<Affirmation> {
        return getAffirmations(null).filter { it.isFavorite }
    }

    override suspend fun updateIsFavorite(id: String) {
        // Update the mock data using StateFlow
        val currentAffirmations = _mockAffirmations.value.toMutableList()
        val index = currentAffirmations.indexOfFirst { it.id == id }
        if (index != -1) {
            val affirmation = currentAffirmations[index]
            currentAffirmations[index] = affirmation.copy(isFavorite = !affirmation.isFavorite)
            _mockAffirmations.value = currentAffirmations
            println("Updated favorite status for affirmation: $id to ${currentAffirmations[index].isFavorite}")
        } else {
            println("Affirmation with id $id not found")
        }
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
    
    override fun getAffirmationsFlow(): Flow<List<Affirmation>> {
        return _mockAffirmations
    }
    
    override suspend fun clearAllFavorites() {
        // Reset all affirmations to not favorite
        val currentAffirmations = _mockAffirmations.value.toMutableList()
        val updatedAffirmations = currentAffirmations.map { affirmation ->
            affirmation.copy(isFavorite = false)
        }
        _mockAffirmations.value = updatedAffirmations
        println("✅ AffirmationRepository: Cleared all favorites - ${updatedAffirmations.count { it.isFavorite }} favorites remaining")
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
        // Use a simple approach without Clock.System
        // For now, return a default value
        return "Dzień"
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