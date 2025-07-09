package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.QuoteRepository

class GetQuoteOfTheDayUseCase(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(): String {
        return repository.getQuoteOfTheDay()
    }
}