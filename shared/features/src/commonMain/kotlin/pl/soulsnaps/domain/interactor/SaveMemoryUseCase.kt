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
        println("DEBUG: SaveMemoryUseCase.invoke() - starting memory save process")
        println("DEBUG: SaveMemoryUseCase.invoke() - memory details: title='${memory.title}', description='${memory.description}', mood='${memory.mood}'")
        
        try {
            // First save the memory to get the ID
            println("DEBUG: SaveMemoryUseCase.invoke() - calling memoryRepository.addMemory()")
            val memoryId = memoryRepository.addMemory(memory)
            println("DEBUG: SaveMemoryUseCase.invoke() - memory saved with ID: $memoryId")
            
            // Generate affirmation for the memory
            println("DEBUG: SaveMemoryUseCase.invoke() - generating affirmation")
            val affirmationText = generateAffirmationUseCase(
                description = memory.description,
                emotion = memory.mood?.name ?: "neutral"
            )
            println("DEBUG: SaveMemoryUseCase.invoke() - affirmation generated: '$affirmationText'")

            // Save the affirmation for this memory
            println("DEBUG: SaveMemoryUseCase.invoke() - saving affirmation for memory")
            affirmationRepository.saveAffirmationForMemory(
                memoryId = memoryId,
                text = affirmationText,
                mood = memory.mood?.name ?: "neutral"
            )
            println("DEBUG: SaveMemoryUseCase.invoke() - affirmation saved successfully")
            
            println("DEBUG: SaveMemoryUseCase.invoke() - memory save process completed successfully")
            return memoryId
            
        } catch (e: Exception) {
            println("ERROR: SaveMemoryUseCase.invoke() - exception occurred during memory save")
            println("ERROR: SaveMemoryUseCase.invoke() - exception type: ${e::class.simpleName}")
            println("ERROR: SaveMemoryUseCase.invoke() - exception message: ${e.message}")
            println("ERROR: SaveMemoryUseCase.invoke() - full stacktrace:")
            e.printStackTrace()
            throw e
        }
    }
}