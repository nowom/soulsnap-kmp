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
    
    init {
        println("DEBUG: CaptureMomentViewModel.init() - initial state date: ${_state.value.date}")
        println("DEBUG: CaptureMomentViewModel.init() - current timestamp: ${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}")
    }
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
        println("DEBUG: CaptureMomentViewModel.saveMemory() - starting memory save")
        
        if (state.value.isTitleValid == null || state.value.isTitleValid == false) {
            println("DEBUG: CaptureMomentViewModel.saveMemory() - title validation failed")
            _state.update { it.copy(isTitleValid = false) }
            return
        }

        viewModelScope.launch {
            println("DEBUG: CaptureMomentViewModel.saveMemory() - checking capacity limits")
            
            // First check capacity limits
            val capacityResult = checkCapacityBeforeSave()
            println("DEBUG: CaptureMomentViewModel.saveMemory() - capacity result: $capacityResult")
            
            if (!capacityResult.allowed) {
                println("DEBUG: CaptureMomentViewModel.saveMemory() - capacity limit exceeded, showing paywall")
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

            println("DEBUG: CaptureMomentViewModel.saveMemory() - creating memory object")
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
            
            println("DEBUG: CaptureMomentViewModel.saveMemory() - memory object created: title='${memory.title}', description='${memory.description}', mood='${memory.mood}', photoUri='${memory.photoUri}', audioUri='${memory.audioUri}', createdAt=${memory.createdAt}")
            println("DEBUG: CaptureMomentViewModel.saveMemory() - state.value.date=${state.value.date}")
            println("DEBUG: CaptureMomentViewModel.saveMemory() - current timestamp=${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}")

            try {
                println("DEBUG: CaptureMomentViewModel.saveMemory() - calling saveMemoryUseCase.invoke()")
                val memoryId = saveMemoryUseCase.invoke(memory)
                println("DEBUG: CaptureMomentViewModel.saveMemory() - memory saved successfully with ID: $memoryId")
                
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        successMessage = "Pamięć została zapisana pomyślnie!",
                        savedMemoryId = memoryId
                    ) 
                }
            } catch (e: Exception) {
                println("ERROR: CaptureMomentViewModel.saveMemory() - exception occurred while saving memory")
                println("ERROR: CaptureMomentViewModel.saveMemory() - exception type: ${e::class.simpleName}")
                println("ERROR: CaptureMomentViewModel.saveMemory() - exception message: ${e.message}")
                println("ERROR: CaptureMomentViewModel.saveMemory() - full stacktrace:")
                e.printStackTrace()
                
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
        println("DEBUG: CaptureMomentViewModel.checkCapacityBeforeSave() - checking capacity limits")
        
        // Estimate file size based on photo and audio
        val estimatedSizeMB = estimateFileSize()
        println("DEBUG: CaptureMomentViewModel.checkCapacityBeforeSave() - estimated file size: ${estimatedSizeMB}MB")
        
        try {
            val result = capacityGuard.canAddSnapWithSize("current_user", estimatedSizeMB) // TODO: get real user ID
            println("DEBUG: CaptureMomentViewModel.checkCapacityBeforeSave() - capacity check result: $result")
            return result
        } catch (e: Exception) {
            println("ERROR: CaptureMomentViewModel.checkCapacityBeforeSave() - exception during capacity check")
            println("ERROR: CaptureMomentViewModel.checkCapacityBeforeSave() - exception type: ${e::class.simpleName}")
            println("ERROR: CaptureMomentViewModel.checkCapacityBeforeSave() - exception message: ${e.message}")
            println("ERROR: CaptureMomentViewModel.checkCapacityBeforeSave() - full stacktrace:")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Estimate file size for capacity check
     */
    private fun estimateFileSize(): Int {
        println("DEBUG: CaptureMomentViewModel.estimateFileSize() - estimating file size")
        
        var totalSizeMB = 0
        
        // Photo typically 2-5MB
        if (state.value.photoUri != null) {
            totalSizeMB += 3
            println("DEBUG: CaptureMomentViewModel.estimateFileSize() - photo detected, adding 3MB")
        }
        
        // Audio typically 1-3MB per minute
        if (state.value.audioUri != null) {
            totalSizeMB += 2
            println("DEBUG: CaptureMomentViewModel.estimateFileSize() - audio detected, adding 2MB")
        }
        
        // Base memory data ~0.1MB
        totalSizeMB += 1
        println("DEBUG: CaptureMomentViewModel.estimateFileSize() - base memory data, adding 1MB")
        
        println("DEBUG: CaptureMomentViewModel.estimateFileSize() - total estimated size: ${totalSizeMB}MB")
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
