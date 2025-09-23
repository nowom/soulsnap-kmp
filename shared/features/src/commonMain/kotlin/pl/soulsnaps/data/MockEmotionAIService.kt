package pl.soulsnaps.data

import pl.soulsnaps.domain.interactor.EmotionAIService
import pl.soulsnaps.domain.interactor.QuizContext
import kotlinx.coroutines.delay

/**
 * Mock implementation of EmotionAIService
 * Provides realistic AI-like responses for testing
 * TODO: Replace with real OpenAI GPT integration
 */
class MockEmotionAIService : EmotionAIService {
    
    override suspend fun generateReflection(context: QuizContext): String {
        println("DEBUG: MockEmotionAIService.generateReflection - mood: ${context.overallMood}, emotion: ${context.primaryEmotion}")
        
        // Simulate API call delay
        delay(1000)
        
        val moodLevel = context.overallMood ?: 5
        val emotion = context.primaryEmotion ?: "neutral"
        val energy = context.energyLevel ?: 5
        val stress = context.stressLevel ?: 5
        
        return when {
            moodLevel >= 8 && energy >= 7 -> {
                "Widzę, że dzisiaj masz świetny dzień! Twój wysoki poziom energii i pozytywny nastrój to doskonała kombinacja. " +
                "To jest moment, kiedy możesz wykorzystać tę energię do realizacji swoich celów. " +
                "Pamiętaj, że takie dni są cennym paliwem dla trudniejszych momentów - ciesz się nimi w pełni!"
            }
            moodLevel <= 3 && stress >= 7 -> {
                "Widzę, że dzisiaj nie był łatwy dzień. Wysoki poziom stresu może być wyczerpujący, ale pamiętaj - " +
                "to tylko chwilowy stan. Każdy trudny dzień uczy nas czegoś o sobie. " +
                "Może to dobry moment na głębszy oddech i przypomnienie sobie, że jesteś silniejszy niż myślisz."
            }
            emotion == "joy" -> {
                "Radość, którą dziś odczuwasz, to prawdziwy skarb. Takie momenty przypominają nam, " +
                "co jest naprawdę ważne w życiu. Spróbuj zapamiętać to uczucie i to, co je wywołało - " +
                "będzie to dla Ciebie źródłem siły w przyszłości."
            }
            emotion == "sadness" -> {
                "Smutek też ma swoją wartość - pozwala nam docenić piękne chwile i pokazuje, że jesteś człowiekiem " +
                "o głębokich emocjach. Nie uciekaj przed tym uczuciem, ale też pamiętaj, że nie będzie trwało wiecznie. " +
                "Jutro może przynieść nowe perspektywy."
            }
            energy <= 3 -> {
                "Niski poziom energii może być sygnałem, że Twoje ciało i umysł potrzebują odpoczynku. " +
                "To nie jest słabość - to mądrość słuchania siebie. Może dziś warto skupić się na prostych, " +
                "przyjemnych czynnościach i dać sobie pozwolenie na wolniejsze tempo."
            }
            else -> {
                "Każdy dzień ma swój własny rytm i nastrój. Dzisiejszy dzień pokazuje, że jesteś w kontakcie " +
                "ze swoimi emocjami, co jest pierwszym krokiem do lepszego samopoczucia. " +
                "Pamiętaj, że każde doświadczenie - czy pozytywne, czy trudne - przyczynia się do Twojego rozwoju."
            }
        }
    }
    
    override suspend fun generateAffirmations(context: QuizContext): List<String> {
        println("DEBUG: MockEmotionAIService.generateAffirmations")
        delay(500)
        
        val moodLevel = context.overallMood ?: 5
        val emotion = context.primaryEmotion ?: "neutral"
        val stress = context.stressLevel ?: 5
        
        return when {
            moodLevel >= 8 -> listOf(
                "Jestem pełen/pełna pozytywnej energii i dzielę się nią ze światem",
                "Moja radość jest zaraźliwa i inspiruje innych",
                "Zasługuję na wszystkie dobre rzeczy, które się dzieją"
            )
            stress >= 7 -> listOf(
                "Jestem spokojny/spokojna i kontroluję swoje reakcje",
                "Każdy oddech przynosi mi więcej spokoju",
                "Mam siłę, by poradzić sobie z każdym wyzwaniem"
            )
            emotion == "sadness" -> listOf(
                "Moje emocje są ważne i pozwalam sobie je odczuwać",
                "Po każdej burzy przychodzi słońce",
                "Jestem silniejszy/silniejsza niż myślę"
            )
            emotion == "anger" -> listOf(
                "Przekształcam swoją energię w pozytywne działania",
                "Jestem panem/panią swoich emocji",
                "Wybaczam sobie i innym, aby znaleźć spokój"
            )
            else -> listOf(
                "Jestem dokładnie tam, gdzie powinienem/powinnam być",
                "Każdy dzień przynosi nowe możliwości",
                "Jestem wdzięczny/wdzięczna za to, co mam"
            )
        }
    }
    
    override suspend fun generateInsights(context: QuizContext): List<String> {
        println("DEBUG: MockEmotionAIService.generateInsights")
        delay(300)
        
        val insights = mutableListOf<String>()
        
        val moodLevel = context.overallMood ?: 5
        val energy = context.energyLevel ?: 5
        val stress = context.stressLevel ?: 5
        
        if (energy < 4 && stress > 6) {
            insights.add("Kombinacja niskiej energii i wysokiego stresu może wskazywać na potrzebę odpoczynku")
        }
        
        if (moodLevel > 7 && energy > 7) {
            insights.add("Twój wysoki nastrój i energia tworzą idealną kombinację do podejmowania nowych wyzwań")
        }
        
        if (stress > 7) {
            insights.add("Wysoki poziom stresu może wpływać na jakość snu i ogólne samopoczucie")
        }
        
        if (context.gratitude != null && context.gratitude.isNotBlank()) {
            insights.add("Praktykowanie wdzięczności to potężne narzędzie budowania pozytywnego nastawienia")
        }
        
        if (insights.isEmpty()) {
            insights.add("Regularne monitorowanie swoich emocji pomaga w lepszym zrozumieniu siebie")
        }
        
        return insights
    }
    
    override suspend fun generateRecommendedActions(context: QuizContext): List<String> {
        println("DEBUG: MockEmotionAIService.generateRecommendedActions")
        delay(300)
        
        val actions = mutableListOf<String>()
        val moodLevel = context.overallMood ?: 5
        val energy = context.energyLevel ?: 5
        val stress = context.stressLevel ?: 5
        
        when {
            stress > 7 -> {
                actions.addAll(listOf(
                    "Spróbuj techniki oddychania 4-7-8 przez 5 minut",
                    "Zrób krótki spacer na świeżym powietrzu",
                    "Posłuchaj uspokajającej muzyki"
                ))
            }
            energy < 4 -> {
                actions.addAll(listOf(
                    "Zrób sobie przerwę i odpoczynij",
                    "Wypij szklankę wody i zjedz zdrową przekąskę",
                    "Połóż się wcześniej spać dziś wieczorem"
                ))
            }
            moodLevel > 7 && energy > 6 -> {
                actions.addAll(listOf(
                    "Wykorzystaj tę pozytywną energię do realizacji celów",
                    "Podziel się swoją radością z kimś bliskim",
                    "Zaplanuj coś przyjemnego na najbliższe dni"
                ))
            }
            else -> {
                actions.addAll(listOf(
                    "Poświęć 10 minut na medytację lub mindfulness",
                    "Napisz 3 rzeczy, za które jesteś wdzięczny/a",
                    "Zrób coś, co sprawia Ci przyjemność"
                ))
            }
        }
        
        return actions.take(3) // Limit to 3 actions
    }
}

