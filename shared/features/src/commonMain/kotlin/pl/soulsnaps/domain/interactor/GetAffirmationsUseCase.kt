package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.model.Affirmation

class GetAffirmationsUseCase(
    private val repository: AffirmationRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        onlyFavorites: Boolean = false
    ): List<Affirmation> {
        val all = repository.getAffirmations(filter)
        return if (onlyFavorites) {
            all.filter { it.isFavorite }
        } else {
            all
        }
    }
}