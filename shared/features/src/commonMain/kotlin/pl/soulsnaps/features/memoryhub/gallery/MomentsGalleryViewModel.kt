package pl.soulsnaps.features.memoryhub.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.model.Memory

class MomentsGalleryViewModel(
    getAllMemoriesUseCase: GetAllMemoriesUseCase
) : ViewModel() {

    val uiState: StateFlow<GalleryUiState> =  getAllMemoriesUseCase().map {
        GalleryUiState(isLoading = false, it)
    }.onStart {
        emit(GalleryUiState(true))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), GalleryUiState())

    data class GalleryUiState(
        val isLoading: Boolean = false,
        val memories: List<Memory> = emptyList()
    )
}