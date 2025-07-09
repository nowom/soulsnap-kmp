package pl.soulsnaps.features.affirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.domain.model.ThemeType

class AffirmationsViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(AffirmationsUiState())
    val uiState: StateFlow<AffirmationsUiState> = _uiState

    init {
        onEvent(AffirmationsEvent.LoadInitial)
    }

    fun onEvent(event: AffirmationsEvent) {
        when (event) {
            is AffirmationsEvent.LoadInitial -> loadAffirmations()
            is AffirmationsEvent.Play -> playAffirmation(event.affirmation)
            is AffirmationsEvent.ToggleFavorite -> toggleFavorite(event.affirmation.id)
            is AffirmationsEvent.SelectFilter -> {
                _uiState.value = _uiState.value.copy(selectedFilter = event.filter)
                loadAffirmations()
            }
            is AffirmationsEvent.ToggleFavoritesOnly -> {
                _uiState.value = _uiState.value.copy(
                    showOnlyFavorites = !_uiState.value.showOnlyFavorites
                )
                loadAffirmations()
            }
        }
    }

    private fun loadAffirmations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val all = sampleAffirmations()
            val filtered = all.filter {
                (!_uiState.value.showOnlyFavorites || it.isFavorite)
            }
            _uiState.value = _uiState.value.copy(
                affirmations = filtered,
                isLoading = false
            )
        }
    }

    private fun toggleFavorite(id: String) {
        _uiState.value = _uiState.value.copy(
            affirmations = _uiState.value.affirmations.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
        )
    }

    private fun playAffirmation(affirmation: Affirmation) {
        println("Play: ${affirmation.text}") // Stub — replace with actual player
    }

    private fun sampleAffirmations(): List<Affirmation> = listOf(
        Affirmation(

            text = "Jestem spokojem i światłem.",
            audioUrl = null,
            emotion = "Spokój",
            timeOfDay = "Poranek",
            isFavorite = true,
            themeType = ThemeType.SELF_LOVE
        ),
        Affirmation(

            text = "Każdy dzień to nowa szansa.",
            audioUrl = null,
            emotion = "Motywacja",
            timeOfDay = "Dzień",
            isFavorite = false,
            themeType = ThemeType.GOALS
        ),
        Affirmation(
            text = "Zasługuję na odpoczynek.",
            audioUrl = null,
            emotion = "Relaks",
            timeOfDay = "Wieczór",
            isFavorite = false,
            themeType = ThemeType.GOALS
        )
    )
}

// UI state

data class AffirmationsUiState(
    val affirmations: List<Affirmation> = emptyList(),
    val selectedFilter: String = "Emotion",
    val showOnlyFavorites: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AffirmationsEvent {
    data object LoadInitial : AffirmationsEvent()
    data class Play(val affirmation: Affirmation) : AffirmationsEvent()
    data class ToggleFavorite(val affirmation: Affirmation) : AffirmationsEvent()
    data class SelectFilter(val filter: String) : AffirmationsEvent()
    data object ToggleFavoritesOnly : AffirmationsEvent()
}
