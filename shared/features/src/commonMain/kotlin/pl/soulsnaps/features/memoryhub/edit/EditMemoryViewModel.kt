package pl.soulsnaps.features.memoryhub.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetMemoryByIdUseCase
import pl.soulsnaps.domain.interactor.EditMemoryUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.utils.getCurrentTimeMillis

data class EditMemoryState(
    val memory: Memory? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Form fields (populated from memory)
    val title: String = "",
    val description: String = "",
    val selectedMood: MoodType? = null,
    val location: String? = null,
    val isTitleValid: Boolean? = null
)

sealed interface EditMemoryIntent {
    data object LoadMemory : EditMemoryIntent
    data class ChangeTitle(val title: String) : EditMemoryIntent
    data class ChangeDescription(val description: String) : EditMemoryIntent
    data class ChangeMood(val mood: MoodType) : EditMemoryIntent
    data class ChangeLocation(val location: String) : EditMemoryIntent
    data object SaveMemory : EditMemoryIntent
    data object ClearMessages : EditMemoryIntent
}

class EditMemoryViewModel(
    private val getMemoryByIdUseCase: GetMemoryByIdUseCase,
    private val editMemoryUseCase: EditMemoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditMemoryState())
    val state: StateFlow<EditMemoryState> = _state.asStateFlow()

    private var currentMemoryId: Int = 0

    fun loadMemoryForEdit(memoryId: Int) {
        currentMemoryId = memoryId
        handleIntent(EditMemoryIntent.LoadMemory)
    }

    fun handleIntent(intent: EditMemoryIntent) {
        when (intent) {
            is EditMemoryIntent.LoadMemory -> loadMemory()
            is EditMemoryIntent.ChangeTitle -> {
                val isValid = intent.title.isNotEmpty()
                _state.update { it.copy(title = intent.title, isTitleValid = isValid) }
            }
            is EditMemoryIntent.ChangeDescription -> _state.update { it.copy(description = intent.description) }
            is EditMemoryIntent.ChangeMood -> _state.update { it.copy(selectedMood = intent.mood) }
            is EditMemoryIntent.ChangeLocation -> _state.update { it.copy(location = intent.location) }
            is EditMemoryIntent.SaveMemory -> saveMemory()
            is EditMemoryIntent.ClearMessages -> _state.update { 
                it.copy(errorMessage = null, successMessage = null) 
            }
        }
    }

    private fun loadMemory() {
        viewModelScope.launch {
            println("DEBUG: EditMemoryViewModel.loadMemory() - loading memory ID: $currentMemoryId")
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val memory = getMemoryByIdUseCase(currentMemoryId)
                println("DEBUG: EditMemoryViewModel.loadMemory() - loaded memory: title='${memory.title}'")
                
                // Populate form fields with existing data
                _state.update { 
                    it.copy(
                        memory = memory,
                        isLoading = false,
                        title = memory.title,
                        description = memory.description,
                        selectedMood = memory.mood,
                        location = memory.locationName,
                        isTitleValid = true
                    ) 
                }
            } catch (e: Exception) {
                println("ERROR: EditMemoryViewModel.loadMemory() - failed to load memory: ${e.message}")
                e.printStackTrace()
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load memory: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun saveMemory() {
        println("DEBUG: EditMemoryViewModel.saveMemory() - starting memory update")
        
        if (state.value.isTitleValid == null || state.value.isTitleValid == false) {
            println("DEBUG: EditMemoryViewModel.saveMemory() - title validation failed")
            _state.update { it.copy(isTitleValid = false) }
            return
        }

        val currentMemory = state.value.memory
        if (currentMemory == null) {
            _state.update { it.copy(errorMessage = "No memory to update") }
            return
        }

        viewModelScope.launch {
            println("DEBUG: EditMemoryViewModel.saveMemory() - updating memory")
            _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            try {
                // Create updated memory with new values
                val updatedMemory = currentMemory.copy(
                    title = state.value.title,
                    description = state.value.description,
                    mood = state.value.selectedMood,
                    locationName = state.value.location,
                    updatedAt = getCurrentTimeMillis()
                )
                
                editMemoryUseCase(updatedMemory)
                
                println("DEBUG: EditMemoryViewModel.saveMemory() - memory updated successfully")
                
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        successMessage = "Memory updated successfully!",
                        isSaved = true,
                        memory = updatedMemory
                    ) 
                }
                
            } catch (e: Exception) {
                println("ERROR: EditMemoryViewModel.saveMemory() - exception occurred while updating memory")
                println("ERROR: EditMemoryViewModel.saveMemory() - exception message: ${e.message}")
                e.printStackTrace()
                
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        errorMessage = "Failed to update memory: ${e.message}"
                    ) 
                }
            }
        }
    }
}
