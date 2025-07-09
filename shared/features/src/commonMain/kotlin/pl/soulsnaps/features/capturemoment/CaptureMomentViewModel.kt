package pl.soulsnaps.features.capturemoment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.domain.model.Memory

class CaptureMomentViewModel(
    private val saveMemoryUseCase: SaveMemoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureMomentState())
    val state: StateFlow<CaptureMomentState> = _state

    fun handleIntent(intent: CaptureMomentIntent) {
        when (intent) {
            is CaptureMomentIntent.ChangeTitle -> {
                val isValid = intent.title.isNotEmpty()
                _state.update { it.copy(title = intent.title, isTitleValid = isValid) }
            }
            is CaptureMomentIntent.ChangeDescription -> _state.update { it.copy(description = intent.description) }
            is CaptureMomentIntent.ChangeDate -> _state.update { it.copy(date = intent.date) }
            is CaptureMomentIntent.ChangeMood -> _state.update { it.copy(selectedMood = intent.mood) }
            is CaptureMomentIntent.ChangePhoto -> _state.update { it.copy(photoUri = intent.photoUri) }
            is CaptureMomentIntent.ChangeLocation -> _state.update { it.copy(location = intent.location) }
            is CaptureMomentIntent.ChangeAudio -> _state.update { it.copy(audioUri = intent.audioUri) }
            is CaptureMomentIntent.SaveMemory -> saveMemory()
        }
    }


    private fun saveMemory() {
        if (state.value.isTitleValid == null || state.value.isTitleValid == false) {
            _state.update { it.copy(isTitleValid = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val memory = Memory(
                title = state.value.title,
                description = state.value.description,
                createdAt = state.value.date,
                mood = state.value.selectedMood,
                photoUri = state.value.photoUri,
                audioUri = state.value.audioUri,
                locationName = state.value.location,
                longitude = null,
                latitude = null,
                id = 0,
            )

            try {
                saveMemoryUseCase.invoke(memory)
                _state.update { it.copy(isSaving = false, ) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "Failed to save memory") }
            }
        }
    }
}
