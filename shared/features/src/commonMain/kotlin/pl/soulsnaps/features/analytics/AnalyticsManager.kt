package pl.soulsnaps.features.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class AnalyticsManager(
    private val repository: AnalyticsRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _onboardingStartTime = MutableStateFlow<Long?>(null)
    private val _stepStartTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    init {
        coroutineScope.launch {
            repository.getEvents().collect { events ->
                _events.value = events
            }
        }
    }

    // Onboarding Analytics
    fun startOnboarding() {
        val startTime = Clock.System.now().toEpochMilliseconds()
        _onboardingStartTime.value = startTime
        trackEvent(AnalyticsEvent.OnboardingStarted(startTime))
    }

    fun startStep(step: String) {
        _stepStartTimes.value = _stepStartTimes.value + (step to Clock.System.now().toEpochMilliseconds())
        trackScreenView(step)
    }

    fun completeStep(step: String) {
        val startTime = _stepStartTimes.value[step]
        val timeSpent = startTime?.let { Clock.System.now().toEpochMilliseconds() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingStepCompleted(step, timeSpent))
    }

    fun skipStep(step: String) {
        trackEvent(AnalyticsEvent.OnboardingSkipped(step))
    }

    fun completeOnboarding(selectedFocus: String?, authMethod: String?) {
        val startTime = _onboardingStartTime.value
        val totalTime = startTime?.let { Clock.System.now().toEpochMilliseconds() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingCompleted(totalTime, selectedFocus, authMethod))
    }

    // Auth Analytics
    fun trackAuthAttempt(method: String, success: Boolean) {
        trackEvent(AnalyticsEvent.AuthAttempted(method, success))
    }

    // Feature Analytics
    fun trackFeatureUsage(feature: String) {
        coroutineScope.launch {
            repository.trackFeatureUsage(feature)
        }
    }

    // Emotion Analytics
    fun trackEmotionCapture(emotion: String, intensity: String) {
        trackEvent(AnalyticsEvent.EmotionCaptured(emotion, intensity))
    }

    // Screen Analytics
    fun trackScreenView(screen: String) {
        coroutineScope.launch {
            repository.trackScreenView(screen)
        }
    }

    // Error Analytics
    fun trackError(error: String, screen: String) {
        coroutineScope.launch {
            repository.trackError(error, screen)
        }
    }

    // Generic event tracking
    fun trackEvent(event: AnalyticsEvent) {
        coroutineScope.launch {
            repository.trackEvent(event)
        }
    }

    // Debug methods
    fun getAnalyticsSummary(): String {
        val currentEvents = events.value
        return buildString {
            appendLine("📊 Analytics Summary:")
            appendLine("Total Events: ${currentEvents.size}")
            appendLine("Events by Type:")
            currentEvents.groupBy { it::class.simpleName }
                .forEach { (type, events) ->
                    appendLine("  $type: ${events.size}")
                }
        }
    }

    fun clearAnalytics() {
        coroutineScope.launch {
            repository.clearEvents()
        }
    }
} 