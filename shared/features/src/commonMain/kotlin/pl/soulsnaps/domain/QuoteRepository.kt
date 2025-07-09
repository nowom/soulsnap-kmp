package pl.soulsnaps.domain

interface QuoteRepository {
    suspend fun getQuoteOfTheDay(): String
}