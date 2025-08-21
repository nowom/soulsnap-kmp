package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory

class SaveMemoryUseCase(
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository,
    private val generateAffirmationUseCase: GenerateAffirmationUseCase
) {
    suspend operator fun invoke(memory: Memory): Int {
        // First save the memory to get the ID
        val memoryId = memoryRepository.addMemory(memory)
        
        // Generate affirmation for the memory
        val affirmationText = generateAffirmationUseCase(
            description = memory.description,
            emotion = memory.mood?.name ?: "neutral"
        )

        // Save the affirmation for this memory
        affirmationRepository.saveAffirmationForMemory(
            memoryId = memoryId,
            text = affirmationText,
            mood = memory.mood?.name ?: "neutral"
        )
        
        return memoryId
    }
}