package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory

class GetSoulSnapUseCase(private val memoryRepository: MemoryRepository) {
    suspend fun invoke(id: Int): Memory? =
        memoryRepository.getMemoryById(id)
}