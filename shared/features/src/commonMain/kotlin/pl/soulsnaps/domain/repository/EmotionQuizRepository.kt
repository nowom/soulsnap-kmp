package pl.soulsnaps.domain.repository

// Removed kotlinx.datetime import
import pl.soulsnaps.features.coach.model.EmotionQuizSession
import pl.soulsnaps.features.coach.model.QuizSummary

/**
 * Repository interface for emotion quiz data
 */
interface EmotionQuizRepository {
    
    /**
     * Get quiz session for specific date
     */
    suspend fun getQuizForDate(userId: String, date: String): EmotionQuizSession?
    
    /**
     * Get quiz session by ID
     */
    suspend fun getQuizById(quizId: String): EmotionQuizSession?
    
    /**
     * Save or update quiz session
     */
    suspend fun saveQuizSession(quiz: EmotionQuizSession): EmotionQuizSession
    
    /**
     * Get quiz history for user
     */
    suspend fun getQuizHistory(
        userId: String, 
        limit: Int = 30
    ): List<EmotionQuizSession>
    
    /**
     * Get quiz summaries for dashboard
     */
    suspend fun getQuizSummaries(
        userId: String,
        fromDate: String,
        toDate: String
    ): List<QuizSummary>
    
    /**
     * Check if user completed quiz today
     */
    suspend fun hasCompletedQuizToday(userId: String): Boolean
    
    /**
     * Delete quiz session
     */
    suspend fun deleteQuiz(quizId: String): Boolean
    
    /**
     * Get user's quiz streak (consecutive days)
     */
    suspend fun getQuizStreak(userId: String): Int
}
