package pl.soulsnaps.data

// Removed kotlinx.datetime imports
import pl.soulsnaps.domain.repository.EmotionQuizRepository
import pl.soulsnaps.features.coach.model.EmotionQuizSession
import pl.soulsnaps.features.coach.model.QuizSummary
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.utils.formatTimestamp

/**
 * In-memory implementation of EmotionQuizRepository
 * TODO: Replace with proper database implementation
 */
class EmotionQuizRepositoryImpl : EmotionQuizRepository {
    
    private val quizSessions = mutableMapOf<String, EmotionQuizSession>()
    private val userQuizzes = mutableMapOf<String, MutableList<String>>() // userId -> quizIds
    
    override suspend fun getQuizForDate(userId: String, date: String): EmotionQuizSession? {
        println("DEBUG: EmotionQuizRepository.getQuizForDate - user: $userId, date: $date")
        
        val userQuizIds = userQuizzes[userId] ?: return null
        return userQuizIds
            .mapNotNull { quizSessions[it] }
            .find { it.date == date }
    }
    
    override suspend fun getQuizById(quizId: String): EmotionQuizSession? {
        println("DEBUG: EmotionQuizRepository.getQuizById - quizId: $quizId")
        return quizSessions[quizId]
    }
    
    override suspend fun saveQuizSession(quiz: EmotionQuizSession): EmotionQuizSession {
        println("DEBUG: EmotionQuizRepository.saveQuizSession - quiz: ${quiz.id}, completed: ${quiz.isCompleted}")
        
        quizSessions[quiz.id] = quiz
        
        // Add to user's quiz list if not already present
        val userQuizIds = userQuizzes.getOrPut(quiz.userId) { mutableListOf() }
        if (!userQuizIds.contains(quiz.id)) {
            userQuizIds.add(quiz.id)
        }
        
        return quiz
    }
    
    override suspend fun getQuizHistory(userId: String, limit: Int): List<EmotionQuizSession> {
        println("DEBUG: EmotionQuizRepository.getQuizHistory - user: $userId, limit: $limit")
        
        val userQuizIds = userQuizzes[userId] ?: return emptyList()
        return userQuizIds
            .mapNotNull { quizSessions[it] }
            .sortedByDescending { it.date }
            .take(limit)
    }
    
    override suspend fun getQuizSummaries(
        userId: String,
        fromDate: String,
        toDate: String
    ): List<QuizSummary> {
        println("DEBUG: EmotionQuizRepository.getQuizSummaries - user: $userId, from: $fromDate, to: $toDate")
        
        val userQuizIds = userQuizzes[userId] ?: return emptyList()
        return userQuizIds
            .mapNotNull { quizSessions[it] }
            .filter { it.date >= fromDate && it.date <= toDate }
            .filter { it.isCompleted }
            .map { quiz ->
                val overallMood = quiz.answers.find { it.questionId == "overall_mood" }?.scaleValue ?: 5
                val energyLevel = quiz.answers.find { it.questionId == "energy_level" }?.scaleValue ?: 5
                val stressLevel = quiz.answers.find { it.questionId == "stress_level" }?.scaleValue ?: 5
                
                val primaryEmotionAnswer = quiz.answers.find { it.questionId == "primary_emotion" }
                val primaryEmotionId = primaryEmotionAnswer?.selectedOptions?.firstOrNull()
                val emotionOption = quiz.questions
                    .find { it.id == "primary_emotion" }
                    ?.options?.find { it.id == primaryEmotionId }
                
                QuizSummary(
                    date = quiz.date,
                    primaryEmotion = emotionOption?.text ?: "Nieznana",
                    emotionEmoji = emotionOption?.emoji ?: "😐",
                    energyLevel = energyLevel,
                    stressLevel = stressLevel,
                    overallMood = overallMood,
                    hasReflection = quiz.aiReflection != null,
                    isCompleted = true
                )
            }
            .sortedByDescending { it.date }
    }
    
    override suspend fun hasCompletedQuizToday(userId: String): Boolean {
        val today = getCurrentDateString()
        val todayQuiz = getQuizForDate(userId, today)
        val result = todayQuiz?.isCompleted == true
        
        println("DEBUG: EmotionQuizRepository.hasCompletedQuizToday - user: $userId, result: $result")
        return result
    }
    
    override suspend fun deleteQuiz(quizId: String): Boolean {
        println("DEBUG: EmotionQuizRepository.deleteQuiz - quizId: $quizId")
        
        val quiz = quizSessions.remove(quizId) ?: return false
        
        // Remove from user's quiz list
        val userQuizIds = userQuizzes[quiz.userId]
        userQuizIds?.remove(quizId)
        
        return true
    }
    
    override suspend fun getQuizStreak(userId: String): Int {
        println("DEBUG: EmotionQuizRepository.getQuizStreak - user: $userId")
        
        val userQuizIds = userQuizzes[userId] ?: return 0
        val completedQuizzes = userQuizIds
            .mapNotNull { quizSessions[it] }
            .filter { it.isCompleted }
            .sortedByDescending { it.date }
        
        if (completedQuizzes.isEmpty()) return 0
        
        val today = getCurrentDateString()
        var streak = 0
        
        // Simple streak calculation - count consecutive days from today backwards
        // This is a simplified version, in production you'd want proper date arithmetic
        for (quiz in completedQuizzes) {
            if (quiz.date == today) {
                streak++
                break // For now, just check today
            }
        }
        
        println("DEBUG: EmotionQuizRepository.getQuizStreak - streak: $streak")
        return streak
    }
    
    private fun getCurrentDateString(): String {
        val timestamp = getCurrentTimeMillis()
        return formatTimestamp(timestamp, "yyyy-MM-dd")
    }
}
