package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory

class GetMemoryByIdUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memoryId: Int): Memory {
        return memoryRepository.getMemoryById(memoryId)
            ?: throw IllegalArgumentException("Memory with id $memoryId not found")
    }
}
