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
import kotlin.random.Random

class DashboardViewModel(
    private val getAllMemoriesUseCase: GetAllMemoriesUseCase,
    private val getQuoteOfTheDayUseCase: GetQuoteOfTheDayUseCase
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
            is DashboardIntent.AddNewSnap -> addNewSnap()
            is DashboardIntent.NavigateToSoulSnaps -> navigateToSoulSnaps()
            is DashboardIntent.NavigateToAffirmations -> navigateToAffirmations()
            is DashboardIntent.NavigateToExercises -> navigateToExercises()
            is DashboardIntent.NavigateToVirtualMirror -> navigateToVirtualMirror()
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
        _state.update { it.copy(isAffirmationPlaying = true) }
        // TODO: Implement actual audio playback
    }

    private fun pauseAffirmation() {
        _state.update { it.copy(isAffirmationPlaying = false) }
        // TODO: Implement actual audio pause
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
} 