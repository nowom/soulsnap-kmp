package pl.soulsnaps.data

import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.model.Affirmation

class AffirmationRepositoryImpl: AffirmationRepository {
    override suspend fun getAffirmations(emotionFilter: String?): List<Affirmation> {
        TODO("Not yet implemented")
    }

    override suspend fun saveAffirmationForMemory(
        memoryId: Int,
        text: String,
        mood: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getAffirmationByMemoryId(memoryId: Int): Affirmation? {
        TODO("Not yet implemented")
    }

    override suspend fun getFavoriteAffirmations(): List<Affirmation> {
        TODO("Not yet implemented")
    }

    override suspend fun updateIsFavorite(id: String) {
        TODO("Not yet implemented")
    }

    override fun playAffirmation(text: String) {
        TODO("Not yet implemented")
    }

    override fun stopAudio() {
        TODO("Not yet implemented")
    }

}