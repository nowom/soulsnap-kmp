package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.utils.getCurrentTimeMillis

class EditMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory): Memory {
        println("DEBUG: EditMemoryUseCase.invoke() - updating memory with ID: ${memory.id}")
        println("DEBUG: EditMemoryUseCase.invoke() - memory details: title='${memory.title}', description='${memory.description}', mood='${memory.mood}'")
        
        // Update the updatedAt timestamp
        val updatedMemory = memory.copy(updatedAt = getCurrentTimeMillis())
        
        // Update in repository
        memoryRepository.updateMemory(updatedMemory)
        
        println("DEBUG: EditMemoryUseCase.invoke() - memory updated successfully")
        return updatedMemory
    }
}
