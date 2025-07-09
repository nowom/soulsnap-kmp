package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory

class SaveMemoryUseCase(
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository,
    private val generateAffirmationUseCase: GenerateAffirmationUseCase
) {
    suspend operator fun invoke(memory: Memory) {
        val affirmationText = generateAffirmationUseCase(
            description = memory.description,
            emotion = memory.mood?.name ?: "neutral"
        )

        affirmationRepository.saveAffirmationForMemory(
            memoryId = memory.id,
            text = affirmationText,
            mood = memory.mood?.name ?: "neutral"
        )
    }
}