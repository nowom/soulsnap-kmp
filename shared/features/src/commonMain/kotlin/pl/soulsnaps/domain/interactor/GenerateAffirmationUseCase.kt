package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AffirmationGenerator

class GenerateAffirmationUseCase(
    private val openAIGenerator: AffirmationGenerator,
    private val ruleBasedGenerator: AffirmationGenerator
) {

    suspend operator fun invoke(description: String, emotion: String): String {
        return try {
            val result = ruleBasedGenerator.generate(description, emotion)//To test

            result.ifBlank {
                ruleBasedGenerator.generate(description, emotion)
            }

        } catch (e: Exception) {
            ruleBasedGenerator.generate(description, emotion)
        }
    }
}