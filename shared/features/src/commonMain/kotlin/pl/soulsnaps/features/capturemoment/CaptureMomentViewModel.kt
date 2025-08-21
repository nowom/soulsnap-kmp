package pl.soulsnaps.features.capturemoment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.features.auth.mvp.guard.CapacityGuard
import pl.soulsnaps.features.auth.mvp.guard.GuardFactory
import pl.soulsnaps.features.auth.mvp.guard.AccessResult
import pl.soulsnaps.features.analytics.CapacityAnalytics

class CaptureMomentViewModel(
    private val saveMemoryUseCase: SaveMemoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureMomentState())
    val state: StateFlow<CaptureMomentState> = _state
    
    // CapacityGuard for checking limits before saving memory
    private val capacityGuard = GuardFactory.createCapacityGuard()
    
    // CapacityAnalytics for tracking usage
    private val capacityAnalytics = CapacityAnalytics(capacityGuard)

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
            is CaptureMomentIntent.ClearMessages -> _state.update { 
                it.copy(errorMessage = null, successMessage = null) 
            }
            
            // New capacity management intents
            is CaptureMomentIntent.CheckCapacity -> checkCapacity()
            is CaptureMomentIntent.ShowPaywall -> showPaywall()
            is CaptureMomentIntent.NavigateToPaywall -> {
                _state.update { 
                    it.copy(
                        showPaywall = true,
                        paywallReason = intent.reason,
                        recommendedPlan = intent.recommendedPlan
                    ) 
                }
            }
            
            // New analytics intents
            is CaptureMomentIntent.ShowAnalytics -> showAnalytics()
            is CaptureMomentIntent.UpdateAnalytics -> updateAnalytics()
        }
    }


    private fun saveMemory() {
        if (state.value.isTitleValid == null || state.value.isTitleValid == false) {
            _state.update { it.copy(isTitleValid = false) }
            return
        }

        viewModelScope.launch {
            // First check capacity limits
            val capacityResult = checkCapacityBeforeSave()
            if (!capacityResult.allowed) {
                // Capacity limit exceeded - show paywall
                val recommendation = capacityGuard.getUpgradeRecommendation("current_user") // TODO: get real user ID
                _state.update { 
                    it.copy(
                        showPaywall = true,
                        paywallReason = capacityResult.message ?: "Limit pojemności przekroczony",
                        recommendedPlan = recommendation.recommendedPlan
                    ) 
                }
                return@launch
            }

            _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
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
                val memoryId = saveMemoryUseCase.invoke(memory)
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        successMessage = "Pamięć została zapisana pomyślnie!",
                        savedMemoryId = memoryId
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        errorMessage = "Nie udało się zapisać pamięci: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Check capacity limits before saving memory
     */
    private suspend fun checkCapacityBeforeSave(): AccessResult {
        // Estimate file size based on photo and audio
        val estimatedSizeMB = estimateFileSize()
        
        return capacityGuard.canAddSnapWithSize("current_user", estimatedSizeMB) // TODO: get real user ID
    }
    
    /**
     * Estimate file size for capacity check
     */
    private fun estimateFileSize(): Int {
        var totalSizeMB = 0
        
        // Photo typically 2-5MB
        if (state.value.photoUri != null) {
            totalSizeMB += 3
        }
        
        // Audio typically 1-3MB per minute
        if (state.value.audioUri != null) {
            totalSizeMB += 2
        }
        
        // Base memory data ~0.1MB
        totalSizeMB += 1
        
        return totalSizeMB
    }
    
    /**
     * Check current capacity status
     */
    private fun checkCapacity() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingCapacity = true) }
            
            try {
                val capacityInfo = capacityGuard.getCapacityInfo("current_user") // TODO: get real user ID
                _state.update { 
                    it.copy(
                        capacityInfo = capacityInfo,
                        isCheckingCapacity = false
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isCheckingCapacity = false,
                        errorMessage = "Nie udało się sprawdzić pojemności: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Show paywall
     */
    private fun showPaywall() {
        _state.update { it.copy(showPaywall = true) }
    }
    
    /**
     * Hide paywall
     */
    fun hidePaywall() {
        _state.update { it.copy(showPaywall = false) }
    }
    
    /**
     * Show analytics
     */
    private fun showAnalytics() {
        viewModelScope.launch {
            // Update analytics data first
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
            _state.update { it.copy(showAnalytics = true) }
        }
    }
    
    /**
     * Update analytics
     */
    private fun updateAnalytics() {
        viewModelScope.launch {
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
        }
    }
    
    /**
     * Hide analytics
     */
    fun hideAnalytics() {
        _state.update { it.copy(showAnalytics = false) }
    }
    
    /**
     * Get analytics instance
     */
    fun getAnalytics(): CapacityAnalytics {
        return capacityAnalytics
    }
}
