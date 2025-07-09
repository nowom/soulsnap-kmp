package pl.soulsnaps.domain

interface AffirmationGenerator {
    suspend fun generate(description: String, emotion: String): String
}