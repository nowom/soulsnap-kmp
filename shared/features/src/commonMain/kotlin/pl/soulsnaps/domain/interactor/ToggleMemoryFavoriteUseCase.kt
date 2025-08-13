package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.MemoryRepository

class ToggleMemoryFavoriteUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(id: Int, isFavorite: Boolean) = 
        memoryRepository.markAsFavorite(id, isFavorite)
}
