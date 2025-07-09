package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AffirmationRepository

class ToggleFavoriteUseCase(private val repository: AffirmationRepository) {
    suspend operator fun invoke(id: String) = repository.updateIsFavorite(id)
}