package pl.soulsnaps.domain.interactor

// Removed kotlinx.datetime imports
import pl.soulsnaps.features.coach.model.*
import pl.soulsnaps.domain.repository.EmotionQuizRepository
import pl.soulsnaps.utils.formatDate
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.utils.toLocalDateTime

/**
 * Use case for managing daily emotion quiz
 */
class GetDailyQuizUseCase(
    private val repository: EmotionQuizRepository
) {
    suspend operator fun invoke(
        userId: String,
        date: String = getCurrentTimeMillis().toLocalDateTime().date.toString()
    ): EmotionQuizSession {
        println("DEBUG: GetDailyQuizUseCase - getting quiz for user: $userId, date: $date")
        
        // Check if quiz already exists for today
        val existingQuiz = repository.getQuizForDate(userId, date)
        if (existingQuiz != null) {
            println("DEBUG: GetDailyQuizUseCase - found existing quiz: ${existingQuiz.id}")
            return existingQuiz
        }
        
        // Create new quiz session
        val questions = DailyQuizTemplates.getStandardDailyQuiz()
        val newQuiz = EmotionQuizSession(
            userId = userId,
            date = date,
            questions = questions
        )
        
        println("DEBUG: GetDailyQuizUseCase - creating new quiz: ${newQuiz.id}")
        return repository.saveQuizSession(newQuiz)
    }
}

/**
 * Use case for submitting quiz answers
 */
class SubmitQuizAnswersUseCase(
    private val repository: EmotionQuizRepository,
    private val aiReflectionUseCase: GenerateAIReflectionUseCase
) {
    suspend operator fun invoke(
        quizSessionId: String,
        answers: List<QuizAnswer>
    ): Result<EmotionQuizSession> {
        return try {
            println("DEBUG: SubmitQuizAnswersUseCase - submitting ${answers.size} answers for quiz: $quizSessionId")
            
            // Get existing quiz session
            val existingQuiz = repository.getQuizById(quizSessionId)
                ?: return Result.failure(Exception("Quiz session not found: $quizSessionId"))
            
            // Update quiz with answers
            val completedQuiz = existingQuiz.copy(
                answers = answers,
                isCompleted = true,
                completedAt = getCurrentTimeMillis()
            )
            
            // Save updated quiz
            val savedQuiz = repository.saveQuizSession(completedQuiz)
            println("DEBUG: SubmitQuizAnswersUseCase - quiz completed and saved")
            
            // Generate AI reflection asynchronously
            try {
                val reflection = aiReflectionUseCase(savedQuiz)
                val quizWithReflection = savedQuiz.copy(aiReflection = reflection)
                repository.saveQuizSession(quizWithReflection)
                println("DEBUG: SubmitQuizAnswersUseCase - AI reflection generated and saved")
                Result.success(quizWithReflection)
            } catch (e: Exception) {
                println("WARNING: SubmitQuizAnswersUseCase - AI reflection failed: ${e.message}")
                // Return quiz without reflection - AI failure shouldn't block user
                Result.success(savedQuiz)
            }
            
        } catch (e: Exception) {
            println("ERROR: SubmitQuizAnswersUseCase - failed: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting quiz summary for dashboard
 */
class GetQuizSummaryUseCase(
    private val repository: EmotionQuizRepository
) {
    suspend operator fun invoke(
        userId: String,
        date: String = getCurrentTimeMillis().toString()
    ): QuizSummary? {
        println("DEBUG: GetQuizSummaryUseCase - getting summary for user: $userId, date: $date")
        
        val quiz = repository.getQuizForDate(userId, date) ?: return null
        
        if (!quiz.isCompleted) {
            return QuizSummary(
                date = date,
                primaryEmotion = "Nie ukończono",
                emotionEmoji = "❓",
                energyLevel = 0,
                stressLevel = 0,
                overallMood = 0,
                hasReflection = false,
                isCompleted = false
            )
        }
        
        // Extract key metrics from answers
        val overallMood = quiz.answers.find { it.questionId == "overall_mood" }?.scaleValue ?: 5
        val energyLevel = quiz.answers.find { it.questionId == "energy_level" }?.scaleValue ?: 5
        val stressLevel = quiz.answers.find { it.questionId == "stress_level" }?.scaleValue ?: 5
        
        // Get primary emotion
        val primaryEmotionAnswer = quiz.answers.find { it.questionId == "primary_emotion" }
        val primaryEmotionId = primaryEmotionAnswer?.selectedOptions?.firstOrNull()
        val emotionOption = quiz.questions
            .find { it.id == "primary_emotion" }
            ?.options?.find { it.id == primaryEmotionId }
        
        return QuizSummary(
            date = date,
            primaryEmotion = emotionOption?.text ?: "Nieznana",
            emotionEmoji = emotionOption?.emoji ?: "😐",
            energyLevel = energyLevel,
            stressLevel = stressLevel,
            overallMood = overallMood,
            hasReflection = quiz.aiReflection != null,
            isCompleted = true
        )
    }
}

/**
 * Use case for generating AI reflection
 */
class GenerateAIReflectionUseCase(
    private val aiService: EmotionAIService
) {
    suspend operator fun invoke(quiz: EmotionQuizSession): AIReflection {
        println("DEBUG: GenerateAIReflectionUseCase - generating reflection for quiz: ${quiz.id}")
        
        // Build context from quiz answers
        val context = buildQuizContext(quiz)
        
        // Generate AI reflection
        val reflectionText = aiService.generateReflection(context)
        val suggestedAffirmations = aiService.generateAffirmations(context)
        val insights = aiService.generateInsights(context)
        val actions = aiService.generateRecommendedActions(context)
        
        return AIReflection(
            quizSessionId = quiz.id,
            reflectionText = reflectionText,
            suggestedAffirmations = suggestedAffirmations,
            emotionalInsights = insights,
            recommendedActions = actions
        )
    }
    
    private fun buildQuizContext(quiz: EmotionQuizSession): QuizContext {
        val answers = quiz.answers.associateBy { it.questionId }
        
        return QuizContext(
            overallMood = answers["overall_mood"]?.scaleValue,
            primaryEmotion = answers["primary_emotion"]?.selectedOptions?.firstOrNull(),
            energyLevel = answers["energy_level"]?.scaleValue,
            stressLevel = answers["stress_level"]?.scaleValue,
            highlights = answers["daily_highlights"]?.selectedOptions ?: emptyList(),
            gratitude = answers["gratitude"]?.textAnswer,
            date = quiz.date
        )
    }
}

/**
 * Context data for AI reflection generation
 */
data class QuizContext(
    val overallMood: Int?,
    val primaryEmotion: String?,
    val energyLevel: Int?,
    val stressLevel: Int?,
    val highlights: List<String>,
    val gratitude: String?,
    val date: String
)

/**
 * AI service interface for emotion analysis
 */
interface EmotionAIService {
    suspend fun generateReflection(context: QuizContext): String
    suspend fun generateAffirmations(context: QuizContext): List<String>
    suspend fun generateInsights(context: QuizContext): List<String>
    suspend fun generateRecommendedActions(context: QuizContext): List<String>
}