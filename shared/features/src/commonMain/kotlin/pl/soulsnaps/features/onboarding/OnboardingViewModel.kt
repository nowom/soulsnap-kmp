package pl.soulsnaps.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NextStep -> nextStep()
            is OnboardingIntent.PreviousStep -> previousStep()
            is OnboardingIntent.SkipVoiceSetup -> {
                _state.update { it.copy(voiceRecordingPath = null) }
                nextStep()
            }
            is OnboardingIntent.RecordVoice -> {
                _state.update { it.copy(voiceRecordingPath = intent.audioPath) }
                nextStep()
            }
            is OnboardingIntent.SelectGoal -> {
                _state.update { it.copy(selectedGoal = intent.goal) }
                nextStep()
            }
            is OnboardingIntent.GrantPermission -> {
                _state.update { 
                    it.copy(permissionsGranted = it.permissionsGranted + intent.permission) 
                }
                // Auto-advance if all permissions are granted
                if (_state.value.permissionsGranted.size >= Permission.values().size) {
                    nextStep()
                }
            }
            is OnboardingIntent.GetStarted -> {
                // TODO: Save onboarding data and navigate to main app
                completeOnboarding()
            }
        }
    }

    private fun nextStep() {
        val currentStep = _state.value.currentStep
        val nextStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.VOICE_SETUP
            OnboardingStep.VOICE_SETUP -> OnboardingStep.GOALS
            OnboardingStep.GOALS -> OnboardingStep.PERMISSIONS
            OnboardingStep.PERMISSIONS -> OnboardingStep.GET_STARTED
            OnboardingStep.GET_STARTED -> OnboardingStep.GET_STARTED // Already at end
        }
        _state.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val currentStep = _state.value.currentStep
        val previousStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME // Already at start
            OnboardingStep.VOICE_SETUP -> OnboardingStep.WELCOME
            OnboardingStep.GOALS -> OnboardingStep.VOICE_SETUP
            OnboardingStep.PERMISSIONS -> OnboardingStep.GOALS
            OnboardingStep.GET_STARTED -> OnboardingStep.PERMISSIONS
        }
        _state.update { it.copy(currentStep = previousStep) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // TODO: Save onboarding data to preferences/database
                // For now, just simulate a delay
                kotlinx.coroutines.delay(500)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to complete onboarding: ${e.message}"
                    )
                }
            }
        }
    }

    fun canGoNext(): Boolean {
        return when (_state.value.currentStep) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.VOICE_SETUP -> true // Can skip
            OnboardingStep.GOALS -> _state.value.selectedGoal != null
            OnboardingStep.PERMISSIONS -> _state.value.permissionsGranted.isNotEmpty()
            OnboardingStep.GET_STARTED -> true
        }
    }

    fun canGoPrevious(): Boolean {
        return _state.value.currentStep != OnboardingStep.WELCOME
    }

    fun getProgress(): Float {
        return (_state.value.currentStep.ordinal + 1).toFloat() / OnboardingStep.values().size
    }
} 