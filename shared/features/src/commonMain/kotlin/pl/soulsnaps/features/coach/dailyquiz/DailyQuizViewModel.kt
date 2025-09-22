package pl.soulsnaps.features.coach.dailyquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetDailyQuizUseCase
import pl.soulsnaps.domain.interactor.SubmitQuizAnswersUseCase
import pl.soulsnaps.features.coach.model.*
import pl.soulsnaps.features.auth.UserSessionManager

/**
 * ViewModel for Daily Emotion Quiz
 */
class DailyQuizViewModel(
    private val getDailyQuizUseCase: GetDailyQuizUseCase,
    private val submitQuizAnswersUseCase: SubmitQuizAnswersUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {
    
    private val userId: String
        get() = userSessionManager.getCurrentUser()?.userId ?: "anonymous_user"
    
    private val _state = MutableStateFlow(DailyQuizState())
    val state: StateFlow<DailyQuizState> = _state.asStateFlow()
    
    init {
        println("DEBUG: DailyQuizViewModel.init()")
        loadDailyQuiz()
    }
    
    fun handleIntent(intent: DailyQuizIntent) {
        when (intent) {
            is DailyQuizIntent.LoadQuiz -> loadDailyQuiz()
            is DailyQuizIntent.AnswerQuestion -> answerQuestion(intent.questionId, intent.answer)
            is DailyQuizIntent.NextQuestion -> nextQuestion()
            is DailyQuizIntent.PreviousQuestion -> previousQuestion()
            is DailyQuizIntent.SubmitQuiz -> submitQuiz()
            is DailyQuizIntent.ViewReflection -> viewReflection()
            is DailyQuizIntent.CloseReflection -> closeReflection()
            is DailyQuizIntent.RetrySubmit -> retrySubmit()
        }
    }
    
    private fun loadDailyQuiz() {
        println("DEBUG: DailyQuizViewModel.loadDailyQuiz()")
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val quiz = getDailyQuizUseCase(userId)
                println("DEBUG: DailyQuizViewModel.loadDailyQuiz - loaded quiz: ${quiz.id}, completed: ${quiz.isCompleted}")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        quiz = quiz,
                        currentQuestionIndex = 0,
                        answers = quiz.answers.associateBy { answer -> answer.questionId }.toMutableMap(),
                        isCompleted = quiz.isCompleted,
                        showReflection = quiz.isCompleted && quiz.aiReflection != null
                    )
                }
                
            } catch (e: Exception) {
                println("ERROR: DailyQuizViewModel.loadDailyQuiz - failed: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Nie udało się załadować quizu: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun answerQuestion(questionId: String, answer: QuizAnswer) {
        println("DEBUG: DailyQuizViewModel.answerQuestion - questionId: $questionId")
        
        _state.update { currentState ->
            val updatedAnswers = currentState.answers.toMutableMap()
            updatedAnswers[questionId] = answer
            
            currentState.copy(
                answers = updatedAnswers,
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun nextQuestion() {
        _state.update { currentState ->
            val nextIndex = (currentState.currentQuestionIndex + 1)
                .coerceAtMost((currentState.quiz?.questions?.size ?: 1) - 1)
            
            println("DEBUG: DailyQuizViewModel.nextQuestion - from ${currentState.currentQuestionIndex} to $nextIndex")
            currentState.copy(currentQuestionIndex = nextIndex)
        }
    }
    
    private fun previousQuestion() {
        _state.update { currentState ->
            val prevIndex = (currentState.currentQuestionIndex - 1).coerceAtLeast(0)
            
            println("DEBUG: DailyQuizViewModel.previousQuestion - from ${currentState.currentQuestionIndex} to $prevIndex")
            currentState.copy(currentQuestionIndex = prevIndex)
        }
    }
    
    private fun submitQuiz() {
        println("DEBUG: DailyQuizViewModel.submitQuiz()")
        
        val currentQuiz = _state.value.quiz
        if (currentQuiz == null) {
            println("ERROR: DailyQuizViewModel.submitQuiz - no quiz loaded")
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            
            try {
                val answers = _state.value.answers.values.toList()
                println("DEBUG: DailyQuizViewModel.submitQuiz - submitting ${answers.size} answers")
                
                val result = submitQuizAnswersUseCase(currentQuiz.id, answers)
                
                result.fold(
                    onSuccess = { completedQuiz ->
                        println("DEBUG: DailyQuizViewModel.submitQuiz - success, has reflection: ${completedQuiz.aiReflection != null}")
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                quiz = completedQuiz,
                                isCompleted = true,
                                hasUnsavedChanges = false,
                                showReflection = completedQuiz.aiReflection != null
                            )
                        }
                    },
                    onFailure = { error ->
                        println("ERROR: DailyQuizViewModel.submitQuiz - failed: ${error.message}")
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = "Nie udało się zapisać odpowiedzi: ${error.message}"
                            )
                        }
                    }
                )
                
            } catch (e: Exception) {
                println("ERROR: DailyQuizViewModel.submitQuiz - exception: ${e.message}")
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Wystąpił błąd: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun viewReflection() {
        println("DEBUG: DailyQuizViewModel.viewReflection()")
        _state.update { it.copy(showReflection = true) }
    }
    
    private fun closeReflection() {
        println("DEBUG: DailyQuizViewModel.closeReflection()")
        _state.update { it.copy(showReflection = false) }
    }
    
    private fun retrySubmit() {
        println("DEBUG: DailyQuizViewModel.retrySubmit()")
        submitQuiz()
    }
    
    // Helper functions
    fun getCurrentQuestion(): EmotionQuizQuestion? {
        val state = _state.value
        return state.quiz?.questions?.getOrNull(state.currentQuestionIndex)
    }
    
    fun isLastQuestion(): Boolean {
        val state = _state.value
        return state.currentQuestionIndex == (state.quiz?.questions?.size ?: 0) - 1
    }
    
    fun isFirstQuestion(): Boolean {
        return _state.value.currentQuestionIndex == 0
    }
    
    fun getProgress(): Float {
        val state = _state.value
        val totalQuestions = state.quiz?.questions?.size ?: 1
        return (state.currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat()
    }
    
    fun getAnswerForCurrentQuestion(): QuizAnswer? {
        val currentQuestion = getCurrentQuestion() ?: return null
        return _state.value.answers[currentQuestion.id]
    }
    
    fun canProceed(): Boolean {
        val currentQuestion = getCurrentQuestion() ?: return false
        if (!currentQuestion.isRequired) return true
        
        val answer = getAnswerForCurrentQuestion()
        return when (currentQuestion.type) {
            QuestionType.EMOTION_SCALE -> answer?.scaleValue != null
            QuestionType.MULTIPLE_CHOICE, QuestionType.EMOJI_SELECT -> answer?.selectedOptions?.isNotEmpty() == true
            QuestionType.MULTIPLE_SELECT -> true // Optional by design
            QuestionType.TEXT_INPUT -> true // Optional or has text
        }
    }
}

/**
 * UI State for Daily Quiz
 */
data class DailyQuizState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val quiz: EmotionQuizSession? = null,
    val currentQuestionIndex: Int = 0,
    val answers: MutableMap<String, QuizAnswer> = mutableMapOf(),
    val isCompleted: Boolean = false,
    val showReflection: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null
)

/**
 * User Intents for Daily Quiz
 */
sealed class DailyQuizIntent {
    data object LoadQuiz : DailyQuizIntent()
    data class AnswerQuestion(val questionId: String, val answer: QuizAnswer) : DailyQuizIntent()
    data object NextQuestion : DailyQuizIntent()
    data object PreviousQuestion : DailyQuizIntent()
    data object SubmitQuiz : DailyQuizIntent()
    data object ViewReflection : DailyQuizIntent()
    data object CloseReflection : DailyQuizIntent()
    data object RetrySubmit : DailyQuizIntent()
}
