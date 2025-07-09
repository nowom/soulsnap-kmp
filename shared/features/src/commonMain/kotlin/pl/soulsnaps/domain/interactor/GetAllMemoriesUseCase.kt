package pl.soulsnaps.domain.interactor

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory

class GetAllMemoriesUseCase(private val memoryRepository: MemoryRepository) {
    operator fun invoke(): Flow<List<Memory>> {
        return memoryRepository.getMemories()
    }
}