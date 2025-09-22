package pl.soulsnaps.features.coach.model

import kotlinx.serialization.Serializable

/**
 * Daily emotion quiz data models
 */

/**
 * Daily emotion quiz question
 */
@Serializable
data class EmotionQuizQuestion(
    val id: String,
    val question: String,
    val type: QuestionType,
    val options: List<QuizOption> = emptyList(),
    val isRequired: Boolean = true,
    val order: Int = 0
)

/**
 * Quiz question types
 */
@Serializable
enum class QuestionType {
    EMOTION_SCALE,      // 1-10 scale for emotions
    MULTIPLE_CHOICE,    // Select one from options
    MULTIPLE_SELECT,    // Select multiple from options
    TEXT_INPUT,         // Free text input
    EMOJI_SELECT        // Select emoji(s)
}

/**
 * Quiz answer option
 */
@Serializable
data class QuizOption(
    val id: String,
    val text: String,
    val emoji: String? = null,
    val value: Int? = null,
    val color: String? = null
)

/**
 * User's answer to a quiz question
 */
@Serializable
data class QuizAnswer(
    val questionId: String,
    val selectedOptions: List<String> = emptyList(), // Option IDs
    val textAnswer: String? = null,
    val scaleValue: Int? = null
)

/**
 * Complete quiz session
 */
@Serializable
data class EmotionQuizSession(
    val id: String = generateQuizId(),
    val userId: String,
    val date: String = getCurrentDateString(),
    val questions: List<EmotionQuizQuestion>,
    val answers: List<QuizAnswer> = emptyList(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val aiReflection: AIReflection? = null
)

/**
 * AI-generated reflection based on quiz answers
 */
@Serializable
data class AIReflection(
    val id: String = generateReflectionId(),
    val quizSessionId: String,
    val reflectionText: String,
    val suggestedAffirmations: List<String> = emptyList(),
    val emotionalInsights: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Quiz result summary for dashboard
 */
data class QuizSummary(
    val date: String,
    val primaryEmotion: String,
    val emotionEmoji: String,
    val energyLevel: Int, // 1-10
    val stressLevel: Int, // 1-10
    val overallMood: Int, // 1-10
    val hasReflection: Boolean,
    val isCompleted: Boolean
)

/**
 * Predefined quiz templates
 */
object DailyQuizTemplates {
    
    /**
     * Standard daily emotion quiz
     */
    fun getStandardDailyQuiz(): List<EmotionQuizQuestion> {
        return listOf(
            EmotionQuizQuestion(
                id = "overall_mood",
                question = "Jak oceniasz swój ogólny nastrój dzisiaj?",
                type = QuestionType.EMOTION_SCALE,
                options = (1..10).map { 
                    QuizOption(
                        id = "mood_$it",
                        text = when(it) {
                            1, 2 -> "Bardzo źle"
                            3, 4 -> "Źle"
                            5, 6 -> "Neutralnie"
                            7, 8 -> "Dobrze"
                            9, 10 -> "Bardzo dobrze"
                            else -> "$it"
                        },
                        value = it
                    )
                },
                order = 1
            ),
            
            EmotionQuizQuestion(
                id = "primary_emotion",
                question = "Jaką główną emocję odczuwasz w tej chwili?",
                type = QuestionType.EMOJI_SELECT,
                options = listOf(
                    QuizOption("joy", "Radość", "😊", color = "#FFD700"),
                    QuizOption("sadness", "Smutek", "😢", color = "#4169E1"),
                    QuizOption("anger", "Złość", "😠", color = "#DC143C"),
                    QuizOption("fear", "Strach", "😨", color = "#9400D3"),
                    QuizOption("surprise", "Zaskoczenie", "😲", color = "#FF8C00"),
                    QuizOption("disgust", "Obrzydzenie", "🤢", color = "#228B22"),
                    QuizOption("anticipation", "Oczekiwanie", "🤔", color = "#FF1493"),
                    QuizOption("trust", "Zaufanie", "😌", color = "#00CED1"),
                    QuizOption("calm", "Spokój", "😇", color = "#98FB98"),
                    QuizOption("confused", "Zagubienie", "😕", color = "#DDA0DD")
                ),
                order = 2
            ),
            
            EmotionQuizQuestion(
                id = "energy_level",
                question = "Jaki jest Twój poziom energii?",
                type = QuestionType.EMOTION_SCALE,
                options = (1..10).map { 
                    QuizOption(
                        id = "energy_$it",
                        text = when(it) {
                            1, 2 -> "Bardzo niski"
                            3, 4 -> "Niski"
                            5, 6 -> "Średni"
                            7, 8 -> "Wysoki"
                            9, 10 -> "Bardzo wysoki"
                            else -> "$it"
                        },
                        value = it
                    )
                },
                order = 3
            ),
            
            EmotionQuizQuestion(
                id = "stress_level",
                question = "Jak oceniasz swój poziom stresu?",
                type = QuestionType.EMOTION_SCALE,
                options = (1..10).map { 
                    QuizOption(
                        id = "stress_$it",
                        text = when(it) {
                            1, 2 -> "Bardzo niski"
                            3, 4 -> "Niski"
                            5, 6 -> "Średni"
                            7, 8 -> "Wysoki"
                            9, 10 -> "Bardzo wysoki"
                            else -> "$it"
                        },
                        value = it
                    )
                },
                order = 4
            ),
            
            EmotionQuizQuestion(
                id = "daily_highlights",
                question = "Co było najważniejsze w Twoim dniu?",
                type = QuestionType.MULTIPLE_SELECT,
                options = listOf(
                    QuizOption("work", "Praca/nauka", "💼"),
                    QuizOption("family", "Rodzina", "👨‍👩‍👧‍👦"),
                    QuizOption("friends", "Przyjaciele", "👫"),
                    QuizOption("health", "Zdrowie", "🏃‍♂️"),
                    QuizOption("hobbies", "Hobby", "🎨"),
                    QuizOption("rest", "Odpoczynek", "🛋️"),
                    QuizOption("nature", "Natura", "🌳"),
                    QuizOption("achievement", "Osiągnięcie", "🏆"),
                    QuizOption("challenge", "Wyzwanie", "⚡"),
                    QuizOption("other", "Inne", "🤷‍♂️")
                ),
                isRequired = false,
                order = 5
            ),
            
            EmotionQuizQuestion(
                id = "gratitude",
                question = "Za co jesteś dziś wdzięczny/a? (opcjonalne)",
                type = QuestionType.TEXT_INPUT,
                isRequired = false,
                order = 6
            )
        )
    }
    
    /**
     * Quick 3-question version for busy days
     */
    fun getQuickDailyQuiz(): List<EmotionQuizQuestion> {
        return getStandardDailyQuiz().take(3)
    }
}

// Helper functions
private fun generateQuizId(): String = "quiz_${System.currentTimeMillis()}"
private fun generateReflectionId(): String = "reflection_${System.currentTimeMillis()}"

private fun getCurrentDateString(): String {
    // Simple date format: YYYY-MM-DD
    val timestamp = System.currentTimeMillis()
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd")
    return formatter.format(date)
}
