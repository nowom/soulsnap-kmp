package pl.soulsnaps.features.memoryhub.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetMemoryByIdUseCase
import pl.soulsnaps.domain.interactor.ToggleMemoryFavoriteUseCase
import pl.soulsnaps.domain.model.Memory

data class MemoryDetailsState(
    val memory: Memory? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface MemoryDetailsIntent {
    data object LoadMemory : MemoryDetailsIntent
    data object ToggleFavorite : MemoryDetailsIntent
    data object DeleteMemory : MemoryDetailsIntent
    data object ShareMemory : MemoryDetailsIntent
    data object EditMemory : MemoryDetailsIntent
}

class MemoryDetailsViewModel(
    private val getMemoryByIdUseCase: GetMemoryByIdUseCase,
    private val toggleMemoryFavoriteUseCase: ToggleMemoryFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryDetailsState())
    val state: StateFlow<MemoryDetailsState> = _state.asStateFlow()

    private var currentMemoryId: Int = 0

    fun loadMemoryDetails(memoryId: Int) {
        currentMemoryId = memoryId
        handleIntent(MemoryDetailsIntent.LoadMemory)
    }

    fun handleIntent(intent: MemoryDetailsIntent) {
        when (intent) {
            is MemoryDetailsIntent.LoadMemory -> loadMemory()
            is MemoryDetailsIntent.ToggleFavorite -> toggleFavorite()
            is MemoryDetailsIntent.DeleteMemory -> deleteMemory()
            is MemoryDetailsIntent.ShareMemory -> shareMemory()
            is MemoryDetailsIntent.EditMemory -> editMemory()
        }
    }

    private fun loadMemory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val memory = getMemoryByIdUseCase(currentMemoryId)
                _state.update { it.copy(memory = memory, isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to load memory: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val currentMemory = _state.value.memory
                if (currentMemory != null) {
                    toggleMemoryFavoriteUseCase(currentMemory.id, !currentMemory.isFavorite)
                    // Update local state
                    _state.update { 
                        it.copy(
                            memory = currentMemory.copy(isFavorite = !currentMemory.isFavorite)
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to toggle favorite: ${e.message}")
                }
            }
        }
    }

    private fun deleteMemory() {
        // TODO: Implement delete functionality
        viewModelScope.launch {
            try {
                // Call delete use case
                _state.update { it.copy(errorMessage = "Delete functionality not implemented yet") }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to delete memory: ${e.message}")
                }
            }
        }
    }

    private fun shareMemory() {
        // TODO: Implement share functionality
        viewModelScope.launch {
            try {
                // Call share use case
                _state.update { it.copy(errorMessage = "Share functionality not implemented yet") }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to share memory: ${e.message}")
                }
            }
        }
    }

    private fun editMemory() {
        // TODO: Implement edit functionality
        viewModelScope.launch {
            try {
                // Call edit use case
                _state.update { it.copy(errorMessage = "Edit functionality not implemented yet") }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to edit memory: ${e.message}")
                }
            }
        }
    }
}
