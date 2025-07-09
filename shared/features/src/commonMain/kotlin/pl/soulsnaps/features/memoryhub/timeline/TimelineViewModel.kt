package pl.soulsnaps.features.memoryhub.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.interactor.GetQuoteOfTheDayUseCase
import pl.soulsnaps.domain.model.Memory

class TimelineViewModel (
    private val getAllMemoriesUseCase: GetAllMemoriesUseCase,
    private val getQuoteOfTheDayUseCase: GetQuoteOfTheDayUseCase
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> =  getAllMemoriesUseCase().combine(
        flow {
            emit(getQuoteOfTheDayUseCase())
        }, { snaps, quoteOfTheDay ->
            TimelineUiState(false, snaps, quoteOfTheDay)
        }).stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(), TimelineUiState())

    // Możesz dodać inne akcje np. odśwież, usuń snap, itp.

    data class TimelineUiState(
        val isLoading: Boolean = false,
        val snaps: List<Memory> = emptyList(),
        val quoteOfTheDay: String? = null
    )
}