package pl.soulsnaps.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.interactor.GetQuoteOfTheDayUseCase
import pl.soulsnaps.audio.AudioManager
import pl.soulsnaps.audio.VoiceType
import kotlin.random.Random

class DashboardViewModel(
    private val getAllMemoriesUseCase: GetAllMemoriesUseCase,
    private val getQuoteOfTheDayUseCase: GetQuoteOfTheDayUseCase,
    private val audioManager: AudioManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val emotions = listOf(
        EmotionOfTheDay("Radość", "😊", "Dziś czujesz radość i energię!"),
        EmotionOfTheDay("Smutek", "😢", "Dziś możesz czuć się przygnębiony. To w porządku."),
        EmotionOfTheDay("Złość", "😠", "Dziś możesz odczuwać złość. Spróbuj się wyciszyć."),
        EmotionOfTheDay("Strach", "😨", "Dziś możesz czuć niepokój. Oddychaj głęboko."),
        EmotionOfTheDay("Zaskoczenie", "😮", "Dziś coś Cię zaskoczyło!"),
        EmotionOfTheDay("Wstręt", "🤢", "Dziś możesz czuć niechęć. Zadbaj o siebie."),
        EmotionOfTheDay("Spokój", "😌", "Dziś czujesz się spokojny i zrelaksowany."),
        EmotionOfTheDay("Motywacja", "💪", "Dziś masz motywację do działania!"),
        EmotionOfTheDay("Wdzięczność", "🙏", "Dziś czujesz wdzięczność za to, co masz."),
        EmotionOfTheDay("Miłość", "❤️", "Dziś czujesz miłość i ciepło.")
    )

    init {
        loadDashboard()
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDashboard -> loadDashboard()
            is DashboardIntent.PlayAffirmation -> playAffirmation()
            is DashboardIntent.PauseAffirmation -> pauseAffirmation()
            is DashboardIntent.ChangeAffirmation -> changeAffirmation()
            is DashboardIntent.FavoriteAffirmation -> favoriteAffirmation()
            is DashboardIntent.AddNewSnap -> addNewSnap()
            is DashboardIntent.NavigateToSoulSnaps -> navigateToSoulSnaps()
            is DashboardIntent.NavigateToAffirmations -> navigateToAffirmations()
            is DashboardIntent.NavigateToExercises -> navigateToExercises()
            is DashboardIntent.NavigateToVirtualMirror -> navigateToVirtualMirror()
            is DashboardIntent.TakeMoodQuiz -> takeMoodQuiz()
            is DashboardIntent.ShowNotifications -> showNotifications()
            is DashboardIntent.RefreshDashboard -> refreshDashboard()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Load last SoulSnap
                getAllMemoriesUseCase().collect { memories ->
                    val lastSnap = memories.firstOrNull()
                    _state.update {
                        it.copy(
                            lastSoulSnap = lastSnap,
                            isLoading = false
                        )
                    }
                }
                // Load affirmation of the day
                val quote = getQuoteOfTheDayUseCase()
                // Pick a random emotion of the day
                val emotion = emotions[Random.nextInt(emotions.size)]
                _state.update {
                    it.copy(
                        affirmationOfTheDay = quote,
                        emotionOfTheDay = emotion,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load dashboard: ${e.message}"
                    )
                }
            }
        }
    }

    private fun playAffirmation() {
        viewModelScope.launch {
            try {
                val affirmationText = _state.value.affirmationOfTheDay
                audioManager.playAffirmation(affirmationText, VoiceType.DEFAULT)
                _state.update { it.copy(isAffirmationPlaying = true) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isAffirmationPlaying = false,
                        errorMessage = "Failed to play affirmation: ${e.message}"
                    )
                }
            }
        }
    }

    private fun pauseAffirmation() {
        viewModelScope.launch {
            try {
                audioManager.pause()
                _state.update { it.copy(isAffirmationPlaying = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        errorMessage = "Failed to pause affirmation: ${e.message}"
                    )
                }
            }
        }
    }

    private fun addNewSnap() {
        // TODO: Navigate to add snap screen
    }

    private fun navigateToSoulSnaps() {
        // TODO: Navigate to SoulSnaps screen
    }

    private fun navigateToAffirmations() {
        // TODO: Navigate to Affirmations screen
    }

    private fun navigateToExercises() {
        // TODO: Navigate to Exercises screen
    }

    private fun navigateToVirtualMirror() {
        // TODO: Navigate to Virtual Mirror screen
    }

    private fun changeAffirmation() {
        viewModelScope.launch {
            try {
                val newQuote = getQuoteOfTheDayUseCase()
                _state.update { it.copy(affirmationOfTheDay = newQuote) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Failed to change affirmation: ${e.message}") }
            }
        }
    }

    private fun favoriteAffirmation() {
        // TODO: Implement favorite affirmation functionality
        // This would save the current affirmation to favorites
    }

    private fun takeMoodQuiz() {
        // TODO: Navigate to mood quiz screen
    }

    private fun showNotifications() {
        // TODO: Navigate to notifications center
    }

    private fun refreshDashboard() {
        loadDashboard()
    }
} 