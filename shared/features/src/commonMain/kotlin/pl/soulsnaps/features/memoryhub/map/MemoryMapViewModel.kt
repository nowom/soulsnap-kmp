package pl.soulsnaps.features.memoryhub.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.model.Memory

class MemoryMapViewModel (
    private val getAllMemoriesUseCase: GetAllMemoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMemoriesWithLocation()
    }

    private fun loadMemoriesWithLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAllMemoriesUseCase().collect {
                val allMemories = it
                val withLocation = allMemories.filter { it.latitude != null && it.longitude != null }
                _uiState.update {
                    it.copy(
                        isLoading = false, memoriesWithLocation = withLocation
                    )
                }
            }
        }
    }
}

data class MapUiState(
    val isLoading: Boolean = false,
    val memoriesWithLocation: List<Memory> = emptyList()
)
