package pl.soulsnaps.domain

import pl.soulsnaps.domain.model.Affirmation

interface AffirmationRepository {
    suspend fun getAffirmations(emotionFilter: String?): List<Affirmation>
    suspend fun saveAffirmationForMemory(memoryId: Int, text: String, mood: String)
    suspend fun getAffirmationByMemoryId(memoryId: Int): Affirmation?
    suspend fun getFavoriteAffirmations(): List<Affirmation>
    suspend fun updateIsFavorite(id: String)
    fun playAffirmation(text: String)
    fun stopAudio()
}