package pl.soulsnaps.data

import pl.soulsnaps.domain.AffirmationGenerator

class RuleBasedAffirmationGenerator : AffirmationGenerator {

    override suspend fun generate(description: String, emotion: String): String {
        val affirmations = listOf(
            "Jestem wystarczający dokładnie taki, jaki jestem.",
            "Każdy dzień przynosi nowe możliwości.",
            "Zasługuję na spokój i harmonię.",
            "Mam moc, aby pokonać trudności."
        )
        return affirmations.random()
    }
}