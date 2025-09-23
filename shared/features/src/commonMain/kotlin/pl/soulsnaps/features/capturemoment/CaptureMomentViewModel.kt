package pl.soulsnaps.features.capturemoment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.access.guard.AccessResult
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.features.analytics.CapacityAnalytics
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.domain.service.AffirmationService
import pl.soulsnaps.domain.model.AffirmationRequest
import pl.soulsnaps.features.auth.UserSessionManager

class CaptureMomentViewModel(
    private val saveMemoryUseCase: SaveMemoryUseCase,
    private val accessGuard: AccessGuard,
    private val affirmationService: AffirmationService,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureMomentState())
    
    private val userId: String
        get() = userSessionManager.getCurrentUser()?.userId ?: "anonymous_user"
    
    init {
        println("DEBUG: CaptureMomentViewModel.init() - initial state date: ${_state.value.date}")
        println("DEBUG: CaptureMomentViewModel.init() - current timestamp: ${getCurrentTimeMillis()}")
    }
    val state: StateFlow<CaptureMomentState> = _state
    
    // CapacityAnalytics for tracking usage
    private val capacityAnalytics = CapacityAnalytics(accessGuard)

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
            
            // New affirmation intents
            is CaptureMomentIntent.ToggleAffirmationRequested -> {
                _state.update { it.copy(affirmationRequested = intent.requested) }
            }
            is CaptureMomentIntent.GenerateAffirmation -> generateAffirmation()
            is CaptureMomentIntent.ShowAffirmationDialog -> {
                _state.update { it.copy(showAffirmationDialog = true) }
            }
            is CaptureMomentIntent.HideAffirmationDialog -> {
                _state.update { it.copy(showAffirmationDialog = false) }
            }
            is CaptureMomentIntent.DismissAffirmationSnackbar -> {
                _state.update { it.copy(showAffirmationSnackbar = false) }
            }
        }
    }


    private fun saveMemory() {
        println("DEBUG: CaptureMomentViewModel.saveMemory() - starting memory creation with simplified flow")
        
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
                val recommendedPlan = accessGuard.getUpgradeRecommendation("memory.create")
                _state.update { 
                    it.copy(
                        showPaywall = true,
                        paywallReason = capacityResult.message ?: "Limit pojemności przekroczony",
                        recommendedPlan = recommendedPlan
                    ) 
                }
                return@launch
            }

            println("DEBUG: CaptureMomentViewModel.saveMemory() - creating memory with simplified approach")
            _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            try {
                // Use SaveMemoryUseCase 
                val memory = Memory(
                    id = 0, // Will be auto-generated
                    title = state.value.title,
                    description = state.value.description,
                    createdAt = getCurrentTimeMillis(),
                    updatedAt = getCurrentTimeMillis(),
                    mood = state.value.selectedMood,
                    photoUri = state.value.photoUri,
                    audioUri = state.value.audioUri,
                    locationName = state.value.location,
                    latitude = null, // TODO: Get from location
                    longitude = null, // TODO: Get from location
                    affirmation = null, // Will be generated by use case
                    isFavorite = false,
                    isSynced = false,
                    remotePhotoPath = null,
                    remoteAudioPath = null,
                    remoteId = null,
                    syncState = pl.soulsnaps.domain.model.SyncState.PENDING,
                    retryCount = 0,
                    errorMessage = null
                )
                
                val memoryId = saveMemoryUseCase(memory)
                
                println("DEBUG: CaptureMomentViewModel.saveMemory() - memory created successfully with ID: $memoryId")
                
                _state.update { 
                        it.copy(
                            isSaving = false, 
                            successMessage = "Pamięć została zapisana lokalnie i będzie zsynchronizowana w tle!",
                            savedMemoryId = memoryId
                        ) 
                    }
                    
                    // Affirmation is generated and saved by SaveMemoryUseCase
                    // Sync is automatically triggered by MemoryRepositoryImpl
                    println("DEBUG: CaptureMomentViewModel.saveMemory() - memory saved, sync will be handled by repository")
                
            } catch (e: Exception) {
                println("ERROR: CaptureMomentViewModel.saveMemory() - exception occurred while creating memory")
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
            val result = accessGuard.allowAction("current_user", "memory.create", "snaps.capacity", "feature.memories")
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
                val capacityInfo = accessGuard.getQuotaInfo("current_user", "snaps.capacity")
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
            capacityAnalytics.updateUsageStats(userId)
            _state.update { it.copy(showAnalytics = true) }
        }
    }
    
    /**
     * Update analytics
     */
    private fun updateAnalytics() {
        viewModelScope.launch {
            capacityAnalytics.updateUsageStats(userId)
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
    
    private fun generateAffirmation() {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingAffirmation = true, affirmationError = null) }
            
            try {
                val request = AffirmationRequest(
                    emotion = state.value.selectedMood?.name,
                    intensity = null, // Could be calculated from mood
                    timeOfDay = getTimeOfDay(state.value.date),
                    location = state.value.location,
                    tags = extractTagsFromDescription(state.value.description),
                    memoryId = state.value.savedMemoryId?.toString()
                )
                
                val result = affirmationService.generateAffirmation(request)
                
                if (result.success) {
                    val affirmationData = pl.soulsnaps.domain.model.AffirmationData(
                        content = result.content,
                        upgradedByAi = false,
                        affirmationRequested = true,
                        memoryId = state.value.savedMemoryId?.toString(),
                        emotion = request.emotion,
                        intensity = request.intensity,
                        timeOfDay = request.timeOfDay,
                        location = request.location,
                        tags = request.tags,
                        version = result.version,
                        generationTimeMs = result.generationTimeMs
                    )
                    
                    val affirmationId = affirmationService.saveAffirmation(affirmationData)
                    val savedAffirmation = affirmationData.copy(id = affirmationId)
                    
                    _state.update { 
                        it.copy(
                            isGeneratingAffirmation = false,
                            generatedAffirmationData = savedAffirmation,
                            showAffirmationSnackbar = true
                        ) 
                    }
                    
                    // Try AI upgrade in background
                    tryAiUpgrade(savedAffirmation)
                } else {
                    _state.update { 
                        it.copy(
                            isGeneratingAffirmation = false,
                            affirmationError = result.error ?: "Nie udało się wygenerować afirmacji"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isGeneratingAffirmation = false,
                        affirmationError = e.message ?: "Nie udało się wygenerować afirmacji"
                    ) 
                }
            }
        }
    }
    
    private fun tryAiUpgrade(originalAffirmation: pl.soulsnaps.domain.model.AffirmationData) {
        viewModelScope.launch {
            try {
                if (affirmationService.isAiUpgradeAvailable()) {
                    val upgradeResult = affirmationService.generateAiUpgrade(originalAffirmation)
                    
                    if (upgradeResult.success) {
                        val upgradedAffirmation = originalAffirmation.copy(
                            content = upgradeResult.content,
                            upgradedByAi = true,
                            version = upgradeResult.version,
                            generationTimeMs = upgradeResult.generationTimeMs
                        )
                        
                        val affirmationId = affirmationService.saveAffirmation(upgradedAffirmation)
                        val savedUpgradedAffirmation = upgradedAffirmation.copy(id = affirmationId)
                        
                        _state.update { 
                            it.copy(
                                generatedAffirmationData = savedUpgradedAffirmation,
                                showAffirmationSnackbar = true
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail for AI upgrade - don't show error to user
                println("AI upgrade failed: ${e.message}")
            }
        }
    }
    
    private fun getTimeOfDay(timestamp: Long): String {
        return pl.soulsnaps.utils.getTimeOfDay(timestamp)
    }
    
    private fun extractTagsFromDescription(description: String): List<String> {
        // Simple tag extraction - look for words starting with #
        return description.split(" ")
            .filter { it.startsWith("#") }
            .map { it.removePrefix("#") }
            .take(2) // Limit to 2 tags as per requirements
    }
}
